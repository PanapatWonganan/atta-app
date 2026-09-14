package com.atta.app.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.atta.app.R
import com.atta.app.data.WidgetTheme

/**
 * Wallpapers, the ATTA way: no photo packs, no downloads — the theme
 * gradient and the day's line rendered at screen size the moment they're
 * set. Same drawing rules as the share card and the widget: authored line
 * breaks, 1.6 line height for Thai tone marks.
 */
object AttaWallpaper {

    /** Wallpapers the free tier can set outright; the rest are premium or one ad each. */
    val FreeIds = setOf("linen", "dawn")

    const val Lock = WallpaperManager.FLAG_LOCK
    const val Home = WallpaperManager.FLAG_SYSTEM
    const val Both = WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM

    /** Renders and sets in one motion; the bitmap never touches disk. */
    fun set(context: Context, theme: WidgetTheme, line: String, which: Int): Boolean = runCatching {
        val metrics = context.resources.displayMetrics
        val bitmap = render(
            context = context,
            theme = theme,
            line = line,
            width = metrics.widthPixels.coerceAtLeast(720),
            height = metrics.heightPixels.coerceAtLeast(1280),
        )
        WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, which)
        bitmap.recycle()
        true
    }.getOrDefault(false)

    fun render(context: Context, theme: WidgetTheme, line: String, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = width.toFloat()
        val h = height.toFloat()
        val inset = w * 0.1f

        val (start, end) = theme.gradientPoints(w, h)
        canvas.drawPaint(
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    start.x, start.y, end.x, end.y,
                    theme.stops.map { it.second.toArgb() }.toIntArray(),
                    theme.stops.map { it.first }.toFloatArray(),
                    Shader.TileMode.CLAMP,
                )
            },
        )

        val serif = ResourcesCompat.getFont(context, R.font.noto_serif_thai_regular)
            ?: Typeface.SERIF
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif
            textSize = w * 0.062f
            color = theme.ink.toArgb()
        }
        val metrics = textPaint.fontMetrics
        val naturalLine = metrics.descent - metrics.ascent
        val multiplier = (textPaint.textSize * 1.6f / naturalLine).coerceAtLeast(1f)
        val layout = StaticLayout.Builder
            .obtain(line, 0, line.length, textPaint, (w - 2 * inset).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, multiplier)
            .setIncludePad(false)
            .build()
        // Sits below center so the lock screen clock keeps the upper air.
        canvas.save()
        canvas.translate(inset, h * 0.52f - layout.height / 2f)
        layout.draw(canvas)
        canvas.restore()

        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.ruleColor().toArgb()
            strokeWidth = h * 0.0016f
        }
        val ruleY = h * 0.52f + layout.height / 2f + h * 0.036f
        canvas.drawLine(inset, ruleY, inset + w * 0.072f, ruleY, rulePaint)

        return bitmap
    }
}
