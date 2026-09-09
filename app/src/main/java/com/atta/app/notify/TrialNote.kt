package com.atta.app.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.atta.app.data.AttaPrefs
import com.atta.app.data.Plans
import java.util.concurrent.TimeUnit

/**
 * The promise the trial-promise screen makes: a quiet note on day 5 of the
 * free week, two days before anything charges. Scheduled whenever a trial
 * plan is taken; the worker re-checks the plan at fire time so a lifetime
 * upgrade or cancellation-to-free never gets a stray nudge.
 */
object TrialNote {

    private const val WorkName = "atta_trial_day5"
    private const val NoteDay = 5L

    fun schedule(context: Context, trialStartMs: Long) {
        val fireAt = trialStartMs + TimeUnit.DAYS.toMillis(NoteDay)
        val delay = fireAt - System.currentTimeMillis()
        if (delay <= 0) return
        val request = OneTimeWorkRequestBuilder<TrialNoteWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WorkName, ExistingWorkPolicy.REPLACE, request)
    }
}

class TrialNoteWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = AttaPrefs(applicationContext).snapshot()
        val onTrial = settings.plan == Plans.TrialWeekly || settings.plan == Plans.TrialYearly
        if (!onTrial) return Result.success()
        val th = settings.language == "th"
        NotificationHelper.ensureChannel(applicationContext)
        NotificationHelper.show(
            context = applicationContext,
            title = if (th) "อีกสองวันครบสัปดาห์ฟรีของคุณ" else "Two days left of your free week",
            line = if (th) {
                "จะเก็บไว้หรือปล่อยไปก็ได้ ไม่มีอะไรตัดเงินก่อนวันที่ 7"
            } else {
                "Still yours to keep or let go. Nothing charges before day 7."
            },
            lineId = "",
            notificationId = 90,
        )
        return Result.success()
    }
}
