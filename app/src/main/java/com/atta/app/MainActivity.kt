package com.atta.app

import android.app.Activity
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
import androidx.compose.runtime.DisposableEffect
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
import com.atta.app.ads.AttaAds
import com.atta.app.analytics.AttaAnalytics
import com.atta.app.billing.AttaBilling
import com.atta.app.data.Affirmations
import com.atta.app.data.AttaPrefs
import com.atta.app.data.AttaSettings
import com.atta.app.data.Plans
import com.atta.app.ui.screens.AboutScreen
import com.atta.app.ui.screens.CommitScreen
import com.atta.app.ui.screens.ComparisonScreen
import com.atta.app.ui.screens.FirstLineScreen
import com.atta.app.ui.screens.FocusScreen
import com.atta.app.ui.screens.HomeScreen
import com.atta.app.ui.screens.PaywallScreen
import com.atta.app.ui.screens.PracticeScreen
import com.atta.app.ui.screens.ProcessingScreen
import com.atta.app.ui.screens.QuestionsScreen
import com.atta.app.ui.screens.ResultScreen
import com.atta.app.ui.screens.SavedScreen
import com.atta.app.ui.screens.SettingsScreen
import com.atta.app.ui.screens.TrialPromiseScreen
import com.atta.app.ui.screens.ValueRecapScreen
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

    val billing = remember { AttaBilling(context.applicationContext) }
    val billingReady by billing.ready.collectAsState()
    val adReady by AttaAds.rewardedReady.collectAsState()
    LaunchedEffect(Unit) {
        billing.connect()
        // Consent gate first, then the SDK; safe no-op on repeat calls.
        (context as? Activity)?.let { AttaAds.start(it) }
    }
    DisposableEffect(Unit) { onDispose { billing.release() } }

    // Play purchases land here: persist the plan, then close any open paywall
    // the same way a local subscribe would have.
    LaunchedEffect(Unit) {
        billing.purchasedPlan.collect { plan ->
            if (plan == null) return@collect
            prefs.setPlan(plan)
            runCatching { AttaWidgetUpdater.updateAll(context) }
            val entry = nav.currentBackStackEntry
            if (entry?.destination?.route?.startsWith("paywall") == true) {
                if (entry.arguments?.getString("source") == "onboarding") {
                    prefs.setOnboardingDone()
                    nav.navigate("widgetmoment") { popUpTo(0) { inclusive = true } }
                } else {
                    nav.popBackStack()
                }
            }
        }
    }

    // Widget and daily-notification taps land on that line, not just the app.
    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val lineId = activity.intent?.getStringExtra(OpenLineExtra)
        if (lineId != null && settings.onboardingDone) {
            activity.intent.removeExtra(OpenLineExtra)
            nav.navigate("line/$lineId")
        }
    }

    // Frozen at first composition: onboardingDone flips true mid-flow (at the
    // paywall) and a live startDestination would yank the graph to home,
    // skipping the widget moment.
    val startDestination = remember {
        if (settings.onboardingDone) "home" else "welcome"
    }
    NavHost(
        navController = nav,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
        exitTransition = { fadeOut(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
        popEnterTransition = { fadeIn(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
        popExitTransition = { fadeOut(tween(AttaMotion.ScreenEnterMs, easing = AttaMotion.EaseInOut)) },
    ) {
        composable("welcome") {
            WelcomeScreen { nav.navigate("questions") }
        }
        composable("questions") {
            QuestionsScreen { focusIds, themeId, perDay ->
                scope.launch {
                    prefs.setFocusIds(focusIds)
                    prefs.setThemeId(themeId)
                    prefs.setRemindersPerDay(perDay)
                }
                nav.navigate("processing")
            }
        }
        composable("processing") {
            ProcessingScreen { nav.navigate("result") }
        }
        composable("result") {
            ResultScreen(settings.focusIds, settings.language) {
                nav.navigate("firstline")
            }
        }
        // The pre-paywall run: product felt (first line, spoken), a promise
        // made, the difference shown, the ask made plain, the fear removed.
        composable("firstline") {
            LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.FirstLineView) }
            FirstLineScreen(settings.themeId, settings.focusIds, settings.language) {
                nav.navigate("commit")
            }
        }
        composable("commit") {
            LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.CommitView) }
            CommitScreen { nav.navigate("compare") }
        }
        composable("compare") {
            LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.CompareView) }
            ComparisonScreen { nav.navigate("valuerecap") }
        }
        composable("valuerecap") {
            LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.ValueView) }
            ValueRecapScreen(settings.focusIds, settings.language) {
                nav.navigate("trialpromise")
            }
        }
        composable("trialpromise") {
            LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.TrialPromiseView) }
            TrialPromiseScreen { nav.navigate("paywall/onboarding") }
        }
        composable("paywall/{source}") { entry ->
            val fromOnboarding = entry.arguments?.getString("source") == "onboarding"
            LaunchedEffect(Unit) { AttaAnalytics.log(context, AttaAnalytics.PaywallView) }
            fun close(plan: String?) {
                if (plan != null && plan != Plans.Free) {
                    AttaAnalytics.log(context, AttaAnalytics.Subscribe, "plan", plan)
                }
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
                onSubscribe = { planId ->
                    val activity = context as? Activity
                    if (billingReady && activity != null) {
                        billing.launchPurchase(activity, planId)
                    } else {
                        // No Play on this device/build: keep the local dev path.
                        close(planId)
                    }
                },
                onRestore = { billing.restore() },
                adReady = adReady && Plans.isFree(settings.plan),
                onWatchAd = {
                    (context as? Activity)?.let { activity ->
                        AttaAds.showRewarded(activity) {
                            AttaAnalytics.log(context, AttaAnalytics.RewardedEarned)
                            scope.launch {
                                prefs.setPlusPassUntil(
                                    System.currentTimeMillis() + 24L * 60 * 60 * 1000,
                                )
                            }
                            close(null)
                        }
                    }
                },
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
                onClose = {
                    nav.popBackStack()
                    // Free tier: at most one interstitial per app session here.
                    if (settings.freeTier) {
                        (context as? Activity)?.let { AttaAds.maybeShowInterstitial(it) }
                    }
                },
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
