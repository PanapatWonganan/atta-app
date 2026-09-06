package com.atta.app.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaSettings
import com.atta.app.data.WidgetThemes
import com.atta.app.ui.components.Eyebrow
import com.atta.app.ui.components.PrimaryButton
import com.atta.app.ui.components.WidgetPreviewCard
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaType
import com.atta.app.widget.AttaWidgetReceiver
import java.time.LocalDate

/**
 * The last onboarding beat: the widget is the product, so ending on "add it"
 * — with today's real line in the preview — is the whole point of the tour.
 */
@Composable
fun WidgetMomentScreen(
    settings: AttaSettings,
    onDone: () -> Unit,
) {
    val colors = Atta.colors
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val line = AffirmationRepository.lineFor(today, settings.focusIds).text(settings.language)
    val date = AffirmationRepository.shortDate(today, settings.language)
    val theme = WidgetThemes.byId(
        if (settings.freeTier) WidgetThemes.FreeThemeId else settings.themeId,
    )
    val widgetManager = AppWidgetManager.getInstance(context)
    val canPin = widgetManager.isRequestPinAppWidgetSupported

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AttaDimens.Md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.8f))
        Eyebrow(text = "ONE LAST THING", color = colors.inkAlpha(0.4f))
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Put your line\non the home screen.",
            style = AttaType.displaySm.copy(fontSize = 24.sp, lineHeight = 38.sp),
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "It changes with the morning.\nNo app to open.",
            style = AttaType.caption.copy(fontSize = 12.5.sp, lineHeight = 20.sp),
            color = colors.inkAlpha(0.55f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        WidgetPreviewCard(
            theme = theme,
            line = line,
            dateLabel = date,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(330f / 140f),
        )
        Spacer(Modifier.weight(1f))
        if (canPin) {
            PrimaryButton(
                text = "Add the widget",
                onClick = {
                    widgetManager.requestPinAppWidget(
                        ComponentName(context, AttaWidgetReceiver::class.java),
                        null,
                        null,
                    )
                    onDone()
                },
            )
            Text(
                text = "Later",
                style = AttaType.label.copy(fontSize = 12.sp, letterSpacing = 0.3.sp),
                color = colors.inkAlpha(0.45f),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(AttaDimens.RadiusChip))
                    .clickable(onClick = onDone)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        } else {
            PrimaryButton(text = "Continue", onClick = onDone)
        }
        Spacer(Modifier.height(AttaDimens.Sm))
    }
}
