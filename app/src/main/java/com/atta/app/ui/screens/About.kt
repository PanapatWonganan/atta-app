package com.atta.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atta.app.ui.components.Eyebrow
import com.atta.app.ui.theme.Atta
import com.atta.app.ui.theme.AttaDimens
import com.atta.app.ui.theme.AttaType

/**
 * Terms and privacy, readable in-app. The privacy story is short because the
 * data story is short: everything lives on the device.
 */
@Composable
fun AboutScreen() {
    val colors = Atta.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AttaDimens.Md, vertical = AttaDimens.Md),
    ) {
        Text(
            text = "About ATTA",
            style = AttaType.displaySm.copy(fontSize = 24.sp),
            color = colors.ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Version 1.0",
            style = AttaType.caption,
            color = colors.inkAlpha(0.45f),
        )

        Section("PRIVACY")
        Body(
            "ATTA keeps your words on your device. Your saved lines, your own " +
                "lines, check-in history, and settings are stored locally and " +
                "included in your phone's standard Android backup — we never " +
                "see them.\n\n" +
                "To improve the app, ATTA collects anonymous usage statistics " +
                "and crash reports through Google Firebase (for example, that " +
                "a practice session was started — never what was read or " +
                "written). Ads on the free tier are served by Google AdMob " +
                "under its own policies. We do not sell personal data.\n\n" +
                "The voice that reads your lines is your device's own " +
                "text-to-speech engine, running on the device. If you share a " +
                "line as an image, it is shared only through the app you " +
                "choose in the share sheet.",
        )

        Section("TERMS")
        Body(
            "ATTA offers a free tier and an optional paid tier (ATTA full) " +
                "that unlocks all widget themes, practice sounds, and your own " +
                "lines. Subscriptions are billed through Google Play and can " +
                "be cancelled there at any time; the paid features remain " +
                "yours until the end of the paid period.\n\n" +
                "ATTA's lines are reflections, not medical or psychological " +
                "advice. If you are struggling, please reach out to someone " +
                "you trust or a professional — the app is a companion, not a " +
                "substitute.\n\n" +
                "The app is provided as-is; we work to keep it quiet, honest, " +
                "and dependable.",
        )

        Section("CONTACT")
        Body("theapppresso@gmail.com")
        Spacer(Modifier.height(AttaDimens.Xl))
    }
}

@Composable
private fun Section(text: String) {
    Eyebrow(
        text = text,
        color = Atta.colors.inkAlpha(0.4f),
        modifier = Modifier.padding(top = 26.dp, bottom = 8.dp),
    )
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        style = AttaType.body.copy(fontSize = 13.5.sp, lineHeight = 23.sp),
        color = Atta.colors.inkAlpha(0.75f),
    )
}
