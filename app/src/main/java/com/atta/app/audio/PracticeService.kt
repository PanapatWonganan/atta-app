package com.atta.app.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import com.atta.app.MainActivity
import com.atta.app.R
import com.atta.app.data.Affirmation
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.Plans
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** What the Practice screen needs to mirror the service. */
data class PracticeState(
    val active: Boolean = false,
    val playing: Boolean = false,
    val index: Int = 0,
    val voiceUnavailable: Boolean = false,
)

/**
 * Foreground media service so Practice keeps reading with the screen off.
 * Owns the voice, the mood bed, and the line loop; the screen is a remote.
 * Lock-screen and notification controls go through a framework MediaSession.
 */
class PracticeService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefs: AttaPrefs
    private lateinit var moodPlayer: MoodPlayer
    private var voice: PracticeVoice? = null
    private var voiceState = PracticeVoice.State.Loading
    private var settings: AttaSettings? = null
    private var feed: List<Pair<LocalDate, Affirmation>> = emptyList()
    private var loopJob: Job? = null
    private var session: MediaSession? = null
    private var focusRequest: AudioFocusRequest? = null

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS ||
            change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
            change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
        ) {
            pause()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = AttaPrefs(this)
        moodPlayer = MoodPlayer(this)
        ensureChannel()
        session = MediaSession(this, "atta-practice").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = resume()
                override fun onPause() = pause()
                override fun onStop() = end()
            })
            isActive = true
        }
        scope.launch {
            prefs.settings.collect { latest ->
                settings = latest
                if (_state.value.playing) applyMood()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionStart -> begin(intent.getIntExtra(ExtraIndex, 0))
            ActionToggle -> if (_state.value.playing) pause() else resume()
            ActionStop -> end()
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) = end()

    override fun onDestroy() {
        loopJob?.cancel()
        voice?.shutdown()
        moodPlayer.stop()
        abandonFocus()
        session?.release()
        scope.cancel()
        _state.value = PracticeState()
        super.onDestroy()
    }

    /** Start fresh, or jump the running session to another card's line. */
    private fun begin(startIndex: Int) {
        startForeground(NotificationId, buildNotification("Practice"))
        scope.launch {
            if (settings == null) settings = prefs.snapshot()
            val current = settings ?: return@launch
            if (feed.isEmpty()) {
                feed = AffirmationRepository.feed(
                    LocalDate.now(), 30, current.focusIds, LocalTime.now().hour >= 18,
                )
            }
            if (voice == null) {
                voice = PracticeVoice(this@PracticeService, current.language) { newState ->
                    voiceState = newState
                    _state.update {
                        it.copy(voiceUnavailable = newState == PracticeVoice.State.Unavailable)
                    }
                }
            }
            _state.update {
                it.copy(
                    active = true,
                    playing = true,
                    index = startIndex.coerceIn(0, feed.lastIndex),
                )
            }
            requestFocus()
            runLoop()
        }
    }

    private fun resume() {
        if (!_state.value.active || _state.value.playing) return
        _state.update { it.copy(playing = true) }
        startForeground(NotificationId, buildNotification(currentLine()))
        requestFocus()
        runLoop()
    }

    private fun pause() {
        if (!_state.value.playing) return
        loopJob?.cancel()
        voice?.stop()
        moodPlayer.stop()
        _state.update { it.copy(playing = false) }
        updateSession()
        // Paused practice shouldn't pin a notification: detach so a swipe ends it.
        stopForeground(STOP_FOREGROUND_DETACH)
        notify(buildNotification(currentLine()))
    }

    private fun end() {
        loopJob?.cancel()
        voice?.stop()
        moodPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        _state.value = PracticeState()
        stopSelf()
    }

    private fun runLoop() {
        loopJob?.cancel()
        loopJob = scope.launch {
            applyMood()
            while (isActive) {
                val current = settings ?: break
                val entry = feed.getOrNull(_state.value.index) ?: break
                updateSession()
                notify(buildNotification(entry.second.text(current.language)))
                when (voiceState) {
                    PracticeVoice.State.Loading -> {
                        delay(400)
                        continue
                    }
                    PracticeVoice.State.Ready -> {
                        delay(700)
                        voice?.speak(
                            entry.second.text(current.language),
                            current.practicePace != "normal",
                        )
                        delay(LineGapMs)
                    }
                    PracticeVoice.State.Unavailable -> delay(SilentLineMs)
                }
                _state.update { it.copy(index = (it.index + 1) % feed.size) }
            }
        }
    }

    private fun applyMood() {
        val current = settings ?: return
        val mood = if (Plans.isFree(current.plan)) Moods.None else Moods.byId(current.practiceMood)
        if (_state.value.playing) moodPlayer.play(mood) else moodPlayer.stop()
    }

    private fun currentLine(): String {
        val current = settings ?: return "Practice"
        return feed.getOrNull(_state.value.index)?.second?.text(current.language) ?: "Practice"
    }

    private fun requestFocus() {
        val manager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setOnAudioFocusChangeListener(focusListener)
            .build()
        focusRequest = request
        manager.requestAudioFocus(request)
    }

    private fun abandonFocus() {
        val manager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest?.let { manager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun updateSession() {
        val playing = _state.value.playing
        session?.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_STOP,
                )
                .setState(
                    if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    1f,
                )
                .build(),
        )
        session?.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentLine().replace("\n", " "))
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "ATTA · Practice")
                .build(),
        )
    }

    private fun ensureChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(ChannelId, "Practice", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Playback controls while practice is reading."
            },
        )
    }

    private fun buildNotification(line: String): Notification {
        val playing = _state.value.playing
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val toggle = servicePendingIntent(1, ActionToggle)
        val stop = servicePendingIntent(2, ActionStop)
        return Notification.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_stat_atta)
            .setContentTitle(line.replace("\n", " "))
            .setContentText("ATTA · Practice")
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session?.sessionToken)
                    .setShowActionsInCompactView(0, 1),
            )
            .setContentIntent(open)
            .setDeleteIntent(stop)
            .setOngoing(playing)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(
                Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(
                        this,
                        if (playing) android.R.drawable.ic_media_pause
                        else android.R.drawable.ic_media_play,
                    ),
                    if (playing) "Pause" else "Play",
                    toggle,
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(
                        this, android.R.drawable.ic_menu_close_clear_cancel,
                    ),
                    "End",
                    stop,
                ).build(),
            )
            .build()
    }

    private fun servicePendingIntent(code: Int, action: String): PendingIntent =
        PendingIntent.getService(
            this, code,
            Intent(this, PracticeService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun notify(notification: Notification) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationId, notification)
    }

    companion object {
        private const val ChannelId = "practice"
        private const val NotificationId = 3
        private const val ActionStart = "com.atta.app.practice.START"
        private const val ActionToggle = "com.atta.app.practice.TOGGLE"
        private const val ActionStop = "com.atta.app.practice.STOP"
        private const val ExtraIndex = "index"
        private const val LineGapMs = 4_000L
        private const val SilentLineMs = 9_000L

        private val _state = MutableStateFlow(PracticeState())
        val state: StateFlow<PracticeState> = _state

        /** Starts practice at a card, or jumps the running session there. */
        fun start(context: Context, index: Int) {
            context.startForegroundService(
                Intent(context, PracticeService::class.java)
                    .setAction(ActionStart)
                    .putExtra(ExtraIndex, index),
            )
        }

        fun toggle(context: Context) {
            context.startService(
                Intent(context, PracticeService::class.java).setAction(ActionToggle),
            )
        }
    }
}
