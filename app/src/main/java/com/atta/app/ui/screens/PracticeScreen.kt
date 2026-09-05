package com.atta.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.audio.Moods
import com.atta.app.audio.PracticeService
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.Plans
import com.atta.app.data.WidgetThemes
import com.atta.app.ui.components.ChevronDownIcon
import com.atta.app.ui.components.PauseIcon
import com.atta.app.ui.components.PlayIcon
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
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
    startIndex: Int,
    onClose: () -> Unit,
    onRequireUpgrade: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val eveningNow = remember { LocalTime.now().hour >= 18 }
    val feed = remember(settings.focusIds) {
        AffirmationRepository.feed(today, 30, settings.focusIds, eveningNow)
    }
    val theme = WidgetThemes.byId(
        if (Plans.isFree(settings.plan)) WidgetThemes.FreeThemeId else settings.themeId,
    )
    val freeTier = Plans.isFree(settings.plan)
    val mood = if (freeTier) Moods.None else Moods.byId(settings.practiceMood)
    val slow = settings.practicePace != "normal"

    val playback by PracticeService.state.collectAsState()
    var showMoods by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { PracticeService.start(context, startIndex) }

    val index = playback.index.coerceIn(0, feed.lastIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(brush = theme.brush(size.width, size.height))
                drawRect(Color.Black.copy(alpha = 0.30f))
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
                        .clickable(onClick = onClose),
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
                        text = mood.displayName,
                        style = AttaType.label.copy(fontSize = 11.sp, letterSpacing = 0.3.sp),
                        color = theme.ink.copy(alpha = 0.75f),
                    )
                }
                Spacer(Modifier.size(44.dp))
            }
            Spacer(Modifier.weight(1f))
            Crossfade(
                targetState = index,
                animationSpec = tween(900),
                label = "practiceLine",
            ) { i ->
                Text(
                    text = feed[i].second.text(settings.language),
                    style = AttaType.displaySm,
                    color = theme.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.weight(1.2f))
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                listOf("slow" to "SLOW", "normal" to "NORMAL").forEach { (id, label) ->
                    val selected = (id == "slow") == slow
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
