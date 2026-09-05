package com.atta.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Raw brand tokens — atta.color.* from the Phase A sheet. */
object AttaPalette {
    val Canvas = Color(0xFFF7F4EF)
    val CanvasAlt = Color(0xFFEDE6DC)
    val Ink = Color(0xFF1C1917)
    val CanvasDark = Color(0xFF191517)
    val InkDark = Color(0xFFF2EDE6)

    // One accent per screen. Matte only — never gradiented, never glossed.
    val Champagne = Color(0xFFC2A57B)
    val ChampagneDeep = Color(0xFF8A7550)
    val Sage = Color(0xFF9AAE9C)
    val SageDeep = Color(0xFF7E937F)
    val Clay = Color(0xFFC99C94)
    val DeepTeal = Color(0xFF1F4B47)
    val Plum = Color(0xFF5E4C63)
}

/** Mode-resolved surfaces. Dark mode inverts canvas/ink; champagne stays matte on both. */
data class AttaColors(
    val canvas: Color,
    val canvasAlt: Color,
    val ink: Color,
    val card: Color,
    val onInk: Color,
    val isDark: Boolean,
) {
    fun inkAlpha(alpha: Float): Color = ink.copy(alpha = alpha)
}

val AttaLightColors = AttaColors(
    canvas = AttaPalette.Canvas,
    canvasAlt = AttaPalette.CanvasAlt,
    ink = AttaPalette.Ink,
    card = Color.White,
    onInk = AttaPalette.InkDark,
    isDark = false,
)

val AttaDarkColors = AttaColors(
    canvas = AttaPalette.CanvasDark,
    canvasAlt = AttaPalette.InkDark.copy(alpha = 0.08f),
    ink = AttaPalette.InkDark,
    card = AttaPalette.InkDark.copy(alpha = 0.06f),
    onInk = AttaPalette.Ink,
    isDark = true,
)
