package com.atta.app.review

import android.app.Activity
import com.atta.app.data.AttaPrefs
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * In-app review, asked rarely and only at happy moments: right after a
 * "Calm" check-in, or after the third saved line. Play's own dialog decides
 * whether it actually appears (it rations itself), so our job is to spend
 * those chances on users who are demonstrably enjoying the app:
 *
 *  - at least [MinUsageDays] distinct days of use,
 *  - at most one ask per [CooldownDays],
 *  - at most [LifetimeAsks] asks ever.
 *
 * No incentives, no pre-filtering questions — both are Play-policy traps.
 */
object ReviewPrompter {

    private const val MinUsageDays = 3
    private const val CooldownDays = 30
    private const val LifetimeAsks = 5

    suspend fun maybeAsk(activity: Activity, prefs: AttaPrefs) {
        val settings = prefs.snapshot()
        val now = System.currentTimeMillis()
        if (settings.usageDays.size < MinUsageDays) return
        if (settings.reviewAskCount >= LifetimeAsks) return
        if (now - settings.reviewLastAskMs < CooldownDays * 24L * 60 * 60 * 1000) return

        // Count the attempt before launching: Play gives no shown/hidden
        // signal, and retry-hammering its quota helps no one.
        prefs.recordReviewAsk(now)
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnSuccessListener { info ->
                manager.launchReviewFlow(activity, info)
            }
        }
    }
}
