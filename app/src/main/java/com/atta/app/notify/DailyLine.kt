package com.atta.app.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.atta.app.MainActivity
import com.atta.app.R
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaPrefs
import com.atta.app.widget.AttaWidgetUpdater
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "daily_line"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily line",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "One quiet line at the hour you choose."
        }
        manager.createNotificationChannel(channel)
    }

    /** The line itself is the preview — the notification is the product. */
    fun show(context: Context, title: String, line: String, evening: Boolean) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_atta)
            .setContentTitle(title)
            .setContentText("“$line”")
            .setStyle(NotificationCompat.BigTextStyle().bigText("“$line”"))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(if (evening) 2 else 1, notification)
    }
}

class DailyLineWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val evening = inputData.getBoolean(KEY_EVENING, false)
        val settings = AttaPrefs(applicationContext).snapshot()
        val affirmation = AffirmationRepository.lineFor(LocalDate.now(), settings.focusIds, evening)
        val line = affirmation.text(settings.language).replace("\n", " ")
        NotificationHelper.show(
            context = applicationContext,
            title = if (evening) "Your evening line is ready" else "Your morning line is ready",
            line = line,
            evening = evening,
        )
        runCatching { AttaWidgetUpdater.updateAll(applicationContext) }
        DailyLineScheduler.scheduleNext(applicationContext, evening)
        return Result.success()
    }

    companion object {
        const val KEY_EVENING = "evening"
    }
}

object DailyLineScheduler {

    private const val MORNING_WORK = "atta_daily_morning"
    private const val EVENING_WORK = "atta_daily_evening"
    private const val EVENING_HOUR = 21

    suspend fun schedule(context: Context) {
        val settings = AttaPrefs(context).snapshot()
        enqueue(context, MORNING_WORK, settings.morningHour, settings.morningMinute, evening = false)
        if (settings.eveningLine) {
            enqueue(context, EVENING_WORK, EVENING_HOUR, 0, evening = true)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork(EVENING_WORK)
        }
    }

    suspend fun scheduleNext(context: Context, evening: Boolean) {
        val settings = AttaPrefs(context).snapshot()
        if (evening) {
            if (settings.eveningLine) enqueue(context, EVENING_WORK, EVENING_HOUR, 0, evening = true)
        } else {
            enqueue(context, MORNING_WORK, settings.morningHour, settings.morningMinute, evening = false)
        }
    }

    private fun enqueue(context: Context, name: String, hour: Int, minute: Int, evening: Boolean) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, minute)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMs = Duration.between(now, next).toMillis()
        val request = OneTimeWorkRequestBuilder<DailyLineWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(DailyLineWorker.KEY_EVENING to evening))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }
}
