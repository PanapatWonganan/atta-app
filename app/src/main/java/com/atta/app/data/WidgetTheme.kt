package com.atta.app.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * One of the eight widget/card themes from the Phase A sheet. Stops are slow and
 * soft: both end stops sit within 0.35 absolute relative luminance of each other,
 * so the paired ink holds effectively the same contrast at every point.
 */
data class WidgetTheme(
    val id: String,
    val displayName: String,
    val stops: List<Pair<Float, Color>>,
    val angleDeg: Float, // CSS convention: 0 = up, clockwise
    val ink: Color,
    val lightInk: Boolean, // true when ink is light (dark theme surface)
    val ruleChampagne: Boolean, // Linen and Onyx carry the champagne rule
    val hairline: Boolean, // Onyx: 1dp edge at 7% light ink — shadows vanish on black
) {
    /** Gradient endpoints for a w×h surface, matching CSS linear-gradient geometry. */
    fun gradientPoints(w: Float, h: Float): Pair<Offset, Offset> {
        val rad = Math.toRadians(angleDeg.toDouble())
        val dx = sin(rad).toFloat()
        val dy = -cos(rad).toFloat()
        val len = abs(w * sin(rad)).toFloat() + abs(h * cos(rad)).toFloat()
        val cx = w / 2f
        val cy = h / 2f
        return Offset(cx - dx * len / 2f, cy - dy * len / 2f) to
            Offset(cx + dx * len / 2f, cy + dy * len / 2f)
    }

    fun brush(w: Float, h: Float): Brush {
        val (start, end) = gradientPoints(w, h)
        return Brush.linearGradient(
            colorStops = stops.toTypedArray(),
            start = start,
            end = end,
        )
    }

    fun eyebrowColor(): Color = ink.copy(alpha = if (lightInk) 0.55f else 0.5f)
    fun dateColor(): Color = ink.copy(alpha = if (lightInk) 0.48f else 0.44f)
    fun ruleColor(): Color =
        if (ruleChampagne) Color(0xFFC2A57B) else ink.copy(alpha = if (lightInk) 0.3f else 0.25f)
}

object WidgetThemes {

    val Dawn = WidgetTheme(
        id = "dawn", displayName = "Dawn",
        stops = listOf(0f to Color(0xFFF5E7D8), 0.52f to Color(0xFFEFD9CB), 1f to Color(0xFFE6CBC0)),
        angleDeg = 160f, ink = Color(0xFF2A211C),
        lightInk = false, ruleChampagne = false, hairline = false,
    )

    val Mist = WidgetTheme(
        id = "mist", displayName = "Mist",
        stops = listOf(0f to Color(0xFFECEFEE), 0.5f to Color(0xFFE0E6E5), 1f to Color(0xFFD2DBDA)),
        angleDeg = 150f, ink = Color(0xFF232A29),
        lightInk = false, ruleChampagne = false, hairline = false,
    )

    val Linen = WidgetTheme(
        id = "linen", displayName = "Linen",
        stops = listOf(0f to Color(0xFFF7F4EF), 0.5f to Color(0xFFEFE8DC), 1f to Color(0xFFE4DACB)),
        angleDeg = 165f, ink = Color(0xFF1C1917),
        lightInk = false, ruleChampagne = true, hairline = false,
    )

    val Dusk = WidgetTheme(
        id = "dusk", displayName = "Dusk",
        stops = listOf(0f to Color(0xFF6B5C70), 0.55f to Color(0xFF584B5E), 1f to Color(0xFF463B50)),
        angleDeg = 155f, ink = Color(0xFFF2EDE6),
        lightInk = true, ruleChampagne = false, hairline = false,
    )

    val Onyx = WidgetTheme(
        id = "onyx", displayName = "Onyx",
        stops = listOf(0f to Color(0xFF2B2627), 0.52f to Color(0xFF211D1E), 1f to Color(0xFF151213)),
        angleDeg = 150f, ink = Color(0xFFEDE6DC),
        lightInk = true, ruleChampagne = true, hairline = true,
    )

    val SageField = WidgetTheme(
        id = "sage", displayName = "Sage Field",
        stops = listOf(0f to Color(0xFFCBD4C6), 0.52f to Color(0xFFB4C1B2), 1f to Color(0xFF9AAE9C)),
        angleDeg = 160f, ink = Color(0xFF1E2A20),
        lightInk = false, ruleChampagne = false, hairline = false,
    )

    val Clay = WidgetTheme(
        id = "clay", displayName = "Clay",
        stops = listOf(0f to Color(0xFFE8CFC6), 0.52f to Color(0xFFDBB8AE), 1f to Color(0xFFC99C94)),
        angleDeg = 158f, ink = Color(0xFF35211D),
        lightInk = false, ruleChampagne = false, hairline = false,
    )

    val DeepWater = WidgetTheme(
        id = "water", displayName = "Deep Water",
        stops = listOf(0f to Color(0xFF356E68), 0.52f to Color(0xFF2A5C57), 1f to Color(0xFF1F4B47)),
        angleDeg = 152f, ink = Color(0xFFE6F0EC),
        lightInk = true, ruleChampagne = false, hairline = false,
    )

    val Rosewood = WidgetTheme(
        id = "rosewood", displayName = "Rosewood",
        stops = listOf(0f to Color(0xFFF2DEDC), 0.52f to Color(0xFFE5C6C4), 1f to Color(0xFFD5ABA9)),
        angleDeg = 158f, ink = Color(0xFF33211F),
        lightInk = false, ruleChampagne = false, hairline = false,
    )

    val Midnight = WidgetTheme(
        id = "midnight", displayName = "Midnight",
        stops = listOf(0f to Color(0xFF2A3240), 0.52f to Color(0xFF212837), 1f to Color(0xFF171D2B)),
        angleDeg = 152f, ink = Color(0xFFE8ECF2),
        lightInk = true, ruleChampagne = false, hairline = false,
    )

    val Honey = WidgetTheme(
        id = "honey", displayName = "Honey",
        stops = listOf(0f to Color(0xFFF6E8CE), 0.52f to Color(0xFFEFD9AF), 1f to Color(0xFFE4C48F)),
        angleDeg = 162f, ink = Color(0xFF2E2414),
        lightInk = false, ruleChampagne = false, hairline = false,
    )

    val All = listOf(Dawn, Mist, Linen, Dusk, Onyx, SageField, Clay, DeepWater, Rosewood, Midnight, Honey)

    const val DefaultId = "dawn"

    /** The free tier keeps Linen only; the widget and other themes are the upgrade. */
    const val FreeThemeId = "linen"

    fun byId(id: String?): WidgetTheme = All.firstOrNull { it.id == id } ?: Dawn
}
