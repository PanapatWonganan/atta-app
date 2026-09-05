package com.atta.app.audio

import android.content.Context
import android.media.MediaPlayer
import com.atta.app.R

/** An ambient bed for Practice. None keeps the room silent. */
data class Mood(val id: String, val displayName: String, val rawRes: Int?)

object Moods {
    val None = Mood("none", "None", null)
    val Calm = Mood("calm", "Calm", R.raw.mood_calm)
    val Rain = Mood("rain", "Rain", R.raw.mood_rain)
    val Waves = Mood("waves", "Waves", R.raw.mood_waves)
    val All = listOf(None, Calm, Rain, Waves)

    fun byId(id: String?): Mood = All.firstOrNull { it.id == id } ?: Calm
}

/** Loops one bundled mood quietly under the voice. */
class MoodPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var currentRes: Int? = null

    fun play(mood: Mood) {
        val res = mood.rawRes ?: run { stop(); return }
        if (res == currentRes && player?.isPlaying == true) return
        stop()
        player = MediaPlayer.create(context, res)?.apply {
            isLooping = true
            setVolume(0.3f, 0.3f)
            start()
        }
        currentRes = res
    }

    fun stop() {
        player?.let { p ->
            runCatching { p.stop() }
            p.release()
        }
        player = null
        currentRes = null
    }
}
