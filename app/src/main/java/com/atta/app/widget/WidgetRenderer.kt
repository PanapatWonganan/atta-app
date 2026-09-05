package com.atta.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.atta.app.R
import com.atta.app.data.WidgetTheme
import kotlin.math.min
import kotlin.math.sqrt

/**
 * The widget is one bitmap. Glance sits on RemoteViews, which has no gradients,
 * no canvas, no custom fonts — so the whole card (gradient, hairline, text) is
 * drawn here and handed over as a single image.
 *
 * Layout, all themes (Phase A §03): eyebrow baseline 34dp from top, left inset
 * 28dp; affirmation block bottom-aligned to 52dp from bottom; date baseline
 * 26dp from bottom; right inset 28dp; text column max 268dp.
 */
object WidgetRenderer {

    // Keep the bitmap under the RemoteViews transaction budget (~1.8 MB); we
    // draw up to 4x and let Glance scale, but never past this byte ceiling.
    private const val MAX_BYTES = 1_500_000f

    fun render(
        context: Context,
        theme: WidgetTheme,
        line: String,
        dateLabel: String,
        widthDp: Float,
        heightDp: Float,
    ): Bitmap {
        val safeW = widthDp.coerceAtLeast(110f)
        val safeH = heightDp.coerceAtLeast(110f)
        val scale = min(4f, sqrt(MAX_BYTES / (safeW * safeH * 4f))).coerceAtLeast(1f)
        val w = (safeW * scale).toInt()
        val h = (safeH * scale).toInt()

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = 22f * scale
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())

        val (start, end) = theme.gradientPoints(w.toFloat(), h.toFloat())
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                start.x, start.y, end.x, end.y,
                theme.stops.map { it.second.toArgb() }.toIntArray(),
                theme.stops.map { it.first }.toFloatArray(),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(rect, radius, radius, fill)

        if (theme.hairline) {
            // Onyx on a near-black wallpaper: a 1dp edge at 7% light ink —
            // the one place a border beats a shadow.
            val hairline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = scale
                color = android.graphics.Color.argb(18, 242, 237, 230)
            }
            val inset = scale / 2f
            canvas.drawRoundRect(
                RectF(inset, inset, w - inset, h - inset),
                radius, radius, hairline,
            )
        }

        val serif = ResourcesCompat.getFont(context, R.font.noto_serif_thai_regular)
            ?: Typeface.SERIF
        val sansSemi = ResourcesCompat.getFont(context, R.font.ibm_plex_sans_thai_semibold)
            ?: Typeface.DEFAULT_BOLD
        val sans = ResourcesCompat.getFont(context, R.font.ibm_plex_sans_thai_regular)
            ?: Typeface.DEFAULT

        val inset = 28f * scale

        val eyebrowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sansSemi
            textSize = 9.5f * scale
            letterSpacing = 2.2f / 9.5f
            color = theme.eyebrowColor().toArgb()
        }
        canvas.drawText(theme.displayName.uppercase(), inset, 34f * scale, eyebrowPaint)

        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.ruleColor().toArgb()
            strokeWidth = scale
        }
        canvas.drawLine(w - inset - 26f * scale, 31f * scale, w - inset, 31f * scale, rulePaint)

        val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sans
            textSize = 9.5f * scale
            letterSpacing = 0.5f / 9.5f
            color = theme.dateColor().toArgb()
        }
        canvas.drawText(dateLabel, inset, h - 26f * scale, datePaint)

        // Text is baked, so measure it ourselves: authored line breaks and an
        // explicit 1.6 line height. Auto-sizing would clip Thai tone marks.
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif
            textSize = 19f * scale
            color = theme.ink.toArgb()
        }
        val maxWidth = min(268f * scale, w - 2 * inset).toInt().coerceAtLeast(1)
        val metrics = textPaint.fontMetrics
        val naturalLine = metrics.descent - metrics.ascent
        val multiplier = (textPaint.textSize * 1.6f / naturalLine).coerceAtLeast(1f)
        val layout = StaticLayout.Builder
            .obtain(line, 0, line.length, textPaint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, multiplier)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(inset, h - 52f * scale - layout.height)
        layout.draw(canvas)
        canvas.restore()

        return bitmap
    }
}
