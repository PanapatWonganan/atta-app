package com.atta.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.ContextCompat
import com.atta.app.ads.AttaAds
import com.atta.app.ads.NativeAdCard
import com.atta.app.audio.PracticeQueue
import com.atta.app.audio.PracticeService
import com.atta.app.data.Affirmation
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.WidgetTheme
import com.atta.app.data.WidgetThemes
import com.atta.app.notify.DailyLineScheduler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.atta.app.share.ShareCard
import com.atta.app.ui.components.BookmarkIcon
import com.atta.app.ui.components.ChevronDownIcon
import com.atta.app.ui.components.ChevronUpIcon
import com.atta.app.ui.components.DotsIcon
import com.atta.app.ui.components.PlayIcon
import com.atta.app.ui.components.ShareIcon
import com.atta.app.ui.components.ThemeDot
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaMotion
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import com.atta.app.widget.AttaWidgetUpdater
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** One sponsored card after every this-many lines, free tier only. */
private const val AdEvery = 7

/**
 * Home is the feed: full-bleed theme, one card per swipe, no tab bar. The
 * top-right dots open a sheet with Widgets, Focus, Saved, Settings.
 */
@Composable
fun HomeScreen(
    prefs: AttaPrefs,
    settings: AttaSettings,
    onOpen: (route: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val eveningNow = remember { LocalTime.now().hour >= 18 }
    val feed = remember(settings.focusIds) {
        AffirmationRepository.feed(today, 30, settings.focusIds, eveningNow)
    }
    val theme = WidgetThemes.byId(
        if (settings.freeTier) WidgetThemes.FreeThemeId else settings.themeId,
    )
    // The free feed carries one sponsored card after every AdEvery lines;
    // these mappings convert pager pages <-> feed indexes across the ad pages
    // so the practice voice and the cards never drift apart.
    val nativeAd by AttaAds.nativeAd.collectAsState()
    val showAds = settings.freeTier && nativeAd != null
    val pageCount = if (showAds) feed.size + feed.size / AdEvery else feed.size
    fun isAdPage(page: Int) = showAds && (page + 1) % (AdEvery + 1) == 0
    fun feedIndexOf(page: Int) = if (showAds) page - (page + 1) / (AdEvery + 1) else page
    fun displayIndexOf(feedIndex: Int) = if (showAds) feedIndex + feedIndex / AdEvery else feedIndex
    val pagerState = rememberPagerState { pageCount }
    var showMenu by remember { mutableStateOf(false) }
    var showThemes by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(settings.morningHour, settings.morningMinute, settings.eveningLine) {
        DailyLineScheduler.schedule(context)
    }

    // While practice reads the feed aloud, the screen and the voice stay on
    // the same card: the pager follows the line being read, and a manual
    // swipe jumps the reading to the visible card. (index == page is the
    // no-op guard that keeps the two effects from chasing each other.)
    val playback by PracticeService.state.collectAsState()
    LaunchedEffect(playback.index, playback.active, playback.playing, showAds) {
        if (playback.active && playback.playing &&
            playback.source == PracticeQueue.SourceFeed &&
            playback.index in feed.indices &&
            pagerState.currentPage != displayIndexOf(playback.index)
        ) {
            pagerState.animateScrollToPage(displayIndexOf(playback.index))
        }
    }
    LaunchedEffect(pagerState, showAds) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (isAdPage(page)) return@collect
            val reading = PracticeService.state.value
            if (reading.active && reading.playing &&
                reading.source == PracticeQueue.SourceFeed &&
                reading.index != feedIndexOf(page)
            ) {
                PracticeService.start(context, feedIndexOf(page), PracticeQueue.SourceFeed)
            }
        }
    }

    VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val ad = nativeAd
        if (isAdPage(page) && ad != null) {
            NativeAdCard(ad = ad, modifier = Modifier.fillMaxSize())
        } else {
            val feedIndex = feedIndexOf(page).coerceIn(0, feed.lastIndex)
            val (date, affirmation) = feed[feedIndex]
            val line = affirmation.text(settings.language)
            val isNight = (feedIndex == 0 && eveningNow) || affirmation.daypart.name == "NIGHT"
            val eyebrow = (if (isNight) "Night" else "Morning") +
                " · " + AffirmationRepository.shortDate(date, settings.language)
            HomeCard(
                theme = theme,
                line = line,
                eyebrow = eyebrow,
                saved = affirmation.id in settings.savedIds,
                onToggleSave = {
                    scope.launch {
                        prefs.toggleSaved(affirmation.id)
                        runCatching { AttaWidgetUpdater.updateAll(context) }
                    }
                },
                onShare = { shareLine(context, theme, line) },
                onOpenThemes = { showThemes = true },
                onOpenMenu = { showMenu = true },
                showChevron = page < pageCount - 1,
                onOpenPractice = { onOpen("practice/feed/$feedIndex") },
            )
        }
    }

    if (showMenu) {
        NavSheet(
            onDismiss = { showMenu = false },
            onOpen = {
                showMenu = false
                onOpen(it)
            },
        )
    }
    if (showThemes) {
        ThemeSheet(
            selectedId = theme.id,
            freeTier = settings.freeTier,
            onDismiss = { showThemes = false },
            onPick = { picked ->
                showThemes = false
                scope.launch {
                    prefs.setThemeId(picked)
                    runCatching { AttaWidgetUpdater.updateAll(context) }
                }
            },
            onRequireUpgrade = {
                showThemes = false
                onOpen("paywall/upgrade")
            },
        )
    }
}

