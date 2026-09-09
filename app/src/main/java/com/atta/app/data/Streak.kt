package com.atta.app.data

import java.time.LocalDate

/**
 * The quiet streak: consecutive met days, still alive if today simply
 * hasn't happened yet. No fire, no guilt — a broken streak just starts
 * again at one.
 */
object Streak {

    fun count(metDays: Set<String>, today: LocalDate = LocalDate.now()): Int {
        var day = if (today.toString() in metDays) today else today.minusDays(1)
        var run = 0
        while (day.toString() in metDays) {
            run++
            day = day.minusDays(1)
        }
        return run
    }

    fun metToday(metDays: Set<String>, today: LocalDate = LocalDate.now()): Boolean =
        today.toString() in metDays

    /** Last seven days, oldest first, paired with met/mood for the summary. */
    fun week(
        metDays: Set<String>,
        moodLog: Set<String>,
        today: LocalDate = LocalDate.now(),
    ): List<DaySummary> = (6 downTo 0).map { back ->
        val date = today.minusDays(back.toLong())
        val key = date.toString()
        DaySummary(
            date = key,
            met = key in metDays,
            mood = moodLog.firstOrNull { it.substringBefore('|') == key }
                ?.substringAfter('|'),
        )
    }

    data class DaySummary(val date: String, val met: Boolean, val mood: String?)
}
