package com.atta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atta.app.data.Affirmations
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.Plans
import com.atta.app.ui.screens.AboutScreen
import com.atta.app.ui.screens.FocusScreen
import com.atta.app.ui.screens.HomeScreen
import com.atta.app.ui.screens.PaywallScreen
import com.atta.app.ui.screens.PracticeScreen
import com.atta.app.ui.screens.ProcessingScreen
import com.atta.app.ui.screens.QuestionsScreen
import com.atta.app.ui.screens.ResultScreen
import com.atta.app.ui.screens.SavedScreen
import com.atta.app.ui.screens.SettingsScreen
import com.atta.app.ui.screens.ViewerScreen
import com.atta.app.ui.screens.WelcomeScreen
import com.atta.app.ui.screens.WidgetGalleryScreen
import com.atta.app.ui.screens.WidgetMomentScreen
import com.atta.app.ui.theme.AttaMotion
import com.atta.app.ui.theme.AttaPalette
import com.atta.app.ui.theme.AttaTheme
import com.atta.app.widget.AttaWidgetUpdater
import com.atta.app.widget.OpenLineExtra
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AttaRoot() }
    }
}

@Composable
fun AttaRoot() {
    val context = LocalContext.current
    val prefs = remember { AttaPrefs(context) }
    val settings by prefs.settings.collectAsState(initial = null)
    val systemDark = isSystemInDarkTheme()
    val current = settings
    if (current == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (systemDark) AttaPalette.CanvasDark else AttaPalette.Canvas),
        )
        return
    }
    val dark = when (current.appearance) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }
    AttaTheme(dark) {
        AttaNavHost(prefs, current)
    }
}

@Composable
fun AttaNavHost(prefs: AttaPrefs, settings: AttaSettings) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Widget and daily-notification taps land on that line, not just the app.
    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val lineId = activity.intent?.getStringExtra(OpenLineExtra)
        if (lineId != null && settings.onboardingDone) {
            activity.intent.removeExtra(OpenLineExtra)
            nav.navigate("line/$lineId")
        }
    }

    NavHost(
        navController = nav,
        startDestination = if (settings.onboardingDone) "home" else "welcome",
        enterTransition = { fadeIn(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
        exitTransition = { fadeOut(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
        popEnterTransition = { fadeIn(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
        popExitTransition = { fadeOut(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
    ) {
        composable("welcome") {
            WelcomeScreen { nav.navigate("questions") }
        }
        composable("questions") {
            QuestionsScreen { focusIds, themeId ->
                scope.launch {
                    prefs.setFocusIds(focusIds)
                    prefs.setThemeId(themeId)
                }
                nav.navigate("processing")
            }
        }
        composable("processing") {
            ProcessingScreen { nav.navigate("result") }
        }
        composable("result") {
            ResultScreen(settings.focusIds, settings.language) {
                nav.navigate("paywall/onboarding")
            }
        }
        composable("paywall/{source}") { entry ->
            val fromOnboarding = entry.arguments?.getString("source") == "onboarding"
            fun close(plan: String?) {
                scope.launch {
                    plan?.let { prefs.setPlan(it) }
                    if (fromOnboarding) prefs.setOnboardingDone()
                    runCatching { AttaWidgetUpdater.updateAll(context) }
                }
                if (fromOnboarding) {
                    nav.navigate("widgetmoment") { popUpTo(0) { inclusive = true } }
                } else {
                    nav.popBackStack()
                }
            }
            PaywallScreen(
                onNotNow = { close(if (Plans.isFree(settings.plan)) Plans.Free else null) },
                onSubscribe = { close(it) },
            )
        }
        composable("widgetmoment") {
            WidgetMomentScreen(settings = settings) {
                nav.navigate("home") { popUpTo(0) { inclusive = true } }
            }
        }
        composable("home") {
            HomeScreen(prefs, settings) { route -> nav.navigate(route) }
        }
        composable("practice/{source}/{index}") { entry ->
            PracticeScreen(
                prefs = prefs,
                settings = settings,
                source = entry.arguments?.getString("source") ?: "feed",
                startIndex = entry.arguments?.getString("index")?.toIntOrNull() ?: 0,
                onClose = { nav.popBackStack() },
                onRequireUpgrade = { nav.navigate("paywall/upgrade") },
            )
        }
        composable("gallery") {
            WidgetGalleryScreen(prefs, settings) { nav.navigate("paywall/upgrade") }
        }
        composable("focus") {
            FocusScreen(prefs, settings) { nav.popBackStack() }
        }
        composable("saved") {
            SavedScreen(
                settings = settings,
                prefs = prefs,
                onOpenLine = { id -> nav.navigate("line/$id") },
                onPractice = { nav.navigate("practice/saved/0") },
                onRequireUpgrade = { nav.navigate("paywall/upgrade") },
            )
        }
        composable("settings") {
            SettingsScreen(
                prefs = prefs,
                settings = settings,
                onOpenFocus = { nav.navigate("focus") },
                onOpenPaywall = { nav.navigate("paywall/upgrade") },
                onOpenAbout = { nav.navigate("about") },
            )
        }
        composable("about") {
            AboutScreen()
        }
        composable("line/{id}") { entry ->
            val affirmation = Affirmations.byId(entry.arguments?.getString("id"))
            if (affirmation == null) {
                LaunchedEffect(Unit) { nav.popBackStack() }
            } else {
                ViewerScreen(affirmation, settings, prefs) { nav.popBackStack() }
            }
        }
    }
}
