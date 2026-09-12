package com.atta.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.atta.app.data.WidgetTheme
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * The moving layer behind the line. Plain themes breathe with the light
 * pools; scene themes draw a slow, hand-tuned scene — the sea swelling,
 * rain falling, stars turning — instead of shipping anyone's video files.
 * Everything stays muted enough that the hand-broken lines keep the room.
 */
@Composable
fun ThemeAtmosphere(theme: WidgetTheme, modifier: Modifier = Modifier) {
    when (theme.sceneId) {
        "sea" -> SeaScene(theme, modifier)
        "rain" -> RainScene(theme, modifier)
        "stars" -> StarScene(theme, modifier)
        else -> LivingBackdrop(theme, modifier)
    }
}

/** One shared clock, 0..1 over a minute — scenes derive their own tempo. */
@Composable
private fun sceneClock(): Float {
    val transition = rememberInfiniteTransition(label = "scene")
    val t by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing), RepeatMode.Restart),
        label = "clock",
    )
    return t
}

private fun DrawScope.moonGlow(color: Color, cx: Float, cy: Float, radius: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = Offset(cx, cy),
            radius = radius,
        ),
        radius = radius,
        center = Offset(cx, cy),
    )
}

/** Four swells crossing at their own speeds under a low moon. */
@Composable
private fun SeaScene(theme: WidgetTheme, modifier: Modifier = Modifier) {
    val t = sceneClock()
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        moonGlow(Color.White, w * 0.74f, h * 0.18f, size.minDimension * 0.4f, 0.10f)
        // Back swells move slower and sit lighter; the front one is deepest.
        for (i in 0 until 4) {
            val baseline = h * (0.58f + i * 0.115f)
            val amplitude = h * (0.010f + i * 0.005f)
            val wavelength = w / (1.1f + i * 0.25f)
            val speed = if (i % 2 == 0) 1f + i * 0.4f else -(1f + i * 0.3f)
            val phase = (t * speed * 2 * PI).toFloat()
            val path = Path().apply {
                moveTo(0f, baseline)
                var x = 0f
                while (x <= w) {
                    val y = baseline +
                        amplitude * sin((x / wavelength) * 2 * PI.toFloat() + phase) +
                        amplitude * 0.4f * sin((x / (wavelength * 0.53f)) * 2 * PI.toFloat() - phase * 1.7f)
                    lineTo(x, y)
                    x += 20f
                }
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path, theme.ink.copy(alpha = 0.035f + i * 0.012f))
        }
    }
}

/** Two depths of thin rain, seeded once so the fall never stutters. */
@Composable
private fun RainScene(theme: WidgetTheme, modifier: Modifier = Modifier) {
    val t = sceneClock()
    val drops = remember {
        val rng = Random(42)
        List(44) {
            Triple(
                rng.nextFloat(), // x
                0.55f + rng.nextFloat() * 0.9f, // fall duration s (part)
                rng.nextFloat(), // phase
            )
        }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        moonGlow(Color.White, w * 0.30f, h * 0.16f, size.minDimension * 0.45f, 0.05f)
        drops.forEachIndexed { i, (x, speed, phase) ->
            val far = i % 2 == 0 // alternating depth
            val len = h * (if (far) 0.045f else 0.075f) * (0.8f + phase * 0.4f)
            // 60s clock -> each drop falls top to bottom in ~1.4-3.4s.
            val fall = ((t * 60f / (1.4f + speed * 2f) + phase) % 1f)
            val y = fall * (h + len) - len
            val drift = w * 0.008f * sin((t * 60f + phase * 7f).toDouble()).toFloat()
            drawLine(
                color = theme.ink.copy(alpha = if (far) 0.07f else 0.12f),
                start = Offset(w * x + drift, y),
                end = Offset(w * x + drift - w * 0.006f, y + len),
                strokeWidth = (if (far) 1.dp else 1.6.dp).toPx(),
            )
        }
    }
}

/** A slow field of stars, each on its own breath, under one still moon. */
@Composable
private fun StarScene(theme: WidgetTheme, modifier: Modifier = Modifier) {
    val t = sceneClock()
    val stars = remember {
        val rng = Random(7)
        List(72) {
            floatArrayOf(
                rng.nextFloat(), // x
                rng.nextFloat() * 0.78f, // y — keep the lower sky clear for the line
                0.6f + rng.nextFloat() * 1.1f, // radius dp
                rng.nextFloat(), // twinkle phase
                2.5f + rng.nextFloat() * 4f, // twinkle period s
            )
        }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        moonGlow(Color.White, w * 0.26f, h * 0.20f, size.minDimension * 0.34f, 0.10f)
        stars.forEach { s ->
            val breathe = 0.5f + 0.5f * sin(((t * 60f / s[4] + s[3]) * 2 * PI)).toFloat()
            drawCircle(
                color = theme.ink.copy(alpha = 0.12f + 0.30f * breathe),
                radius = s[2].dp.toPx(),
                center = Offset(w * s[0], h * s[1]),
            )
        }
    }
}
