package com.atta.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.AttaSettings
import com.atta.app.data.Streak
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaMotion
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.delay

/**
 * The streak's small applause: when today's line gets met — held, practiced,
 * or checked in — a pill slides down from the top with the week's dots and
 * the count, holds a breath, and leaves on its own. It never appears on a
 * plain app open; the reward belongs to the act, not the arrival.
 */
@Composable
fun StreakToastHost(settings: AttaSettings, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val metNow = Streak.metToday(settings.metDays, today)
    // Seeded with the current value: opening the app already-met stays silent.
    var lastMet by remember { mutableStateOf(metNow) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(metNow) {
        if (metNow && !lastMet) {
            lastMet = true
            visible = true
            delay(2600)
            visible = false
        } else {
            lastMet = metNow
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) { -it } +
            fadeIn(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)),
        exit = slideOutVertically(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) { -it } +
            fadeOut(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)),
    ) {
        StreakPill(settings, today)
    }
}

@Composable
private fun StreakPill(settings: AttaSettings, today: LocalDate) {
    val colors = Atta.colors
    val th = settings.language == "th"
    val streak = Streak.count(settings.metDays, today)
    val monday = today.with(DayOfWeek.MONDAY)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AttaDimens.RadiusButton))
            .background(colors.canvas)
            .border(1.dp, colors.inkAlpha(0.14f), RoundedCornerShape(AttaDimens.RadiusButton))
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (0..6).forEach { i ->
            val day = monday.plusDays(i.toLong())
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (day.toString() in settings.metDays) {
                            AttaPalette.Champagne
                        } else {
                            colors.inkAlpha(0.14f)
                        },
                    ),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = when {
                streak <= 1 && th -> "เช้าแรกของคุณ"
                streak <= 1 -> "First morning met"
                th -> "$streak เช้าติดกัน"
                else -> "$streak mornings met"
            },
            style = AttaType.label.copy(fontSize = 12.5.sp, letterSpacing = 0.3.sp),
            color = colors.ink,
        )
    }
}
