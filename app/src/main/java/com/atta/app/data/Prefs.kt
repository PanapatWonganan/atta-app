package com.atta.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.attaDataStore by preferencesDataStore(name = "atta_prefs")

object Plans {
    const val None = "none"
    const val Free = "free"
    const val TrialWeekly = "trial_weekly"
    const val TrialYearly = "trial_yearly"
    const val Lifetime = "lifetime"

    fun isFree(plan: String): Boolean = plan == Free || plan == None

    fun label(plan: String): String = when (plan) {
        TrialWeekly -> "Weekly · trial"
        TrialYearly -> "Yearly · trial"
        Lifetime -> "Lifetime"
        Free -> "Free"
        else -> "—"
    }
}

data class AttaSettings(
    val onboardingDone: Boolean = false,
    val savedIds: Set<String> = emptySet(),
    val themeId: String = WidgetThemes.DefaultId,
    val focusIds: Set<String> = emptySet(),
    val morningHour: Int = 7, // window start for daily-line reminders
    val morningMinute: Int = 0,
    val eveningLine: Boolean = true,
    val remindersPerDay: Int = 3, // evenly spaced inside the window
    val windowEndHour: Int = 21,
    val windowEndMinute: Int = 0,
    val plan: String = Plans.None,
    val appearance: String = "system", // system | light | dark
    val language: String = "en", // en | th
    val practiceMood: String = "calm", // none | calm | rain | waves
    val practicePace: String = "slow", // slow | normal
    val customLines: Set<String> = emptySet(), // "<id>\u0001<text>" — see CustomLines
    val moodLog: Set<String> = emptySet(), // "<yyyy-MM-dd>|<calm|okay|heavy>", one per day
    val plusPassUntil: Long = 0L, // epoch ms; rewarded-ad day pass expiry
    val usageDays: Set<String> = emptySet(), // distinct "yyyy-MM-dd" the app was opened
    val reviewLastAskMs: Long = 0L,
    val reviewAskCount: Int = 0,
    val metDays: Set<String> = emptySet(), // days the line was actually met (hold, practice, check-in)
    val trialStartMs: Long = 0L, // when a trial plan was first taken; drives the day-5 note
    val paywallDismisses: Int = 0, // "Not now" count; the second one earns the weekly downsell
) {
    /** Free means no paid plan AND no live ad-earned day pass. */
    val freeTier: Boolean
        get() = Plans.isFree(plan) && System.currentTimeMillis() >= plusPassUntil
}

private object Keys {
    val OnboardingDone = booleanPreferencesKey("onboarding_done")
    val SavedIds = stringSetPreferencesKey("saved_ids")
    val ThemeId = stringPreferencesKey("theme_id")
    val FocusIds = stringSetPreferencesKey("focus_ids")
    val MorningHour = intPreferencesKey("morning_hour")
    val MorningMinute = intPreferencesKey("morning_minute")
    val EveningLine = booleanPreferencesKey("evening_line")
    val Plan = stringPreferencesKey("plan")
    val Appearance = stringPreferencesKey("appearance")
    val Language = stringPreferencesKey("language")
    val PracticeMood = stringPreferencesKey("practice_mood")
    val PracticePace = stringPreferencesKey("practice_pace")
    val CustomLines = stringSetPreferencesKey("custom_lines")
    val MoodLog = stringSetPreferencesKey("mood_log")
    val PlusPassUntil = longPreferencesKey("plus_pass_until")
    val UsageDays = stringSetPreferencesKey("usage_days")
    val ReviewLastAsk = longPreferencesKey("review_last_ask")
    val ReviewAskCount = intPreferencesKey("review_ask_count")
    val RemindersPerDay = intPreferencesKey("reminders_per_day")
    val WindowEndHour = intPreferencesKey("window_end_hour")
    val WindowEndMinute = intPreferencesKey("window_end_minute")
    val MetDays = stringSetPreferencesKey("met_days")
    val TrialStartMs = longPreferencesKey("trial_start_ms")
    val PaywallDismisses = intPreferencesKey("paywall_dismisses")
}

private fun Preferences.toSettings() = AttaSettings(
    onboardingDone = this[Keys.OnboardingDone] ?: false,
    savedIds = this[Keys.SavedIds] ?: emptySet(),
    themeId = this[Keys.ThemeId] ?: WidgetThemes.DefaultId,
    focusIds = this[Keys.FocusIds] ?: emptySet(),
    morningHour = this[Keys.MorningHour] ?: 7,
    morningMinute = this[Keys.MorningMinute] ?: 0,
    eveningLine = this[Keys.EveningLine] ?: true,
    remindersPerDay = this[Keys.RemindersPerDay] ?: 3,
    windowEndHour = this[Keys.WindowEndHour] ?: 21,
    windowEndMinute = this[Keys.WindowEndMinute] ?: 0,
    plan = this[Keys.Plan] ?: Plans.None,
    appearance = this[Keys.Appearance] ?: "system",
    language = this[Keys.Language] ?: "en",
    practiceMood = this[Keys.PracticeMood] ?: "calm",
    practicePace = this[Keys.PracticePace] ?: "slow",
    customLines = this[Keys.CustomLines] ?: emptySet(),
    moodLog = this[Keys.MoodLog] ?: emptySet(),
    plusPassUntil = this[Keys.PlusPassUntil] ?: 0L,
    usageDays = this[Keys.UsageDays] ?: emptySet(),
    reviewLastAskMs = this[Keys.ReviewLastAsk] ?: 0L,
    reviewAskCount = this[Keys.ReviewAskCount] ?: 0,
    metDays = this[Keys.MetDays] ?: emptySet(),
    trialStartMs = this[Keys.TrialStartMs] ?: 0L,
    paywallDismisses = this[Keys.PaywallDismisses] ?: 0,
)

