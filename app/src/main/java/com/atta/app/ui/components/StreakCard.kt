package com.atta.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.AttaSettings
import com.atta.app.data.Streak
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The streak, made visible: the run count in a ring and this week's seven
 * days, met ones filled champagne. Quiet arithmetic of showing up — no
 * fire, no guilt; a missed day just starts the count again.
 */
@Composable
fun StreakCard(settings: AttaSettings, modifier: Modifier = Modifier) {
    val colors = Atta.colors
    val th = settings.language == "th"
    val today = LocalDate.now()
    val streak = Streak.count(settings.metDays, today)
    val monday = today.with(DayOfWeek.MONDAY)
    val initials = if (th) {
        listOf("จ.", "อ.", "พ.", "พฤ.", "ศ.", "ส.", "อา.")
    } else {
        listOf("M", "T", "W", "T", "F", "S", "S")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AttaDimens.RadiusCard))
            .background(colors.canvasAlt)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Eyebrow(
            text = if (th) "ความต่อเนื่องของคุณ" else "Your streak",
            color = colors.inkAlpha(0.45f),
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StreakRing(streak = streak)
            Spacer(Modifier.size(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                initials.forEachIndexed { i, initial ->
                    val day = monday.plusDays(i.toLong())
                    DayDot(
                        initial = initial,
                        met = day.toString() in settings.metDays,
                        isToday = day == today,
                        isFuture = day.isAfter(today),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = when {
                streak == 0 && th -> "แตะค้างที่ประโยควันนี้ เพื่อเริ่มนับ"
                streak == 0 -> "Hold today's line to begin"
                th -> "พบกันทุกเช้า ก็เท่านั้นเอง"
                else -> "Meet the line each morning — that's all it is"
            },
            style = AttaType.caption.copy(fontSize = 11.sp),
            color = colors.inkAlpha(0.5f),
        )
    }
}

/** The run count inside a thin champagne ring, one spark at its shoulder. */
@Composable
private fun StreakRing(streak: Int) {
    val colors = Atta.colors
    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(58.dp)) {
            val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = AttaPalette.Champagne,
                radius = size.minDimension / 2 - stroke.width,
                style = stroke,
            )
            // One spark, low right — the ring's quiet ornament.
            val c = Offset(size.width * 0.92f, size.height * 0.84f)
            val r = size.width * 0.055f
            drawLine(AttaPalette.Champagne, Offset(c.x - r, c.y), Offset(c.x + r, c.y), stroke.width, StrokeCap.Round)
            drawLine(AttaPalette.Champagne, Offset(c.x, c.y - r), Offset(c.x, c.y + r), stroke.width, StrokeCap.Round)
        }
        Text(
            text = "$streak",
            style = AttaType.displaySm.copy(fontSize = 24.sp),
            color = colors.ink,
        )
    }
}

@Composable
private fun DayDot(initial: String, met: Boolean, isToday: Boolean, isFuture: Boolean) {
    val colors = Atta.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = initial,
            style = AttaType.caption.copy(fontSize = 10.sp),
            color = colors.inkAlpha(if (isToday) 0.7f else 0.4f),
        )
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        met -> AttaPalette.Champagne
                        isFuture -> colors.inkAlpha(0.05f)
                        else -> colors.inkAlpha(0.12f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (met) {
                Canvas(Modifier.size(10.dp)) {
                    val s = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    val p = Path().apply {
                        moveTo(size.width * 0.1f, size.height * 0.55f)
                        lineTo(size.width * 0.4f, size.height * 0.85f)
                        lineTo(size.width * 0.9f, size.height * 0.15f)
                    }
                    drawPath(p, colors.canvas, style = s)
                }
            } else if (isToday) {
                Canvas(Modifier.size(20.dp)) {
                    drawCircle(
                        color = AttaPalette.Champagne,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
            }
        }
    }
}
