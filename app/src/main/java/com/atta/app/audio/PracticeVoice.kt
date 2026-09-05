package com.atta.app.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * On-device voice for Practice. Wraps TextToSpeech: async engine init, the
 * app-language locale, and one line at a time with a completion callback.
 * Missing engine or voice degrades to [State.Unavailable] — never a crash.
 */
private const val SpeakTimeoutMs = 30_000L

class PracticeVoice(context: Context, language: String, private val onState: (State) -> Unit) {

    enum class State { Loading, Ready, Unavailable }

    private val main = Handler(Looper.getMainLooper())
    private val locale = if (language == "th") Locale.forLanguageTag("th-TH") else Locale.US
    private var ready = false
    private var onDone: (() -> Unit)? = null

    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        main.post {
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(locale)
                ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                onState(if (ready) State.Ready else State.Unavailable)
            } else {
                onState(State.Unavailable)
            }
        }
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = finish()
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finish()
            override fun onError(utteranceId: String?, errorCode: Int) = finish()

            private fun finish() {
                main.post {
                    val callback = onDone
                    onDone = null
                    callback?.invoke()
                }
            }
        })
    }

    private var failures = 0

    /**
     * Reads one line, suspending until the engine finishes it. Engines can die
     * mid-utterance without ever delivering a callback, so the wait is capped;
     * repeated failures flip the state to Unavailable instead of freezing the
     * practice loop.
     */
    suspend fun speak(line: String, slow: Boolean) {
        if (!ready) return
        val spoke = withTimeoutOrNull(SpeakTimeoutMs) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation { stop() }
                onDone = { if (cont.isActive) cont.resume(true) }
                tts.setSpeechRate(if (slow) 0.72f else 0.95f)
                val queued = tts.speak(line, TextToSpeech.QUEUE_FLUSH, null, "atta-practice-line")
                if (queued != TextToSpeech.SUCCESS && cont.isActive) {
                    onDone = null
                    cont.resume(false)
                }
            }
        } ?: false
        if (spoke) {
            failures = 0
        } else if (++failures >= 2) {
            ready = false
            onState(State.Unavailable)
        }
    }

    fun stop() {
        onDone = null
        tts.stop()
    }

    fun shutdown() {
        stop()
        tts.shutdown()
    }
}