class AttaPrefs(private val context: Context) {

    val settings: Flow<AttaSettings> = context.attaDataStore.data.map { it.toSettings() }

    suspend fun snapshot(): AttaSettings = context.attaDataStore.data.first().toSettings()

    suspend fun setOnboardingDone() =
        context.attaDataStore.edit { it[Keys.OnboardingDone] = true }

    suspend fun toggleSaved(id: String) = context.attaDataStore.edit {
        val current = it[Keys.SavedIds] ?: emptySet()
        it[Keys.SavedIds] = if (id in current) current - id else current + id
    }

    suspend fun setThemeId(themeId: String) =
        context.attaDataStore.edit { it[Keys.ThemeId] = themeId }

    suspend fun setFocusIds(ids: Set<String>) =
        context.attaDataStore.edit { it[Keys.FocusIds] = ids }

    suspend fun setMorningTime(hour: Int, minute: Int) = context.attaDataStore.edit {
        it[Keys.MorningHour] = hour
        it[Keys.MorningMinute] = minute
    }

    suspend fun setEveningLine(enabled: Boolean) =
        context.attaDataStore.edit { it[Keys.EveningLine] = enabled }

    suspend fun setRemindersPerDay(count: Int) =
        context.attaDataStore.edit { it[Keys.RemindersPerDay] = count }

    suspend fun setWindowEnd(hour: Int, minute: Int) = context.attaDataStore.edit {
        it[Keys.WindowEndHour] = hour
        it[Keys.WindowEndMinute] = minute
    }

    suspend fun setPlan(plan: String) =
        context.attaDataStore.edit { it[Keys.Plan] = plan }

    suspend fun setAppearance(value: String) =
        context.attaDataStore.edit { it[Keys.Appearance] = value }

    suspend fun setLanguage(value: String) =
        context.attaDataStore.edit { it[Keys.Language] = value }

    suspend fun setPracticeMood(value: String) =
        context.attaDataStore.edit { it[Keys.PracticeMood] = value }

    suspend fun setPracticePace(value: String) =
        context.attaDataStore.edit { it[Keys.PracticePace] = value }

    suspend fun addCustomLine(entry: String) = context.attaDataStore.edit {
        it[Keys.CustomLines] = (it[Keys.CustomLines] ?: emptySet()) + entry
    }

    suspend fun removeCustomLine(id: String) = context.attaDataStore.edit {
        it[Keys.CustomLines] = (it[Keys.CustomLines] ?: emptySet())
            .filterNot { entry -> entry.substringBefore('\u0001') == id }.toSet()
    }

    suspend fun setPlusPassUntil(epochMs: Long) =
        context.attaDataStore.edit { it[Keys.PlusPassUntil] = epochMs }

    /** One entry per distinct day; keeps only the most recent 60. */
    suspend fun recordUsageDay(date: String) = context.attaDataStore.edit {
        val days = (it[Keys.UsageDays] ?: emptySet()) + date
        it[Keys.UsageDays] = days.sortedDescending().take(60).toSet()
    }

    suspend fun recordReviewAsk(nowMs: Long) = context.attaDataStore.edit {
        it[Keys.ReviewLastAsk] = nowMs
        it[Keys.ReviewAskCount] = (it[Keys.ReviewAskCount] ?: 0) + 1
    }

    /** One entry per day: relogging a day replaces its value. */
    suspend fun logMood(date: String, value: String) = context.attaDataStore.edit {
        it[Keys.MoodLog] = (it[Keys.MoodLog] ?: emptySet())
            .filterNot { entry -> entry.substringBefore('|') == date }.toSet() + "$date|$value"
    }

    /** The day counts as met once; keeps the most recent 60 like usageDays. */
    suspend fun recordMetDay(date: String) = context.attaDataStore.edit {
        val days = (it[Keys.MetDays] ?: emptySet()) + date
        it[Keys.MetDays] = days.sortedDescending().take(60).toSet()
    }

    /** Stamped once per trial: re-purchasing restarts the day-5 clock. */
    suspend fun setTrialStart(epochMs: Long) =
        context.attaDataStore.edit { it[Keys.TrialStartMs] = epochMs }

    suspend fun recordPaywallDismiss() = context.attaDataStore.edit {
        it[Keys.PaywallDismisses] = (it[Keys.PaywallDismisses] ?: 0) + 1
    }
}
