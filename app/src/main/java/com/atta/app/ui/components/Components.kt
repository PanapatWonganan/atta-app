package com.atta.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.WidgetTheme
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaMotion
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType

/** The one primary action per screen: ink slab, 14dp radius, 120ms 0.98 tap feedback. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Atta.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) AttaMotion.TapScale else 1f,
        animationSpec = tween(AttaMotion.TapMs, easing = AttaMotion.EaseInOut),
        label = "tap",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.4f
            }
            .clip(RoundedCornerShape(AttaDimens.RadiusButton))
            .background(colors.ink)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AttaType.label.copy(fontSize = 15.sp, letterSpacing = 0.4.sp),
            color = colors.onInk,
        )
    }
}

/** Single-select answer / category card. Selection is a hairline, not a fill. */
@Composable
fun OptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Atta.colors
    val shape = RoundedCornerShape(AttaDimens.RadiusButton)
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(if (selected) colors.card else colors.canvasAlt)
    val bordered = if (selected) base.border(1.dp, AttaPalette.Champagne, shape) else base
    Row(
        modifier = bordered
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = AttaType.body.copy(fontSize = 15.sp),
            color = if (selected) colors.ink else colors.inkAlpha(0.8f),
            modifier = Modifier.weight(1f, fill = false),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(AttaPalette.Champagne),
                contentAlignment = Alignment.Center,
            ) {
                CheckIcon(color = colors.canvas, modifier = Modifier.size(10.dp))
            }
        }
    }
}

/**
 * The breathing ring — 4s in, 6s out on a 10s loop. Onboarding's processing
 * state and the app's only loading indicator: never a spinner, never a shimmer.
 */
@Composable
fun BreathingRing(modifier: Modifier = Modifier.size(120.dp)) {
    val transition = rememberInfiniteTransition(label = "breathe")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = AttaMotion.BreathInMs + AttaMotion.BreathOutMs
                1f at 0 using AttaMotion.EaseInOut
                1.12f at AttaMotion.BreathInMs using AttaMotion.EaseInOut
                1f at AttaMotion.BreathInMs + AttaMotion.BreathOutMs
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "scale",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = AttaMotion.BreathInMs + AttaMotion.BreathOutMs
                0.55f at 0 using AttaMotion.EaseInOut
                0.85f at AttaMotion.BreathInMs using AttaMotion.EaseInOut
                0.55f at AttaMotion.BreathInMs + AttaMotion.BreathOutMs
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "alpha",
    )
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val center = this.center
        drawCircle(
            color = AttaPalette.Champagne.copy(alpha = ringAlpha),
            radius = (r - 0.5f) * scale,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
        drawCircle(
            color = AttaPalette.Champagne.copy(alpha = 0.4f),
            radius = r * (1f - 18f / 60f),
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
        drawCircle(
            color = AttaPalette.Champagne,
            radius = 4.dp.toPx(),
            center = center,
        )
    }
}

/** Champagne pill toggle — the app's only toggle style. */
@Composable
fun AttaToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = Atta.colors
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(AttaMotion.TapMs * 2, easing = AttaMotion.EaseInOut),
        label = "toggle",
    )
    Box(
        modifier = modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (checked) AttaPalette.Champagne else colors.inkAlpha(0.18f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) }
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { translationX = thumbOffset * 18.dp.toPx() }
                .clip(CircleShape)
                .background(colors.canvas),
        )
    }
}

/** Gradient swatch dot for theme pickers and chips. */
@Composable
fun ThemeDot(theme: WidgetTheme, size: Dp, modifier: Modifier = Modifier) {
    val colors = Atta.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .drawBehind { drawRect(brush = theme.brush(this.size.width, this.size.height)) }
            .border(1.dp, colors.inkAlpha(0.2f), CircleShape),
    )
}

/** Rounded gradient surface carrying a theme, used by cards and previews. */
fun Modifier.themeSurface(theme: WidgetTheme, radius: Dp): Modifier =
    clip(RoundedCornerShape(radius))
        .drawBehind { drawRect(brush = theme.brush(size.width, size.height)) }
        .then(
            if (theme.hairline) {
                Modifier.border(1.dp, Color(0xFFF2EDE6).copy(alpha = 0.07f), RoundedCornerShape(radius))
            } else {
                Modifier
            },
        )

/**
 * Widget preview: eyebrow at top, hand-broken affirmation bottom-left, date stamp.
 * Mirrors the bitmap layout the real widget renders.
 */
@Composable
fun WidgetPreviewCard(
    theme: WidgetTheme,
    line: String,
    dateLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .themeSurface(theme, 16.dp)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = theme.displayName.uppercase(),
                style = AttaType.eyebrow.copy(fontSize = 9.sp, letterSpacing = 1.8.sp),
                color = theme.eyebrowColor(),
            )
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(1.dp)
                    .background(theme.ruleColor()),
            )
        }
        Text(
            text = line,
            style = AttaType.displaySm.copy(fontSize = 15.sp, lineHeight = 24.sp),
            color = theme.ink,
            modifier = Modifier.padding(vertical = 10.dp),
        )
        Text(
            text = dateLabel,
            style = AttaType.caption.copy(fontSize = 9.sp),
            color = theme.dateColor(),
        )
    }
}

/** Section eyebrow used across screens. */
@Composable
fun Eyebrow(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AttaType.eyebrow,
        color = color,
        modifier = modifier,
        textAlign = TextAlign.Start,
    )
}
