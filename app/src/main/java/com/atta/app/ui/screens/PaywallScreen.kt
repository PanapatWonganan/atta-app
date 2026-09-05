package com.atta.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.Plans
import com.atta.app.ui.components.CheckIcon
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType

private data class PlanOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val mostChosen: Boolean = false,
)

private val PlanOptions = listOf(
    PlanOption(Plans.TrialWeekly, "Weekly", "7 days free, then \$2.99 / week"),
    PlanOption(Plans.TrialYearly, "Yearly", "7 days free, then \$39.99 / year · \$3.33 a month", mostChosen = true),
    PlanOption(Plans.Lifetime, "Lifetime", "\$79.99 once. Yours for good"),
)

/**
 * Closes without pressure: "Not now" is visible from second one, the trial
 * timeline makes the Day-5 reminder a stated feature, yearly is pre-selected
 * and framed per-month. No strikethroughs, no countdowns, no shaking buttons.
 */
@Composable
fun PaywallScreen(
    onNotNow: () -> Unit,
    onSubscribe: (planId: String) -> Unit,
    onRestore: () -> Unit = {},
) {
    val colors = Atta.colors
    var selected by remember { mutableStateOf(Plans.TrialYearly) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNotNow,
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Not now",
                    style = AttaType.body.copy(fontSize = 14.sp),
                    color = colors.inkAlpha(0.45f),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Keep tomorrow's line coming.",
                style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 36.sp),
                color = colors.ink,
                modifier = Modifier.widthIn(max = 300.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Every theme, every category, and the widget on your home screen.",
                style = AttaType.body.copy(fontSize = 14.sp, lineHeight = 23.sp),
                color = colors.inkAlpha(0.6f),
                modifier = Modifier.widthIn(max = 300.dp),
            )
            Spacer(Modifier.height(22.dp))
            TimelineRow("Today", "everything unlocks")
            Spacer(Modifier.height(10.dp))
            TimelineRow("Day 5", "we remind you the trial is ending")
            Spacer(Modifier.height(10.dp))
            TimelineRow("Day 7", "trial ends. Nothing charges before this")
            Spacer(Modifier.height(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanOptions.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        selected = selected == plan.id,
                        onClick = { selected = plan.id },
                    )
                }
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
        PrimaryButton(
            text = if (selected == Plans.Lifetime) "Unlock lifetime" else "Start my 7 days free",
            onClick = { onSubscribe(selected) },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Cancel anytime in two taps · Restore purchase",
            style = AttaType.caption.copy(fontSize = 10.5.sp),
            color = colors.inkAlpha(0.45f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRestore,
                )
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun TimelineRow(lead: String, rest: String) {
    val colors = Atta.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(12.dp)
                .height(1.dp)
                .background(AttaPalette.Champagne),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(AttaType.body.copy(fontWeight = FontWeight.Medium).toSpanStyle()) {
                    append(lead)
                }
                append(" — $rest")
            },
            style = AttaType.body.copy(fontSize = 13.5.sp, lineHeight = 21.sp),
            color = colors.inkAlpha(0.75f),
        )
    }
}

@Composable
private fun PlanCard(plan: PlanOption, selected: Boolean, onClick: () -> Unit) {
    val colors = Atta.colors
    val shape = RoundedCornerShape(AttaDimens.RadiusButton)
    Box(Modifier.padding(top = if (plan.mostChosen) 7.dp else 0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) colors.card else colors.canvas)
                .border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) AttaPalette.Champagne else colors.inkAlpha(0.14f),
                    shape = shape,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = plan.title,
                    style = AttaType.label.copy(fontSize = 14.sp, letterSpacing = 0.sp),
                    color = colors.ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = plan.subtitle,
                    style = AttaType.caption.copy(fontSize = 11.5.sp),
                    color = colors.inkAlpha(0.5f),
                )
            }
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
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(1.dp, colors.inkAlpha(0.25f), CircleShape),
                )
            }
        }
        if (plan.mostChosen) {
            Box(
                modifier = Modifier
                    .offset(x = 14.dp, y = (-7).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AttaPalette.Champagne)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "MOST CHOSEN",
                    style = AttaType.eyebrow.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                    color = colors.canvas,
                )
            }
        }
    }
}
