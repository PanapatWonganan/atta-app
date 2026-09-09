package com.atta.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.ui.components.Eyebrow
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaMotion
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType

/**
 * Pre-paywall persuasion pair, in the house register: no red badges, no
 * star emoji — the same two conversion beats (difference made visible,
 * then payment anxiety removed) spoken quietly.
 */

/** Bars grow on entry; the claim stays modest enough to be true. */
@Composable
fun ComparisonScreen(onContinue: () -> Unit) {
    val colors = Atta.colors
    var grown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { grown = true }
    val aloneHeight by animateFloatAsState(
        targetValue = if (grown) 0.3f else 0.06f,
        animationSpec = tween(AttaMotion.CardSwipeMs, easing = AttaMotion.EaseInOut),
        label = "alone",
    )
    val attaHeight by animateFloatAsState(
        targetValue = if (grown) 0.86f else 0.06f,
        animationSpec = tween(AttaMotion.CardSwipeMs, delayMillis = 160, easing = AttaMotion.EaseInOut),
        label = "atta",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Sm),
    ) {
        Spacer(Modifier.height(AttaDimens.Xl))
        Eyebrow(text = "Why it works", color = AttaPalette.ChampagneDeep)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "A habit with a set time\nis twice as likely to stick.",
            style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 38.sp),
            color = colors.ink,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Spacer(Modifier.height(AttaDimens.Md))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AttaDimens.RadiusCard))
                .background(colors.canvasAlt)
                .padding(horizontal = 28.dp, vertical = 26.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                ComparisonBar(
                    heightFraction = aloneHeight,
                    fill = colors.inkAlpha(0.16f),
                    value = "1×",
                    valueColor = colors.inkAlpha(0.55f),
                    label = "On your own",
                    modifier = Modifier.weight(1f),
                )
                ComparisonBar(
                    heightFraction = attaHeight,
                    fill = AttaPalette.Champagne,
                    value = "2×",
                    valueColor = colors.ink,
                    label = "With ATTA",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "People who tie a new habit to a fixed moment keep it about twice as often. ATTA does the tying — your line, at your hours, already on your home screen.",
                style = AttaType.caption.copy(fontSize = 12.sp, lineHeight = 20.sp),
                color = colors.inkAlpha(0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
private fun ComparisonBar(
    heightFraction: Float,
    fill: androidx.compose.ui.graphics.Color,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = Atta.colors
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = value,
            style = AttaType.label.copy(fontSize = 15.sp, letterSpacing = 0.5.sp),
            color = valueColor,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(fill),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            style = AttaType.caption.copy(fontSize = 11.sp),
            color = colors.inkAlpha(0.55f),
        )
    }
}

/** The reminder promise before any price talk removes the sign-up fear. */
@Composable
fun TrialPromiseScreen(onContinue: () -> Unit) {
    val colors = Atta.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        LineArtBell(Modifier.size(96.dp))
        Spacer(Modifier.height(AttaDimens.Lg))
        Text(
            text = "We'll remind you before\nyour free week ends.",
            style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 38.sp),
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "A quiet note on day 5. The trial ends on day 7,\nand nothing charges before you say so.",
            style = AttaType.body.copy(fontSize = 14.sp, lineHeight = 24.sp),
            color = colors.inkAlpha(0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1.2f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(AttaPalette.Champagne),
                contentAlignment = Alignment.Center,
            ) {
                CheckMark(color = colors.canvas, modifier = Modifier.size(9.dp))
            }
            Text(
                text = "No payment due now",
                style = AttaType.label.copy(fontSize = 14.sp, letterSpacing = 0.2.sp),
                color = colors.ink,
            )
        }
        Spacer(Modifier.height(AttaDimens.Sm))
        PrimaryButton(text = "Continue for free", onClick = onContinue)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "7 days free, then \$39.99 / year · cancel anytime",
            style = AttaType.caption,
            color = colors.inkAlpha(0.5f),
        )
    }
}

/** Single-stroke bell with a champagne dot — the brand's line, bent once. */
@Composable
private fun LineArtBell(modifier: Modifier = Modifier) {
    val colors = Atta.colors
    val ink = colors.inkAlpha(0.75f)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        // Bell body: shoulders slope from the crown to a flared mouth.
        val path = Path().apply {
            moveTo(w * 0.24f, h * 0.66f)
            cubicTo(w * 0.30f, h * 0.60f, w * 0.30f, h * 0.34f, w * 0.38f, h * 0.26f)
            cubicTo(w * 0.44f, h * 0.20f, w * 0.56f, h * 0.20f, w * 0.62f, h * 0.26f)
            cubicTo(w * 0.70f, h * 0.34f, w * 0.70f, h * 0.60f, w * 0.76f, h * 0.66f)
        }
        drawPath(path, ink, style = stroke)
        // Mouth line.
        drawLine(ink, Offset(w * 0.20f, h * 0.66f), Offset(w * 0.80f, h * 0.66f), stroke.width, StrokeCap.Round)
        // Clapper.
        drawArc(
            color = ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.44f, h * 0.66f),
            size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.12f),
            style = stroke,
        )
        // The one accent: a filled champagne dot where a red badge would shout.
        drawCircle(
            color = AttaPalette.Champagne,
            radius = w * 0.045f,
            center = Offset(w * 0.72f, h * 0.22f),
        )
    }
}

@Composable
private fun CheckMark(color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.55f)
            lineTo(w * 0.4f, h * 0.85f)
            lineTo(w * 0.9f, h * 0.15f)
        }
        drawPath(path, color, style = stroke)
    }
}
