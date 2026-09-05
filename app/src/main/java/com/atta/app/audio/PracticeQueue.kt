package com.atta.app.audio

import com.atta.app.data.Affirmation
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.Affirmations
import com.atta.app.data.AttaSettings
import com.atta.app.data.CustomLines
import java.time.LocalDate

/**
 * The one list Practice reads from, built identically by the service and the
 * screen (same inputs → same order, no shared state needed).
 */
object PracticeQueue {

    const val SourceFeed = "feed"
    const val SourceSaved = "saved"

    fun build(
        source: String,
        settings: AttaSettings,
        today: LocalDate,
        evening: Boolean,
    ): List<Affirmation> = when (source) {
        SourceSaved ->
            CustomLines.parse(settings.customLines) +
                Affirmations.All.filter { it.id in settings.savedIds }
        else ->
            AffirmationRepository.feed(today, 30, settings.focusIds, evening).map { it.second }
    }
}
