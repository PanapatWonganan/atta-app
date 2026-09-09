package com.atta.app.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * One quiet funnel. Events record what people do (opened the paywall,
 * earned a pass, started practice) — never what they write or read. Lines,
 * custom text, and check-in history stay on the device.
 */
object AttaAnalytics {

    const val PaywallView = "paywall_view"
    const val FirstLineView = "first_line_view"
    const val CommitView = "commit_view"
    const val CompareView = "compare_view"
    const val ValueView = "value_view"
    const val TrialPromiseView = "trial_promise_view"
    const val Subscribe = "subscribe"
    const val RewardedEarned = "rewarded_earned"
    const val PracticeStart = "practice_start"
    const val WidgetPinned = "widget_pinned"
    const val OwnLineAdded = "own_line_added"
    const val CheckIn = "check_in"

    fun log(context: Context, event: String, key: String? = null, value: String? = null) {
        runCatching {
            val params = Bundle()
            if (key != null && value != null) params.putString(key, value)
            FirebaseAnalytics.getInstance(context.applicationContext).logEvent(event, params)
        }
    }
}
