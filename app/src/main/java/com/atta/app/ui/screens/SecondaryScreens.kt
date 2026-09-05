package com.atta.app.ui.screens

import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.Affirmations
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.Categories
import com.atta.app.data.CustomLines
import com.atta.app.data.Plans
import com.atta.app.data.WidgetThemes
import com.atta.app.notify.DailyLineScheduler
import com.atta.app.ui.components.AttaToggle
import com.atta.app.ui.components.BookmarkIcon
import com.atta.app.ui.components.Eyebrow
import com.atta.app.ui.components.PlayIcon
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.components.ThemeDot
import com.atta.app.ui.components.WidgetPreviewCard
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaType
import com.atta.app.widget.AttaWidgetReceiver
import com.atta.app.widget.AttaWidgetUpdater
import kotlinx.coroutines.launch
import java.time.LocalDate

/** The selling page: live previews with today's real line, not sample art. */
@Composable
fun WidgetGalleryScreen(
    prefs: AttaPrefs,
    settings: AttaSettings,
    onOpenPaywall: () -> Unit,
) {
    val colors = Atta.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val line = AffirmationRepository.lineFor(today, settings.focusIds).text(settings.language)
    val date = AffirmationRepository.shortDate(today, settings.language)
    val freeTier = Plans.isFree(settings.plan)
    val activeThemeId = if (freeTier) WidgetThemes.FreeThemeId else settings.themeId

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = AttaDimens.Md, end = AttaDimens.Md, top = AttaDimens.Md, bottom = AttaDimens.Xl,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Widgets",
                    style = AttaType.displaySm.copy(fontSize = 24.sp),
                    color = colors.ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Pick a theme. It updates with your line each morning.",
                    style = AttaType.caption.copy(fontSize = 12.5.sp, lineHeight = 20.sp),
                    color = colors.inkAlpha(0.55f),
                )
                val widgetManager = AppWidgetManager.getInstance(context)
                if (!freeTier && widgetManager.isRequestPinAppWidgetSupported) {
                    Text(
                        text = "Add to home screen",
                        style = AttaType.label.copy(fontSize = 13.sp, letterSpacing = 0.3.sp),
                        color = AttaPalette.ChampagneDeep,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                            .clickable {
                                widgetManager.requestPinAppWidget(
                                    ComponentName(context, AttaWidgetReceiver::class.java),
                                    null,
                                    null,
                                )
                            }
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }
        items(WidgetThemes.All, key = { it.id }) { theme ->
            Column {
                WidgetPreviewCard(
                    theme = theme,
                    line = line,
                    dateLabel = date,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(330f / 140f),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = theme.displayName,
                        style = AttaType.label.copy(fontSize = 13.5.sp, letterSpacing = 0.sp),
                        color = colors.ink,
                    )
                    if (theme.id == activeThemeId) {
                        Text(
                            text = "IN USE",
                            style = AttaType.eyebrow.copy(fontSize = 9.5.sp, letterSpacing = 1.2.sp),
                            color = AttaPalette.ChampagneDeep,
                        )
                    } else {
                        Text(
                            text = "Use",
                            style = AttaType.body.copy(fontSize = 13.sp),
                            color = colors.inkAlpha(0.45f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                                .clickable {
                                    if (freeTier) {
                                        onOpenPaywall()
                                    } else {
                                        scope.launch {
                                            prefs.setThemeId(theme.id)
                                            runCatching { AttaWidgetUpdater.updateAll(context) }
                                        }
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
        if (freeTier) {
            item {
                // the quiet inline upgrade card — never a popup
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusCard))
                        .background(colors.canvasAlt)
                        .clickable(onClick = onOpenPaywall)
                        .padding(24.dp),
                ) {
                    Text(
                        text = "The widget is part of ATTA full.",
                        style = AttaType.body.copy(fontSize = 14.5.sp),
                        color = colors.inkAlpha(0.8f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Start 7 days free",
                        style = AttaType.label.copy(fontSize = 13.sp, letterSpacing = 0.3.sp),
                        color = AttaPalette.ChampagneDeep,
                    )
                }
            }
        }
    }
}

/** Ten categories, choose up to three. Selection is a hairline, not a fill. */
@Composable
fun FocusScreen(
    prefs: AttaPrefs,
    settings: AttaSettings,
    onDone: () -> Unit,
) {
    val colors = Atta.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(settings.focusIds) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Sm),
    ) {
        Spacer(Modifier.height(AttaDimens.Xs))
        Text(
            text = "Focus",
            style = AttaType.displaySm.copy(fontSize = 24.sp),
            color = colors.ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Choose up to three. Your mornings draw from these.",
            style = AttaType.caption.copy(fontSize = 12.5.sp, lineHeight = 20.sp),
            color = colors.inkAlpha(0.55f),
        )
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(Categories.All, key = { it.id }) { category ->
                val isSelected = category.id in selected
                val shape = RoundedCornerShape(AttaDimens.RadiusButton)
                val base = Modifier
                    .clip(shape)
                    .background(if (isSelected) colors.card else colors.canvasAlt)
                Column(
                    modifier = (if (isSelected) base.border(1.dp, AttaPalette.Champagne, shape) else base)
                        .clickable {
                            selected = when {
                                category.id in selected -> selected - category.id
                                selected.size < Categories.MaxSelected -> selected + category.id
                                else -> selected
                            }
                        }
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AttaPalette.Champagne else colors.inkAlpha(0.3f)),
                    )
                    Text(
                        text = category.name(settings.language),
                        style = AttaType.label.copy(fontSize = 12.5.sp, letterSpacing = 0.sp),
                        color = colors.ink,
                    )
                }
            }
        }
        Spacer(Modifier.height(AttaDimens.Sm))
        PrimaryButton(
            text = "Save · ${selected.size} chosen",
            enabled = selected.isNotEmpty(),
            onClick = {
                scope.launch {
                    prefs.setFocusIds(selected)
                    runCatching { AttaWidgetUpdater.updateAll(context) }
                    onDone()
                }
            },
        )
    }
}

