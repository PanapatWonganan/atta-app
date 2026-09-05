package com.atta.app.widget

import android.content.Context
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.atta.app.MainActivity
import com.atta.app.data.AffirmationRepository
import com.atta.app.data.AttaPrefs
import com.atta.app.data.Plans
import com.atta.app.data.WidgetThemes
import java.time.LocalDate
import java.time.LocalTime

/**
 * Renders per exact size and re-renders only when the line or theme changes —
 * never on an update tick. One tap target: the whole surface opens today's card.
 */
class AttaWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = AttaPrefs(context).snapshot()
        val today = LocalDate.now()
        val evening = settings.eveningLine && LocalTime.now().hour >= 18
        val affirmation = AffirmationRepository.lineFor(today, settings.focusIds, evening)
        val line = affirmation.text(settings.language)
        val date = AffirmationRepository.shortDate(today, settings.language)
        val theme = WidgetThemes.byId(
            if (Plans.isFree(settings.plan)) WidgetThemes.FreeThemeId else settings.themeId,
        )

        provideContent {
            val size = LocalSize.current
            val bitmap = remember(size.width.value, size.height.value, theme.id, line) {
                WidgetRenderer.render(
                    context = context,
                    theme = theme,
                    line = line,
                    dateLabel = date,
                    widthDp = size.width.value,
                    heightDp = size.height.value,
                )
            }
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = line.replace("\n", " "),
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>()),
            )
        }
    }
}

class AttaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AttaWidget()
}

object AttaWidgetUpdater {
    suspend fun updateAll(context: Context) = AttaWidget().updateAll(context)
}
