package com.atta.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

val LocalAttaColors = staticCompositionLocalOf { AttaLightColors }

object Atta {
    val colors: AttaColors
        @Composable get() = LocalAttaColors.current
}

/** Spacing on an 8dp base. Whitespace is the product: screen gutter never below md. */
object AttaDimens {
    val Xs = 8.dp
    val Sm = 16.dp
    val Md = 24.dp
    val Lg = 32.dp
    val Xl = 48.dp
    val Xxl = 64.dp

    val RadiusCard = 20.dp
    val RadiusButton = 14.dp
    val RadiusChip = 10.dp

    val TouchTarget = 48.dp
}

@Composable
fun AttaTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) AttaDarkColors else AttaLightColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = AttaPalette.Champagne,
            background = colors.canvas,
            surface = colors.canvas,
            onBackground = colors.ink,
            onSurface = colors.ink,
        )
    } else {
        lightColorScheme(
            primary = AttaPalette.Champagne,
            background = colors.canvas,
            surface = colors.canvas,
            onBackground = colors.ink,
            onSurface = colors.ink,
        )
    }
    CompositionLocalProvider(LocalAttaColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
