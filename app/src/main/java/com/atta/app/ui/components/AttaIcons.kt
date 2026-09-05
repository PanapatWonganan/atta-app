package com.atta.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/*
 * The icon set is drawn, not imported: thin 1.5dp-equivalent single-weight
 * strokes on a 16-unit grid, matching the prototype's inline SVGs.
 */

@Composable
fun BookmarkIcon(color: Color, filled: Boolean, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 16f
        val path = Path().apply {
            moveTo(4f * s, 2.5f * s)
            lineTo(12f * s, 2.5f * s)
            lineTo(12f * s, 13.5f * s)
            lineTo(8f * s, 10.5f * s)
            lineTo(4f * s, 13.5f * s)
            close()
        }
        if (filled) {
            drawPath(path, color, style = Fill)
        } else {
            drawPath(path, color, style = Stroke(width = 1.2f * s, join = StrokeJoin.Round))
        }
    }
}

@Composable
fun ShareIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 16f
        val stroke = Stroke(width = 1.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // arrow shaft + head
        drawPath(
            Path().apply {
                moveTo(8f * s, 10f * s)
                lineTo(8f * s, 2.5f * s)
                moveTo(5f * s, 5.5f * s)
                lineTo(8f * s, 2.5f * s)
                lineTo(11f * s, 5.5f * s)
            },
            color, style = stroke,
        )
        // tray
        drawPath(
            Path().apply {
                moveTo(3f * s, 9f * s)
                lineTo(3f * s, 13.5f * s)
                lineTo(13f * s, 13.5f * s)
                lineTo(13f * s, 9f * s)
            },
            color, style = stroke,
        )
    }
}

@Composable
fun DotsIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 16f
        listOf(3f, 8f, 13f).forEach { y ->
            drawCircle(color, radius = 1f * s, center = Offset(8f * s, y * s))
        }
    }
}

@Composable
fun ChevronUpIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawPath(
            Path().apply {
                moveTo(w * 0.08f, h * 0.83f)
                lineTo(w * 0.5f, h * 0.17f)
                lineTo(w * 0.92f, h * 0.83f)
            },
            color,
            style = Stroke(width = size.width * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun ChevronDownIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawPath(
            Path().apply {
                moveTo(w * 0.08f, h * 0.17f)
                lineTo(w * 0.5f, h * 0.83f)
                lineTo(w * 0.92f, h * 0.17f)
            },
            color,
            style = Stroke(width = size.width * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun CheckIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 8f
        drawPath(
            Path().apply {
                moveTo(1.5f * s, 4.2f * s)
                lineTo(3.2f * s, 6f * s)
                lineTo(6.5f * s, 2.2f * s)
            },
            color,
            style = Stroke(width = 1.3f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