private fun shareLine(context: android.content.Context, theme: WidgetTheme, line: String) {
    ShareCard.share(context, theme, line)
}

/** One full-bleed affirmation card. Shared by the feed and the saved-line viewer. */
@Composable
fun HomeCard(
    theme: WidgetTheme,
    line: String,
    eyebrow: String,
    saved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onOpenThemes: (() -> Unit)?,
    onOpenMenu: (() -> Unit)?,
    showChevron: Boolean,
    onClose: (() -> Unit)? = null,
    onOpenPractice: (() -> Unit)? = null,
) {
    val transition = rememberInfiniteTransition(label = "drift")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AttaMotion.GradientDriftMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftValue",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val (start, end) = theme.gradientPoints(size.width, size.height)
                val dir = end - start
                val len = dir.getDistance().coerceAtLeast(1f)
                val shift = (drift - 0.5f) * size.minDimension * 0.06f
                val offset = androidx.compose.ui.geometry.Offset(
                    dir.x / len * shift,
                    dir.y / len * shift,
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = theme.stops.toTypedArray(),
                        start = start + offset,
                        end = end + offset,
                    ),
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AttaDimens.Md),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (onClose != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .semantics { contentDescription = "Close" }
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        ChevronDownIcon(
                            color = theme.ink.copy(alpha = 0.55f),
                            modifier = Modifier.size(width = 14.dp, height = 8.dp),
                        )
                    }
                }
                Text(
                    text = eyebrow.uppercase(),
                    style = AttaType.eyebrow.copy(fontSize = 10.sp, letterSpacing = 1.8.sp),
                    color = theme.eyebrowColor(),
                )
                if (onOpenMenu != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .semantics { contentDescription = "Menu" }
                            .clickable(onClick = onOpenMenu),
                        contentAlignment = Alignment.Center,
                    ) {
                        DotsIcon(color = theme.ink.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                } else {
                    Spacer(Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = line,
                style = AttaType.displaySm,
                color = theme.ink,
                modifier = Modifier.widthIn(max = 320.dp),
            )
            Spacer(Modifier.height(22.dp))
            Box(
                Modifier
                    .width(26.dp)
                    .height(1.dp)
                    .background(if (saved) AttaPalette.Champagne else theme.ink.copy(alpha = 0.3f)),
            )
            Spacer(Modifier.weight(1.2f))
            if (onOpenPractice != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, theme.ink.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                        .clickable(onClick = onOpenPractice)
                        .padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlayIcon(color = theme.ink.copy(alpha = 0.8f), modifier = Modifier.size(11.dp))
                    Text(
                        text = "Practice",
                        style = AttaType.label.copy(fontSize = 11.sp, letterSpacing = 0.3.sp),
                        color = theme.ink.copy(alpha = 0.8f),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCircle(
                        theme = theme,
                        description = if (saved) "Remove from saved" else "Save this line",
                        onClick = onToggleSave,
                    ) {
                        BookmarkIcon(
                            color = if (saved) AttaPalette.Champagne else theme.ink,
                            filled = saved,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    ActionCircle(theme = theme, description = "Share", onClick = onShare) {
                        ShareIcon(color = theme.ink, modifier = Modifier.size(17.dp))
                    }
                }
                if (onOpenThemes != null) {
                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, theme.ink.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                            .clickable(onClick = onOpenThemes)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        ThemeDot(theme = theme, size = 11.dp)
                        Text(
                            text = theme.displayName,
                            style = AttaType.label.copy(fontSize = 11.sp, letterSpacing = 0.3.sp),
                            color = theme.ink.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (showChevron) {
                    ChevronUpIcon(
                        color = theme.ink.copy(alpha = 0.35f),
                        modifier = Modifier.size(width = 14.dp, height = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCircle(
    theme: WidgetTheme,
    description: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(1.dp, theme.ink.copy(alpha = 0.22f), CircleShape)
            .semantics { contentDescription = description }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * The nav sheet: four quiet serif rows. A five-item chrome under a meditation
 * card would say "app" when the product wants to say "object".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavSheet(onDismiss: () -> Unit, onOpen: (route: String) -> Unit) {
    val colors = Atta.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.canvas,
    ) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            listOf(
                "Widgets" to "gallery",
                "Focus" to "focus",
                "Saved" to "saved",
                "Settings" to "settings",
            ).forEachIndexed { i, (label, route) ->
                if (i > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.inkAlpha(0.07f)),
                    )
                }
                Text(
                    text = label,
                    style = AttaType.displaySm.copy(fontSize = 22.sp, lineHeight = 34.sp),
                    color = colors.ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(route) }
                        .padding(vertical = 16.dp),
                )
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}

/** Theme picker. On the free tier only Linen stays unlocked — quietly. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSheet(
    selectedId: String,
    freeTier: Boolean,
    onDismiss: () -> Unit,
    onPick: (themeId: String) -> Unit,
    onRequireUpgrade: () -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.canvas,
    ) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            Text(
                text = "Theme",
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            WidgetThemes.All.forEach { theme ->
                val locked = freeTier && theme.id != WidgetThemes.FreeThemeId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable {
                            if (locked) onRequireUpgrade() else onPick(theme.id)
                        }
                        .padding(horizontal = 4.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ThemeDot(theme = theme, size = 22.dp)
                        Text(
                            text = theme.displayName,
                            style = AttaType.body.copy(fontSize = 15.sp),
                            color = colors.ink,
                        )
                    }
                    when {
                        theme.id == selectedId -> Text(
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

/** A saved line reopened full-screen in its theme. */
@Composable
fun ViewerScreen(
    affirmation: Affirmation,
    settings: AttaSettings,
    prefs: AttaPrefs,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme = WidgetThemes.byId(
        if (settings.freeTier) WidgetThemes.FreeThemeId else settings.themeId,
    )
    val line = affirmation.text(settings.language)
    HomeCard(
        theme = theme,
        line = line,
        eyebrow = com.atta.app.data.Categories.name(affirmation.categoryId, settings.language),
        saved = affirmation.id in settings.savedIds,
        onToggleSave = { scope.launch { prefs.toggleSaved(affirmation.id) } },
        onShare = { shareLine(context, theme, line) },
        onOpenThemes = null,
        onOpenMenu = null,
        showChevron = false,
        onClose = onBack,
    )
}
