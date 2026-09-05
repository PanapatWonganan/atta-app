package com.atta.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/** atta.motion.* — breath-paced. No springs anywhere; overshoot breaks the register. */
object AttaMotion {
    const val ScreenEnterMs = 480
    const val CardSwipeMs = 560
    const val GradientDriftMs = 10_000
    const val BreathInMs = 4_000
    const val BreathOutMs = 6_000
    const val TapMs = 120
    const val TapScale = 0.98f

    val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
}