/** Plain reading list; tap a line to reopen it full-screen in its theme. */
@Composable
fun SavedScreen(
    settings: AttaSettings,
    prefs: AttaPrefs,
    onOpenLine: (id: String) -> Unit,
    onPractice: () -> Unit,
    onRequireUpgrade: () -> Unit,
) {
    val colors = Atta.colors
    val scope = rememberCoroutineScope()
    val saved = Affirmations.All.filter { it.id in settings.savedIds }
    val custom = CustomLines.parse(settings.customLines)
    val freeTier = Plans.isFree(settings.plan)
    var showEditor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .padding(horizontal = AttaDimens.Md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AttaDimens.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Saved",
                style = AttaType.displaySm.copy(fontSize = 24.sp),
                color = colors.ink,
            )
            if (saved.isNotEmpty() || custom.isNotEmpty()) {
                // Listen to the whole list, your own lines first.
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable(onClick = onPractice)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PlayIcon(color = AttaPalette.ChampagneDeep, modifier = Modifier.size(10.dp))
                    Text(
                        text = "Listen",
                        style = AttaType.label.copy(fontSize = 12.sp, letterSpacing = 0.3.sp),
                        color = AttaPalette.ChampagneDeep,
                    )
                }
            }
        }
        Text(
            text = "+ Your own line",
            style = AttaType.label.copy(fontSize = 13.sp, letterSpacing = 0.3.sp),
            color = AttaPalette.ChampagneDeep,
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                .clickable { if (freeTier) onRequireUpgrade() else showEditor = true }
                .padding(vertical = 8.dp, horizontal = 2.dp),
        )
        if (custom.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                custom.forEach { line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AttaDimens.RadiusButton))
                            .background(colors.card)
                            .padding(start = 18.dp, end = 10.dp, top = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = line.text(settings.language).replace("\n", " "),
                            style = AttaType.body.copy(
                                fontFamily = AttaType.displaySm.fontFamily,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                            ),
                            color = colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "✕",
                            style = AttaType.caption,
                            color = colors.inkAlpha(0.35f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { scope.launch { prefs.removeCustomLine(line.id) } }
                                .padding(10.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        if (saved.isEmpty() && custom.isEmpty()) {
            // No illustration, no mascot: the outline bookmark and two quiet lines.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = AttaDimens.Xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BookmarkIcon(
                    color = colors.inkAlpha(0.3f),
                    filled = false,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Nothing kept yet.",
                    style = AttaType.body.copy(fontFamily = AttaType.displaySm.fontFamily),
                    color = colors.inkAlpha(0.7f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "When a line lands, save it here.\nThe good ones are worth rereading.",
                    style = AttaType.caption.copy(fontSize = 12.sp, lineHeight = 20.sp),
                    color = colors.inkAlpha(0.45f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 240.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 18.dp, bottom = AttaDimens.Xl,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(saved, key = { it.id }) { affirmation ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AttaDimens.RadiusButton))
                            .background(colors.card)
                            .clickable { onOpenLine(affirmation.id) }
                            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = affirmation.text(settings.language).replace("\n", " "),
                            style = AttaType.body.copy(
                                fontFamily = AttaType.displaySm.fontFamily,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                            ),
                            color = colors.ink,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Eyebrow(
                                text = Categories.name(affirmation.categoryId, settings.language),
                                color = colors.inkAlpha(0.4f),
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { scope.launch { prefs.toggleSaved(affirmation.id) } },
                                contentAlignment = Alignment.Center,
                            ) {
                                BookmarkIcon(
                                    color = AttaPalette.Champagne,
                                    filled = true,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        CustomLineEditor(
            onDismiss = { showEditor = false },
            onSave = { text ->
                showEditor = false
                scope.launch {
                    prefs.addCustomLine(CustomLines.encode(CustomLines.newId(), text))
                }
            },
        )
    }
}

/** One field, one button. Their words become part of the practice queue. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomLineEditor(
    onDismiss: () -> Unit,
    onSave: (text: String) -> Unit,
) {
    val colors = Atta.colors
    var text by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.canvas) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            Text(
                text = "Your own line",
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "In your words. Read back to you each practice.",
                style = AttaType.caption.copy(fontSize = 12.sp),
                color = colors.inkAlpha(0.5f),
            )
            Spacer(Modifier.height(16.dp))
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 140) text = it },
                textStyle = AttaType.displaySm.copy(fontSize = 19.sp, color = colors.ink),
                cursorBrush = SolidColor(AttaPalette.Champagne),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clip(RoundedCornerShape(AttaDimens.RadiusButton))
                    .background(colors.card)
                    .padding(18.dp),
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Keep",
                enabled = text.trim().isNotEmpty(),
                onClick = { onSave(text.trim()) },
            )
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}

/** Hairline rows, no icons, no cards. The one toggle is champagne. */
@Composable
fun SettingsScreen(
    prefs: AttaPrefs,
    settings: AttaSettings,
    onOpenFocus: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val colors = Atta.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showThemeSheet by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    val theme = WidgetThemes.byId(settings.themeId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(AttaDimens.Md))
            Text(
                text = "Settings",
                style = AttaType.displaySm.copy(fontSize = 24.sp),
                color = colors.ink,
            )
            Spacer(Modifier.height(10.dp))

            SectionLabel("Daily line")
            SettingsRow(
                label = "Arrives at",
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            scope.launch {
                                prefs.setMorningTime(hour, minute)
                                DailyLineScheduler.schedule(context)
                            }
                        },
                        settings.morningHour,
                        settings.morningMinute,
                        true,
                    ).show()
                },
            ) {
                Text(
                    text = "%d:%02d".format(settings.morningHour, settings.morningMinute),
                    style = AttaType.body.copy(fontSize = 14.sp),
                    color = AttaPalette.ChampagneDeep,
                )
            }
            SettingsRow(label = "Evening line") {
                AttaToggle(
                    checked = settings.eveningLine,
                    onCheckedChange = {
                        scope.launch {
                            prefs.setEveningLine(it)
                            DailyLineScheduler.schedule(context)
                        }
                    },
                )
            }
            SettingsRow(label = "Focus areas", onClick = onOpenFocus) {
                ValueText("${settings.focusIds.size} chosen")
            }

            SectionLabel("Appearance")
            SettingsRow(label = "Theme", onClick = { showThemeSheet = true }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    ThemeDot(theme = theme, size = 11.dp)
                    ValueText(theme.displayName)
                }
            }
            SettingsRow(label = "App appearance", onClick = { showAppearance = true }) {
                ValueText(
                    when (settings.appearance) {
                        "light" -> "Light"
                        "dark" -> "Dark"
                        else -> "System"
                    },
                )
            }
            SettingsRow(label = "Language", onClick = { showLanguage = true }) {
                ValueText(if (settings.language == "th") "ไทย" else "English")
            }

            SectionLabel("Account")
            SettingsRow(label = "Subscription", onClick = onOpenPaywall, divider = false) {
                ValueText(Plans.label(settings.plan))
            }
            Spacer(Modifier.height(AttaDimens.Lg))
        }
        Text(
            text = "ATTA 1.0 · Terms · Privacy",
            style = AttaType.caption.copy(fontSize = 10.sp),
            color = colors.inkAlpha(0.3f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )
    }

    if (showThemeSheet) {
        ThemeSheet(
            selectedId = settings.themeId,
            freeTier = Plans.isFree(settings.plan),
            onDismiss = { showThemeSheet = false },
            onPick = { picked ->
                showThemeSheet = false
                scope.launch {
                    prefs.setThemeId(picked)
                    runCatching { AttaWidgetUpdater.updateAll(context) }
                }
            },
            onRequireUpgrade = {
                showThemeSheet = false
                onOpenPaywall()
            },
        )
    }
    if (showAppearance) {
        OptionSheet(
            title = "App appearance",
            options = listOf("system" to "System", "light" to "Light", "dark" to "Dark"),
            selectedKey = settings.appearance,
            onDismiss = { showAppearance = false },
            onSelect = {
                showAppearance = false
                scope.launch { prefs.setAppearance(it) }
            },
        )
    }
    if (showLanguage) {
        OptionSheet(
            title = "Language",
            options = listOf("en" to "English", "th" to "ไทย"),
            selectedKey = settings.language,
            onDismiss = { showLanguage = false },
            onSelect = {
                showLanguage = false
                scope.launch {
                    prefs.setLanguage(it)
                    runCatching { AttaWidgetUpdater.updateAll(context) }
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = Atta.colors
    Eyebrow(
        text = text,
        color = colors.inkAlpha(0.4f),
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun ValueText(text: String) {
    Text(
        text = text,
        style = AttaType.body.copy(fontSize = 14.sp),
        color = Atta.colors.inkAlpha(0.45f),
    )
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: (() -> Unit)? = null,
    divider: Boolean = true,
    trailing: @Composable () -> Unit,
) {
    val colors = Atta.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = AttaType.body.copy(fontSize = 14.5.sp),
                color = colors.ink,
            )
            trailing()
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.inkAlpha(0.07f)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionSheet(
    title: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (key: String) -> Unit,
) {
    val colors = Atta.colors
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.canvas) {
        Column(Modifier.padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Xs)) {
            Text(
                text = title,
                style = AttaType.displaySm.copy(fontSize = 20.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            options.forEach { (key, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                        .clickable { onSelect(key) }
                        .padding(horizontal = 4.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = AttaType.body.copy(fontSize = 15.sp),
                        color = colors.ink,
                    )
                    if (key == selectedKey) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AttaPalette.Champagne),
                        )
                    }
                }
            }
            Spacer(Modifier.height(AttaDimens.Md))
        }
    }
}
