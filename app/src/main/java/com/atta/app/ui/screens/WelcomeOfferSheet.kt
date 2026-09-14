package com.atta.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.billing.AttaBilling
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType

/**
 * The welcome-back offer: a real Play discount on the first year, shown to a
 * free user who declined the paywall and came back anyway. One quiet sheet,
 * at most once a day — never a countdown, never a shaking button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeOfferSheet(
    offer: AttaBilling.WelcomeOffer,
    onTake: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.canvas,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AttaDimens.Md)
                .padding(bottom = AttaDimens.Lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LineArtGem(Modifier.size(64.dp))
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Welcome back.\nHalf off your first year.",
                style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 38.sp),
                color = colors.ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AttaDimens.Md))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OfferRow("Every theme, voice, and scene opens")
                OfferRow("Your line, at your hours, every day")
                offer.perMonthApprox?.let { OfferRow("$it a month, billed once a year") }
            }
            Spacer(Modifier.height(AttaDimens.Md))
            Text(
                text = buildAnnotatedString {
                    offer.original?.let { original ->
                        withStyle(
                            AttaType.body.copy(fontSize = 15.sp).toSpanStyle()
                                .copy(textDecoration = TextDecoration.LineThrough),
                        ) { append(original) }
                        append("  ")
                    }
                    withStyle(
                        AttaType.title.copy(fontSize = 18.sp).toSpanStyle(),
                    ) { append("${offer.discounted} / year") }
                },
                color = colors.ink,
            )
            Spacer(Modifier.height(AttaDimens.Sm))
            PrimaryButton(text = "Take the welcome price", onClick = onTake)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Not now",
                style = AttaType.body.copy(fontSize = 14.sp),
                color = colors.inkAlpha(0.45f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "First year only" +
                    (offer.original?.let { " · then $it / year" } ?: "") +
                    " · cancel anytime",
                style = AttaType.caption.copy(fontSize = 10.5.sp),
                color = colors.inkAlpha(0.45f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OfferRow(text: String) {
    val colors = Atta.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(13.dp)) {
            val s = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            val p = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.55f)
                lineTo(size.width * 0.4f, size.height * 0.85f)
                lineTo(size.width * 0.9f, size.height * 0.15f)
            }
            drawPath(p, AttaPalette.ChampagneDeep, style = s)
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = text,
            style = AttaType.body.copy(fontSize = 14.sp, lineHeight = 22.sp),
            color = colors.inkAlpha(0.75f),
        )
    }
}

/** Single-stroke gem with two sparks — the brand's line, cut once. */
@Composable
private fun LineArtGem(modifier: Modifier = Modifier) {
    val colors = Atta.colors
    val ink = colors.inkAlpha(0.75f)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        // Gem: a slim hexagon with one facet line.
        val gem = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.70f, h * 0.34f)
            lineTo(w * 0.64f, h * 0.72f)
            lineTo(w * 0.50f, h * 0.86f)
            lineTo(w * 0.36f, h * 0.72f)
            lineTo(w * 0.30f, h * 0.34f)
            close()
            moveTo(w * 0.30f, h * 0.34f)
            lineTo(w * 0.70f, h * 0.34f)
        }
        drawPath(gem, ink, style = stroke)
        // Two champagne sparks.
        listOf(Offset(w * 0.14f, h * 0.22f) to 0.05f, Offset(w * 0.85f, h * 0.60f) to 0.04f)
            .forEach { (c, r) ->
                drawLine(AttaPalette.Champagne, Offset(c.x - w * r, c.y), Offset(c.x + w * r, c.y), stroke.width, StrokeCap.Round)
                drawLine(AttaPalette.Champagne, Offset(c.x, c.y - w * r), Offset(c.x, c.y + w * r), stroke.width, StrokeCap.Round)
            }
    }
}
