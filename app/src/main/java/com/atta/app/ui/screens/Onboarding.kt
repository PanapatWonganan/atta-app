package com.atta.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.Categories
import com.atta.app.ui.components.BreathingRing
import com.atta.app.ui.components.Eyebrow
import com.atta.app.ui.components.OptionCard
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import kotlinx.coroutines.delay

/** Welcome: no carousel, no skip button to manage. One promise, one action. */
@Composable
fun WelcomeScreen(onBegin: () -> Unit) {
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
        Box(
            Modifier
                .width(1.dp)
                .height(44.dp)
                .background(AttaPalette.Champagne),
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = "ATTA",
            style = AttaType.displaySm.copy(fontSize = 28.sp, letterSpacing = 8.sp),
            color = colors.ink,
            modifier = Modifier.padding(start = 8.dp), // recenter the trailing tracking
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text = "One quiet line, every morning, on your home screen.",
            style = AttaType.title.copy(fontFamily = AttaType.displaySm.fontFamily),
            color = colors.inkAlpha(0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        Spacer(Modifier.weight(1.3f))
        PrimaryButton(text = "Begin", onClick = onBegin)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Takes about a minute",
            style = AttaType.caption,
            color = colors.inkAlpha(0.5f),
        )
    }
}

private data class Question(val prompt: String, val options: List<String>)

private val OnboardingQuestions = listOf(
    Question(
        "What brings you here?",
        listOf("A calmer start to the day", "Kinder self-talk", "Firmer boundaries", "Better nights"),
    ),
    Question(
        "What do your mornings usually feel like?",
        listOf("Rushed before I'm awake", "Fine, but a little flat", "Heavy to get moving", "Quiet — I want to keep it"),
    ),
    Question(
        "Which voice lands best?",
        listOf("Gentle reminders", "Straight talk, kindly", "Short and spare", "Warm encouragement"),
    ),
    Question(
        "Where does your energy dip?",
        listOf("Early morning", "Midday", "Evenings", "Late night"),
    ),
    Question(
        "What should this week make room for?",
        listOf("Rest", "Focus", "Courage", "Gratitude"),
    ),
)

private fun deriveFocus(answers: List<Int>): Set<String> {
    val q1 = listOf("calm-mornings", "self-worth", "boundaries", "nights")
    val q2 = listOf("calm-mornings", "gratitude", "rest", "calm-mornings")
    val q5 = listOf("rest", "focus-work", "courage", "gratitude")
    val picks = linkedSetOf(
        q1[answers[0].coerceIn(0, 3)],
        q5[answers[4].coerceIn(0, 3)],
        q2[answers[1].coerceIn(0, 3)],
    )
    val fallback = listOf("self-worth", "healing", "love")
    var i = 0
    while (picks.size < Categories.MaxSelected && i < fallback.size) {
        picks += fallback[i]
        i++
    }
    return picks.take(Categories.MaxSelected).toSet()
}

private fun deriveTheme(answers: List<Int>): String =
    listOf("dawn", "sage", "dusk", "onyx")[answers[3].coerceIn(0, 3)]

/** Five single-select cards, hairline progress. Answers seed focus + first theme. */
@Composable
fun QuestionsScreen(onDone: (focusIds: Set<String>, themeId: String) -> Unit) {
    val colors = Atta.colors
    var index by remember { mutableIntStateOf(0) }
    val answers = remember { MutableList(OnboardingQuestions.size) { -1 }.toMutableStateList() }
    val question = OnboardingQuestions[index]

    BackHandler(enabled = index > 0) { index-- }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AttaDimens.Xs),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            OnboardingQuestions.indices.forEach { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (i <= index) AttaPalette.Champagne else colors.inkAlpha(0.12f)),
                )
            }
        }
        Spacer(Modifier.height(AttaDimens.Lg))
        Eyebrow(text = "${index + 1} of ${OnboardingQuestions.size}", color = colors.inkAlpha(0.45f))
        Spacer(Modifier.height(12.dp))
        Text(
            text = question.prompt,
            style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 38.sp),
            color = colors.ink,
            modifier = Modifier.widthIn(max = 300.dp),
        )
        Spacer(Modifier.height(AttaDimens.Md))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            question.options.forEachIndexed { i, option ->
                OptionCard(
                    text = option,
                    selected = answers[index] == i,
                    onClick = { answers[index] = i },
                )
            }
        }
        Spacer(Modifier.height(AttaDimens.Sm))
        PrimaryButton(
            text = "Continue",
            enabled = answers[index] >= 0,
            onClick = {
                if (index < OnboardingQuestions.lastIndex) {
                    index++
                } else {
                    onDone(deriveFocus(answers), deriveTheme(answers))
                }
            },
        )
    }
}

/** The breathing ring doubles as the app's loading state everywhere. */
@Composable
fun ProcessingScreen(onDone: () -> Unit) {
    val colors = Atta.colors
    LaunchedEffect(Unit) {
        delay(3600)
        onDone()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(AttaDimens.Md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BreathingRing(Modifier.size(120.dp))
        Spacer(Modifier.height(36.dp))
        Text(
            text = "Shaping your first week",
            style = AttaType.title.copy(fontFamily = AttaType.displaySm.fontFamily),
            color = colors.inkAlpha(0.85f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Choosing lines that match\nyour mornings",
            style = AttaType.caption.copy(fontSize = 13.sp, lineHeight = 22.sp),
            color = colors.inkAlpha(0.5f),
            textAlign = TextAlign.Center,
        )
    }
}

/** The one sage screen. The CTA promises the product, not the paywall. */
@Composable
fun ResultScreen(
    focusIds: Set<String>,
    lang: String,
    onContinue: () -> Unit,
) {
    val colors = Atta.colors
    val names = focusIds.mapNotNull { Categories.byId(it)?.name(lang) }
    val headline = names.joinToString(", ") { it.replaceFirstChar(Char::lowercase) }
        .replaceFirstChar(Char::uppercase) + "."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Sm),
    ) {
        Spacer(Modifier.height(AttaDimens.Xl))
        Eyebrow(text = "Your practice", color = AttaPalette.SageDeep)
        Spacer(Modifier.height(14.dp))
        Text(
            text = headline,
            style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 38.sp),
            color = colors.ink,
            modifier = Modifier.widthIn(max = 300.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Your first week draws from these three. You can change them anytime.",
            style = AttaType.body.copy(fontSize = 14.sp, lineHeight = 24.sp),
            color = colors.inkAlpha(0.6f),
            modifier = Modifier.widthIn(max = 300.dp),
        )
        Spacer(Modifier.height(AttaDimens.Md))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            names.forEach { name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusButton))
                        .background(colors.canvasAlt)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AttaPalette.Sage),
                    )
                    Text(
                        text = name,
                        style = AttaType.label.copy(fontSize = 14.sp, letterSpacing = 0.sp),
                        color = colors.ink,
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "See my first line", onClick = onContinue)
    }
}
