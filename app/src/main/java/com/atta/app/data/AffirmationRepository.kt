package com.atta.app.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Deterministic line-of-day selection: the same date, focus set, and daypart
 * always produce the same line, so the widget, notification, and home feed
 * agree without any shared mutable state.
 */
object AffirmationRepository {

    fun lineFor(
        date: LocalDate,
        focusIds: Set<String> = emptySet(),
        evening: Boolean = false,
    ): Affirmation {
        val pool = Affirmations.All.filter {
            if (evening) it.daypart != Daypart.MORNING else it.daypart != Daypart.NIGHT
        }
        val focused = pool.filter { it.categoryId in focusIds }
        val candidates = if (focused.isNotEmpty()) focused else pool
        val idx = Math.floorMod(date.toEpochDay() + if (evening) 1L else 0L, candidates.size.toLong())
        return candidates[idx.toInt()]
    }

    /**
     * A distinct line per reminder slot within a day. Deterministic, so a
     * rescheduled worker repeats its own line instead of drifting.
     */
    fun lineForSlot(
        date: LocalDate,
        slot: Int,
        focusIds: Set<String> = emptySet(),
        evening: Boolean = false,
    ): Affirmation {
        val pool = Affirmations.All.filter {
            if (evening) it.daypart != Daypart.MORNING else it.daypart != Daypart.NIGHT
        }
        val focused = pool.filter { it.categoryId in focusIds }
        val candidates = if (focused.isNotEmpty()) focused else pool
        val idx = Math.floorMod(date.toEpochDay() * 7 + slot, candidates.size.toLong())
        return candidates[idx.toInt()]
    }

    /** Home feed: today first, then one line per previous day. */
    fun feed(
        today: LocalDate,
        days: Int,
        focusIds: Set<String> = emptySet(),
        evening: Boolean = false,
    ): List<Pair<LocalDate, Affirmation>> =
        (0 until days).map { offset ->
            val date = today.minusDays(offset.toLong())
            date to lineFor(date, focusIds, evening && offset == 0)
        }

    private fun locale(lang: String): Locale =
        if (lang == "th") Locale.forLanguageTag("th-TH") else Locale.ENGLISH

    /** "1 Sep" — the widget/card date stamp. */
    fun shortDate(date: LocalDate, lang: String = "en"): String =
        date.format(DateTimeFormatter.ofPattern("d MMM", locale(lang)))

    /** "Monday, 1 September" — lock screen and home header. */
    fun longDate(date: LocalDate, lang: String = "en"): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale(lang)))
}
