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
import com.atta.app.data.AttaSettings
import com.atta.app.widget.AttaWidgetUpdater
import com.atta.app.widget.OpenLineExtra
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
            description = "One quiet line at the hours you choose."
        }
        manager.createNotificationChannel(channel)
    }

    /** The line itself is the preview — the notification is the product. */
    fun show(context: Context, title: String, line: String, lineId: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(OpenLineExtra, lineId)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
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
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}

class DailyLineWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = inputData.getInt(KEY_SLOT, 0)
        val settings = AttaPrefs(applicationContext).snapshot()
        val evening = DailyLineScheduler.slotTime(settings, slot).hour >= 18
        val affirmation =
            AffirmationRepository.lineForSlot(LocalDate.now(), slot, settings.focusIds, evening)
        val line = affirmation.text(settings.language).replace("\n", " ")
        NotificationHelper.show(
            context = applicationContext,
            title = when {
                slot == 0 -> "Your morning line is ready"
                evening -> "Your evening line is ready"
                else -> "A line for you"
            },
            line = line,
            lineId = affirmation.id,
            notificationId = 100 + slot,
        )
        runCatching { AttaWidgetUpdater.updateAll(applicationContext) }
        DailyLineScheduler.scheduleNext(applicationContext, slot)
        return Result.success()
    }

    companion object {
        const val KEY_SLOT = "slot"
    }
}

/**
 * N reminders a day, evenly spaced inside the user's window — each slot is
 * its own unique work that notifies and reschedules itself daily.
 */
object DailyLineScheduler {

    private const val MaxSlots = 10

    suspend fun schedule(context: Context) {
        val settings = AttaPrefs(context).snapshot()
        val manager = WorkManager.getInstance(context)
        // Names from the old two-a-day scheduler.
        manager.cancelUniqueWork("atta_daily_morning")
        manager.cancelUniqueWork("atta_daily_evening")
        val count = settings.remindersPerDay.coerceIn(1, MaxSlots)
        for (slot in 0 until MaxSlots) {
            if (slot < count) {
                enqueue(context, slot, settings)
            } else {
                manager.cancelUniqueWork(workName(slot))
            }
        }
    }

    suspend fun scheduleNext(context: Context, slot: Int) {
        val settings = AttaPrefs(context).snapshot()
        if (slot < settings.remindersPerDay.coerceIn(1, MaxSlots)) {
            enqueue(context, slot, settings)
        }
    }

    /** Slot i sits at start + i * span/(count-1); a single slot sits at start. */
    fun slotTime(settings: AttaSettings, slot: Int): LocalTime {
        val start = settings.morningHour * 60 + settings.morningMinute
        val end = settings.windowEndHour * 60 + settings.windowEndMinute
        val count = settings.remindersPerDay.coerceIn(1, MaxSlots)
        val span = (end - start).coerceAtLeast(0)
        val minutes = if (count == 1) start else start + slot * span / (count - 1)
        return LocalTime.of(
            (minutes / 60).coerceIn(0, 23),
            (minutes % 60).coerceIn(0, 59),
        )
    }

    private fun workName(slot: Int) = "atta_line_slot_$slot"

    private fun enqueue(context: Context, slot: Int, settings: AttaSettings) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(slotTime(settings, slot))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMs = Duration.between(now, next).toMillis()
        val request = OneTimeWorkRequestBuilder<DailyLineWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(DailyLineWorker.KEY_SLOT to slot))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(slot), ExistingWorkPolicy.REPLACE, request)
    }
}
