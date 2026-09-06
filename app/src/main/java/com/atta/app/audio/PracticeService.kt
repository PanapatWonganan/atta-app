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
    val source: String = PracticeQueue.SourceFeed,
    val timerMinutes: Int = 0, // 0 = no sleep timer
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
    private var feed: List<Affirmation> = emptyList()
    private var loopJob: Job? = null
    private var timerJob: Job? = null
    private var endPending = false
    private var silentLines = 0
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
                val previous = settings
                settings = latest
                // The queue's inputs changed mid-session: rebuild it so the
                // voice never keeps reading a stale list.
                if (_state.value.active && previous != null && (
                        previous.focusIds != latest.focusIds ||
                            previous.savedIds != latest.savedIds ||
                            previous.customLines != latest.customLines
                        )
                ) {
                    feed = PracticeQueue.build(
                        _state.value.source, latest, LocalDate.now(), LocalTime.now().hour >= 18,
                    )
                    if (feed.isEmpty()) {
                        end()
                        return@collect
                    }
                    _state.update { it.copy(index = it.index.coerceIn(0, feed.lastIndex)) }
                }
                if (_state.value.playing) applyMood()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionStart -> begin(
                intent.getIntExtra(ExtraIndex, 0),
                intent.getStringExtra(ExtraSource) ?: PracticeQueue.SourceFeed,
            )
            ActionToggle -> if (_state.value.playing) pause() else resume()
            ActionTimer -> setTimerMinutes(intent.getIntExtra(ExtraMinutes, 0))
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
    private fun begin(startIndex: Int, source: String) {
        // Silence the old reading immediately — a switch must never let the
        // previous queue slip in one more line while the new one spins up.
        loopJob?.cancel()
        voice?.stop()
        startForeground(NotificationId, buildNotification("Practice"))
        scope.launch {
            if (settings == null) settings = prefs.snapshot()
            val current = settings ?: return@launch
            // Always rebuild: focus, saved, and custom lines may have changed
            // since the queue was last cached.
            feed = PracticeQueue.build(source, current, LocalDate.now(), LocalTime.now().hour >= 18)
            _state.update { it.copy(source = source) }
            if (feed.isEmpty()) {
                end()
                return@launch
            }
            if (voice == null) createVoice(current.language)
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
        timerJob?.cancel()
        endPending = false
        voice?.stop()
        moodPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        _state.value = PracticeState()
        stopSelf()
    }

    /** Sleep timer: wall-clock; when it fires the mood fades and the session
     * ends after the line being read. 0 clears it. */
    private fun setTimerMinutes(minutes: Int) {
        timerJob?.cancel()
        endPending = false
        _state.update { it.copy(timerMinutes = minutes) }
        if (minutes <= 0) return
        timerJob = scope.launch {
            delay(minutes * 60_000L)
            if (_state.value.playing) {
                endPending = true
                repeat(FadeSteps) { step ->
                    moodPlayer.setLevel(1f - (step + 1f) / FadeSteps)
                    delay(FadeStepMs)
                }
            } else {
                end()
            }
        }
    }

    private fun runLoop() {
        loopJob?.cancel()
        loopJob = scope.launch {
            applyMood()
            while (isActive) {
                val current = settings ?: break
                val entry = feed.getOrNull(_state.value.index) ?: break
                updateSession()
                notify(buildNotification(entry.text(current.language)))
                when (voiceState) {
                    PracticeVoice.State.Loading -> {
                        delay(400)
                        continue
                    }
                    // Voice can be switched off (pace "off"): the mood bed and
                    // the line rhythm continue in silence.
                    PracticeVoice.State.Ready ->
                        if (current.practicePace == "off") {
                            delay(SilentLineMs)
                        } else {
                            delay(700)
                            voice?.speak(
                                entry.text(current.language),
                                current.practicePace != "normal",
                            )
                            delay(LineGapMs)
                        }
                    // Engines die mid-utterance and restart on their own; keep
                    // the rhythm in silence but try a fresh connection every
                    // couple of lines instead of staying mute forever.
                    PracticeVoice.State.Unavailable -> {
                        delay(SilentLineMs)
                        silentLines++
                        if (silentLines % 2 == 0) {
                            voice?.shutdown()
                            voice = null
                            createVoice(current.language)
                        }
                    }
                }
                if (endPending) {
                    end()
                    break
                }
                _state.update { it.copy(index = (it.index + 1) % feed.size) }
            }
        }
    }

    private fun createVoice(language: String) {
        voiceState = PracticeVoice.State.Loading
        voice = PracticeVoice(this, language) { newState ->
            voiceState = newState
            if (newState == PracticeVoice.State.Ready) silentLines = 0
            _state.update {
                it.copy(voiceUnavailable = newState == PracticeVoice.State.Unavailable)
            }
        }
    }

    private fun applyMood() {
        val current = settings ?: return
        val mood = if (current.freeTier) Moods.None else Moods.byId(current.practiceMood)
        if (_state.value.playing) moodPlayer.play(mood) else moodPlayer.stop()
    }

    private fun currentLine(): String {
        val current = settings ?: return "Practice"
        return feed.getOrNull(_state.value.index)?.text(current.language) ?: "Practice"
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
        private const val ActionTimer = "com.atta.app.practice.TIMER"
        private const val ActionStop = "com.atta.app.practice.STOP"
        private const val ExtraIndex = "index"
        private const val ExtraSource = "source"
        private const val ExtraMinutes = "minutes"
        private const val LineGapMs = 4_000L
        private const val SilentLineMs = 9_000L
        private const val FadeSteps = 12
        private const val FadeStepMs = 400L

        private val _state = MutableStateFlow(PracticeState())
        val state: StateFlow<PracticeState> = _state

        /** Starts practice at a queue position, or jumps the running session there. */
        fun start(context: Context, index: Int, source: String = PracticeQueue.SourceFeed) {
            context.startForegroundService(
                Intent(context, PracticeService::class.java)
                    .setAction(ActionStart)
                    .putExtra(ExtraIndex, index)
                    .putExtra(ExtraSource, source),
            )
        }

        fun toggle(context: Context) {
            context.startService(
                Intent(context, PracticeService::class.java).setAction(ActionToggle),
            )
        }

        fun setTimer(context: Context, minutes: Int) {
            context.startService(
                Intent(context, PracticeService::class.java)
                    .setAction(ActionTimer)
                    .putExtra(ExtraMinutes, minutes),
            )
        }
    }
}
