package com.atta.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.ads.AttaAds
import com.atta.app.analytics.AttaAnalytics
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.WidgetTheme
import com.atta.app.data.WidgetThemes
import com.atta.app.ui.components.ChevronUpIcon
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.components.themeSurface
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import com.atta.app.wallpaper.AttaWallpaper
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

/**
 * Wallpapers: every theme carrying today's line, set straight onto the
 * phone. Two are free; a rewarded ad buys one for keeps; premium opens
 * them all — the third quiet lane to the subscription.
 */
@Composable
fun WallpapersScreen(
    prefs: AttaPrefs,
    settings: AttaSettings,
    onOpenPaywall: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = Atta.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val evening = remember { LocalTime.now().hour >= 18 }
    val line = remember {
        AffirmationRepository.lineFor(today, settings.focusIds, evening)
            .text(settings.language)
    }
    val adReady by AttaAds.rewardedReady.collectAsState()
    var pending by remember { mutableStateOf<WidgetTheme?>(null) } // gated pick
    var setting by remember { mutableStateOf<WidgetTheme?>(null) } // allowed pick
    val th = settings.language == "th"

    LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.WallpaperView) }

    fun canUse(theme: WidgetTheme): Boolean =
        !settings.freeTier ||
            theme.id in AttaWallpaper.FreeIds ||
            theme.id in settings.unlockedWallpapers

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AttaDimens.Md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .semantics { contentDescription = "Back" }
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart,
            ) {
                ChevronUpIcon(
                    color = colors.inkAlpha(0.55f),
                    modifier = Modifier.size(width = 14.dp, height = 8.dp),
                )
            }
            Text(
                text = if (th) "วอลเปเปอร์" else "Wallpapers",
                style = AttaType.displaySm.copy(fontSize = 24.sp),
                color = colors.ink,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (th) {
                "ประโยคของวันนี้ บนหน้าจอเครื่องของคุณเลย"
            } else {
                "Today's line, on the phone itself. Set it again any day."
            },
            style = AttaType.body.copy(fontSize = 13.5.sp, lineHeight = 22.sp),
            color = colors.inkAlpha(0.55f),
        )
        Spacer(Modifier.height(AttaDimens.Sm))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(WidgetThemes.All, key = { it.id }) { theme ->
                WallpaperCard(
                    theme = theme,
                    line = line,
                    freeChip = settings.freeTier && theme.id in AttaWallpaper.FreeIds,
                    unlockedChip = settings.freeTier && theme.id in settings.unlockedWallpapers,
                    onClick = {
                        if (canUse(theme)) setting = theme else pending = theme
                    },
                )
            }
        }
    }

    setting?.let { theme ->
        SetWallpaperSheet(
            th = th,
            onDismiss = { setting = null },
            onPick = { which ->
                AttaAnalytics.log(context, AttaAnalytics.WallpaperSet, "theme", theme.id)
                AttaWallpaper.set(context, theme, line, which)
                setting = null
            },
        )
    }

    pending?.let { theme ->
        UnlockWallpaperSheet(
            th = th,
            adReady = adReady,
            onDismiss = { pending = null },
            onPremium = {
                pending = null
                onOpenPaywall()
            },
            onWatchAd = {
                (context as? Activity)?.let { activity ->
                    AttaAds.showRewarded(activity) {
                        scope.launch { prefs.addUnlockedWallpaper(theme.id) }
                        pending = null
                        setting = theme
                    }
                }
            },
        )
    }
}

@Composable
private fun WallpaperCard(
    theme: WidgetTheme,
    line: String,
    freeChip: Boolean,
    unlockedChip: Boolean,
    onClick: () -> Unit,
) {
    val colors = Atta.colors
    Box(
        Modifier
            .aspectRatio(9f / 16f)
            .themeSurface(theme, AttaDimens.RadiusCard)
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = line,
                        style = AttaType.displaySm.copy(fontSize = 13.sp, lineHeight = 21.sp),
                        color = theme.ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .size(width = 18.dp, height = 1.dp)
                            .background(theme.ruleColor()),
                    )
                }
                if (freeChip || unlockedChip) {
                    Text(
                        text = if (freeChip) "FREE" else "YOURS",
                        style = AttaType.eyebrow.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                        color = colors.canvas,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AttaPalette.Champagne)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetWallpaperSheet(
    th: Boolean,
    onDismiss: () -> Unit,
    onPick: (which: Int) -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.canvas) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            Text(
                text = if (th) "ตั้งเป็นวอลเปเปอร์" else "Set as wallpaper",
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            listOf(
                (if (th) "หน้าจอล็อก" else "Lock screen") to AttaWallpaper.Lock,
                (if (th) "หน้าโฮม" else "Home screen") to AttaWallpaper.Home,
                (if (th) "ทั้งสองหน้าจอ" else "Both") to AttaWallpaper.Both,
            ).forEach { (label, which) ->
                Text(
                    text = label,
                    style = AttaType.body.copy(fontSize = 15.sp),
                    color = colors.ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable { onPick(which) }
                        .padding(vertical = 14.dp),
                )
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnlockWallpaperSheet(
    th: Boolean,
    adReady: Boolean,
    onDismiss: () -> Unit,
    onPremium: () -> Unit,
    onWatchAd: () -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.canvas) {
        Column(
            modifier = Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (th) "ปลดล็อกวอลเปเปอร์นี้" else "Unlock this wallpaper",
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (th) {
                    "สมาชิกใช้ได้ทุกแบบ หรือดูโฆษณาสั้น ๆ เพื่อเก็บแบบนี้ไว้เลย"
                } else {
                    "Premium opens them all — or watch a short ad to keep this one."
                },
                style = AttaType.body.copy(fontSize = 13.5.sp, lineHeight = 22.sp),
                color = colors.inkAlpha(0.6f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AttaDimens.Md))
            PrimaryButton(
                text = if (th) "สมัครสมาชิก" else "Go Premium",
                onClick = onPremium,
            )
            if (adReady) {
                Text(
                    text = if (th) "ดูโฆษณาเพื่อปลดล็อก" else "Unlock by watching an ad",
                    style = AttaType.label.copy(fontSize = 13.sp, letterSpacing = 0.3.sp),
                    color = AttaPalette.ChampagneDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable(onClick = onWatchAd)
                        .padding(vertical = 12.dp),
                )
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}
