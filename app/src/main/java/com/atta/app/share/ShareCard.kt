package com.atta.app.share

import android.content.Context
import android.content.Intent
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
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.atta.app.R
import com.atta.app.data.WidgetTheme
import java.io.File

/**
 * Shares a line as a story-sized card (1080x1920): the theme gradient, the
 * serif line, the champagne rule, a small ATTA mark. Same drawing rules as
 * the widget — authored line breaks, 1.6 line height for Thai tone marks.
 */
object ShareCard {

    private const val W = 1080
    private const val H = 1920
    private const val Inset = 108f

    fun share(context: Context, theme: WidgetTheme, line: String) {
        val bitmap = render(context, theme, line)
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "atta-card.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "“${line.replace("\n", " ")}” — ATTA")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, null))
    }

    private fun render(context: Context, theme: WidgetTheme, line: String): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val (start, end) = theme.gradientPoints(W.toFloat(), H.toFloat())
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
        val sansSemi = ResourcesCompat.getFont(context, R.font.ibm_plex_sans_thai_semibold)
            ?: Typeface.DEFAULT_BOLD

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif
            textSize = 76f
            color = theme.ink.toArgb()
        }
        val metrics = textPaint.fontMetrics
        val naturalLine = metrics.descent - metrics.ascent
        val multiplier = (textPaint.textSize * 1.6f / naturalLine).coerceAtLeast(1f)
        val layout = StaticLayout.Builder
            .obtain(line, 0, line.length, textPaint, (W - 2 * Inset).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, multiplier)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(Inset, H * 0.42f - layout.height / 2f)
        layout.draw(canvas)
        canvas.restore()

        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.ruleColor().toArgb()
            strokeWidth = 3f
        }
        val ruleY = H * 0.42f + layout.height / 2f + 70f
        canvas.drawLine(Inset, ruleY, Inset + 78f, ruleY, rulePaint)

        val markPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sansSemi
            textSize = 34f
            letterSpacing = 8f / 34f
            color = theme.eyebrowColor().toArgb()
        }
        val mark = "ATTA"
        val markWidth = markPaint.measureText(mark)
        canvas.drawText(mark, (W - markWidth) / 2f, H - 140f, markPaint)

        return bitmap
    }
}
