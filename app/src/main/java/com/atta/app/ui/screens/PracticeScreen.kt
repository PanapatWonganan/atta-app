package com.atta.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.analytics.AttaAnalytics
import com.atta.app.audio.Moods
import com.atta.app.audio.PracticeQueue
import com.atta.app.audio.PracticeService
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.WidgetThemes
import com.atta.app.review.ReviewPrompter
import com.atta.app.ui.components.ChevronDownIcon
import com.atta.app.ui.components.ChevronUpIcon
import com.atta.app.ui.components.PauseIcon
import com.atta.app.ui.components.PlayIcon
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaMotion
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

/**
 * Practice: the feed read aloud, one line at a time with quiet gaps, over an
 * optional mood bed. The audio lives in [PracticeService], so it keeps going
 * when this screen closes or the phone locks — this screen is the remote.
 */
@Composable
fun PracticeScreen(
    prefs: AttaPrefs,
    settings: AttaSettings,
    source: String,
    startIndex: Int,
    onClose: () -> Unit,
    onRequireUpgrade: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val eveningNow = remember { LocalTime.now().hour >= 18 }
    val feed = remember(settings.focusIds, settings.savedIds, settings.customLines, source) {
        PracticeQueue.build(source, settings, today, eveningNow)
    }
    val theme = WidgetThemes.byId(
        if (settings.freeTier) WidgetThemes.FreeThemeId else settings.themeId,
    )
    val freeTier = settings.freeTier
    val mood = if (freeTier) Moods.None else Moods.byId(settings.practiceMood)

    val playback by PracticeService.state.collectAsState()
    var showMoods by remember { mutableStateOf(false) }
    var showCheckIn by remember { mutableStateOf(false) }
    val todayKey = remember { today.toString() }
    val loggedToday = settings.moodLog.any { it.substringBefore('|') == todayKey }
    val close = {
        if (loggedToday) onClose() else showCheckIn = true
    }

    if (feed.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    LaunchedEffect(Unit) { PracticeService.start(context, startIndex, source) }

    val index = playback.index.coerceIn(0, feed.lastIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(brush = theme.brush(size.width, size.height))
                drawRect(Color.Black.copy(alpha = 0.30f))
            }
            // Swipe up for the next line, down for the previous one — the
            // service jumps there and keeps reading.
            .pointerInput(feed.size, source) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onVerticalDrag = { _, amount -> total += amount },
                    onDragEnd = {
                        val threshold = 110.dp.toPx()
                        val current = PracticeService.state.value.index
                        if (total < -threshold) {
                            PracticeService.start(context, (current + 1) % feed.size, source)
                        } else if (total > threshold) {
                            PracticeService.start(
                                context,
                                (current - 1 + feed.size) % feed.size,
                                source,
                            )
                        }
                    },
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AttaDimens.Md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .semantics { contentDescription = "Close practice" }
                        .clickable(onClick = close),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    ChevronDownIcon(
                        color = theme.ink.copy(alpha = 0.55f),
                        modifier = Modifier.size(width = 14.dp, height = 8.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, theme.ink.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                        .clickable { showMoods = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        // The free tier hears silence — say what's behind the chip
                        // instead of a dead-looking "None".
                        text = if (freeTier) "Sound · Plus" else mood.displayName,
                        style = AttaType.label.copy(fontSize = 11.sp, letterSpacing = 0.3.sp),
                        color = theme.ink.copy(alpha = 0.75f),
                    )
                }
                // Sleep timer: taps cycle off → 5 → 10 → 15 minutes.
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .semantics {
                            contentDescription = if (playback.timerMinutes == 0) {
                                "Sleep timer, off"
                            } else {
                                "Sleep timer, ${playback.timerMinutes} minutes"
                            }
                        }
                        .clickable {
                            val next = when (playback.timerMinutes) {
                                0 -> 5
                                5 -> 10
                                10 -> 15
                                else -> 0
                            }
                            PracticeService.setTimer(context, next)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (playback.timerMinutes == 0) "∞" else "${playback.timerMinutes}′",
                        style = AttaType.label.copy(fontSize = 12.sp, letterSpacing = 0.sp),
                        color = if (playback.timerMinutes == 0) {
                            theme.ink.copy(alpha = 0.45f)
                        } else {
                            AttaPalette.Champagne
                        },
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Crossfade(
                targetState = index,
                animationSpec = tween(900),
                label = "practiceLine",
            ) { i ->
                Text(
                    text = feed[i].text(settings.language),
                    style = AttaType.displaySm,
                    color = theme.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(34.dp))
            BreathingRule(theme = theme, breathing = playback.playing)
            Spacer(Modifier.weight(1.2f))
            ChevronUpIcon(
                color = theme.ink.copy(alpha = 0.3f),
                modifier = Modifier.size(width = 14.dp, height = 7.dp),
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                listOf("off" to "VOICE OFF", "slow" to "SLOW", "normal" to "NORMAL").forEach { (id, label) ->
                    val selected = settings.practicePace == id ||
                        (id == "slow" && settings.practicePace !in setOf("off", "normal"))
                    Text(
                        text = label,
                        style = AttaType.eyebrow.copy(fontSize = 10.sp, letterSpacing = 1.8.sp),
                        color = if (selected) AttaPalette.Champagne else theme.ink.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { scope.launch { prefs.setPracticePace(id) } }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(1.dp, theme.ink.copy(alpha = 0.28f), CircleShape)
                    .semantics {
                        contentDescription = if (playback.playing) "Pause" else "Play"
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { PracticeService.toggle(context) },
                contentAlignment = Alignment.Center,
            ) {
                if (playback.playing) {
                    PauseIcon(color = theme.ink, modifier = Modifier.size(24.dp))
                } else {
                    PlayIcon(color = theme.ink, modifier = Modifier.size(24.dp))
                }
            }
            Box(
                modifier = Modifier.padding(top = 16.dp, bottom = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (playback.voiceUnavailable) {
                    Text(
                        text = "Voice unavailable on this device",
                        style = AttaType.caption,
                        color = theme.ink.copy(alpha = 0.45f),
                    )
                } else {
                    Spacer(Modifier.height(18.dp))
                }
            }
        }
    }

    if (showCheckIn) {
        CheckInSheet(
            moodLog = settings.moodLog,
            today = today,
            onDismiss = {
                showCheckIn = false
                onClose()
            },
            onPick = { value ->
                showCheckIn = false
                AttaAnalytics.log(context, AttaAnalytics.CheckIn, "mood", value)
                scope.launch {
                    prefs.logMood(todayKey, value)
                    prefs.recordMetDay(todayKey)
                    // They just told us they feel calm — the happiest moment
                    // this app has; ReviewPrompter rate-limits itself.
                    if (value == "calm") {
                        (context as? android.app.Activity)?.let {
                            ReviewPrompter.maybeAsk(it, prefs)
                        }
                    }
                }
                onClose()
            },
        )
    }
    if (showMoods) {
        MoodSheet(
            selectedId = mood.id,
            freeTier = freeTier,
            onDismiss = { showMoods = false },
            onPick = { picked ->
                showMoods = false
                scope.launch { prefs.setPracticeMood(picked) }
            },
            onRequireUpgrade = {
                showMoods = false
                onRequireUpgrade()
            },
        )
    }
}

/**
 * The breath is the only motion on screen: a hairline that widens for 4s in
 * and settles for 6s out (atta.motion.breath), matching a slow exhale-heavy
 * rhythm. Paused practice holds it still.
 */
@Composable
private fun BreathingRule(theme: com.atta.app.data.WidgetTheme, breathing: Boolean) {
    val cycleMs = AttaMotion.BreathInMs + AttaMotion.BreathOutMs
    val transition = rememberInfiniteTransition(label = "breath")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "breathPhase",
    )
    val inFraction = AttaMotion.BreathInMs.toFloat() / cycleMs
    val breath = if (!breathing) {
        0f
    } else if (phase < inFraction) {
        AttaMotion.EaseInOut.transform(phase / inFraction)
    } else {
        1f - AttaMotion.EaseInOut.transform((phase - inFraction) / (1f - inFraction))
    }
    Box(
        Modifier
            .height(1.dp)
            .size(width = (26 + 34 * breath).dp, height = 1.dp)
            .background(theme.ink.copy(alpha = 0.25f + 0.3f * breath)),
    )
}

/**
 * The quiet check-in shown once a day when leaving practice: one tap, three
 * words, and a fortnight of small dots. No streaks, no numbers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInSheet(
    moodLog: Set<String>,
    today: LocalDate,
    onDismiss: () -> Unit,
    onPick: (value: String) -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.canvas) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            Text(
                text = "How do you feel?",
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            listOf("calm" to "Calm", "okay" to "Okay", "heavy" to "Heavy").forEach { (key, label) ->
                Text(
                    text = label,
                    style = AttaType.displaySm.copy(fontSize = 19.sp, lineHeight = 30.sp),
                    color = colors.ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable { onPick(key) }
                        .padding(vertical = 13.dp, horizontal = 4.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                (13 downTo 0).forEach { back ->
                    val date = today.minusDays(back.toLong()).toString()
                    val value = moodLog.firstOrNull { it.substringBefore('|') == date }
                        ?.substringAfter('|')
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when (value) {
                                    "calm" -> AttaPalette.Champagne
                                    "okay" -> colors.inkAlpha(0.3f)
                                    "heavy" -> colors.inkAlpha(0.65f)
                                    else -> colors.inkAlpha(0.08f)
                                },
                            ),
                    )
                }
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}

/** Mood picker. On the free tier only silence stays unlocked — quietly. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodSheet(
    selectedId: String,
    freeTier: Boolean,
    onDismiss: () -> Unit,
    onPick: (moodId: String) -> Unit,
    onRequireUpgrade: () -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.canvas,
    ) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            Text(
                text = "Mood",
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Moods.All.forEach { mood ->
                val locked = freeTier && mood.id != Moods.None.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable {
                            if (locked) onRequireUpgrade() else onPick(mood.id)
                        }
                        .padding(horizontal = 4.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = mood.displayName,
                        style = AttaType.body.copy(fontSize = 15.sp),
                        color = colors.ink,
                    )
                    when {
                        mood.id == selectedId -> Text(
                            text = "IN USE",
                            style = AttaType.eyebrow.copy(fontSize = 9.sp, letterSpacing = 1.2.sp),
                            color = AttaPalette.ChampagneDeep,
                        )
                        locked -> Text(
                            text = "Unlock",
                            style = AttaType.caption,
                            color = AttaPalette.ChampagneDeep,
                        )
                    }
                }
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}
