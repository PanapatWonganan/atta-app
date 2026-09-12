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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.atta.app.data.WidgetTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * The living layer: three soft pools of light drifting over the theme
 * gradient on slow, breath-length loops. The answer to the category's
 * video backgrounds, in the house register — no files, no loops of
 * someone else's footage, just the gradient gently alive. Alphas stay
 * whisper-low so hand-broken lines keep their contrast.
 */
@Composable
fun LivingBackdrop(theme: WidgetTheme, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "living")
    // Prime-ish periods so the three pools never sync up.
    val t1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(17_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t1",
    )
    val t2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(23_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t2",
    )
    val t3 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(29_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t3",
    )

    val glow = if (theme.lightInk) Color.White else theme.ink // light pool
    val shade = theme.ink // grounding pool, always the theme's own ink

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val r = size.minDimension

        fun pool(t: Float, cx: Float, cy: Float, ax: Float, ay: Float, radius: Float, color: Color, alpha: Float) {
            val a = (t * 2 * Math.PI).toFloat()
            val center = Offset(
                w * cx + w * ax * sin(a),
                h * cy + h * ay * cos(a),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        // A warm champagne breath, a cool counterweight, and a soft shadow.
        pool(t1, 0.30f, 0.28f, 0.14f, 0.10f, r * 0.62f, Color(0xFFC2A57B), if (theme.lightInk) 0.10f else 0.07f)
        pool(t2, 0.74f, 0.62f, 0.12f, 0.14f, r * 0.55f, glow, if (theme.lightInk) 0.06f else 0.05f)
        pool(t3, 0.48f, 0.86f, 0.16f, 0.08f, r * 0.5f, shade, 0.045f)
    }
}
