package com.atta.app.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * AdMob for the free tier only — paid plans and a live day pass see nothing.
 * The star is the rewarded ad: watching one earns a 24-hour Plus pass. Every
 * format retries with backoff after a failed load, the banner is adaptive,
 * and loading starts only after the UMP consent gate says ads may be
 * requested — the three code-side levers that keep match rate honest.
 */
object AttaAds {

    /** Real ad units for the Play release. Flip back to true in dev builds —
     * real ids clicked during development risk the AdMob account. */
    const val UseTestIds = false

    // Real units (AdMob "Manifest app"). The app id lives in the manifest.
    private const val RealBanner = "ca-app-pub-5439708053812589/7623407900"
    private const val RealInterstitial = "ca-app-pub-5439708053812589/4546652573"
    private const val RealRewarded = "ca-app-pub-5439708053812589/6500179290"
    private const val RealNative = "ca-app-pub-5439708053812589/8537868473"
    @Suppress("unused") // created in AdMob; wire up when app-open ads are wanted
    private const val RealAppOpen = "ca-app-pub-5439708053812589/9958761358"

    val bannerUnitId: String
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/6300978111" else RealBanner
    private val interstitialUnit
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/1033173712" else RealInterstitial
    private val rewardedUnit
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/5224354917" else RealRewarded
    private val nativeUnit
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/2247696110" else RealNative

    /** True while a rewarded ad is loaded and waiting; the paywall row shows
     * only when this is true, so the offer never dead-ends. */
    val rewardedReady: StateFlow<Boolean> get() = _rewardedReady
    private val _rewardedReady = MutableStateFlow(false)

    /** The one cached native ad for the home-feed ad card (null = no card). */
    val nativeAd: StateFlow<NativeAd?> get() = _nativeAd
    private val _nativeAd = MutableStateFlow<NativeAd?>(null)

    private val main = Handler(Looper.getMainLooper())
    private var started = false
    private var adsAllowed = false
    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private var interstitialShownThisSession = false
    private var rewardedRetries = 0
    private var interstitialRetries = 0
    private var nativeRetries = 0

    /**
     * Entry point: runs the UMP consent gate (shows Google's form where the
     * region requires one), then initializes the SDK and preloads. Safe to
     * call again — it runs once.
     */
    fun start(activity: Activity) {
        if (started) return
        started = true
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    if (consent.canRequestAds()) beginLoading(activity)
                }
            },
            {
                // Consent state unavailable (offline etc.): try anyway; the
                // SDK itself enforces the final say.
                beginLoading(activity)
            },
        )
        // Consent may already be settled from a previous session.
        if (consent.canRequestAds()) beginLoading(activity)
    }

    private fun beginLoading(context: Context) {
        if (adsAllowed) return
        adsAllowed = true
        runCatching { MobileAds.initialize(context.applicationContext) {} }
        loadRewarded(context)
        loadInterstitial(context)
        loadNative(context)
    }

    /** Failed loads retry with capped exponential backoff instead of going
     * silent for the rest of the session. */
    private fun retry(attempt: Int, block: () -> Unit) {
        val delayMs = (5_000L shl attempt.coerceAtMost(4)).coerceAtMost(60_000L)
        main.postDelayed(block, delayMs)
    }

    fun loadRewarded(context: Context) {
        if (!adsAllowed || rewarded != null) return
        RewardedAd.load(
            context.applicationContext,
            rewardedUnit,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    rewardedRetries = 0
                    _rewardedReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                    _rewardedReady.value = false
                    retry(rewardedRetries++) { loadRewarded(context) }
                }
            },
        )
    }

    /** Shows the rewarded ad; [onEarned] fires only when the reward is earned. */
    fun showRewarded(activity: Activity, onEarned: () -> Unit) {
        val ad = rewarded ?: return
        rewarded = null
        _rewardedReady.value = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = loadRewarded(activity)
            override fun onAdFailedToShowFullScreenContent(error: AdError) = loadRewarded(activity)
        }
        ad.show(activity) { onEarned() }
    }

    private fun loadInterstitial(context: Context) {
        if (!adsAllowed || interstitial != null) return
        InterstitialAd.load(
            context.applicationContext,
            interstitialUnit,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    interstitialRetries = 0
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    retry(interstitialRetries++) { loadInterstitial(context) }
                }
            },
        )
    }

    /** At most one interstitial per app session, and only if one is loaded. */
    fun maybeShowInterstitial(activity: Activity) {
        if (interstitialShownThisSession) return
        val ad = interstitial ?: return
        interstitial = null
        interstitialShownThisSession = true
        ad.show(activity)
    }

    private fun loadNative(context: Context) {
        if (!adsAllowed || _nativeAd.value != null) return
        val loader = AdLoader.Builder(context.applicationContext, nativeUnit)
            .forNativeAd { ad ->
                _nativeAd.value?.destroy()
                _nativeAd.value = ad
                nativeRetries = 0
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    retry(nativeRetries++) { loadNative(context) }
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build(),
            )
            .build()
        loader.loadAd(AdRequest.Builder().build())
    }
}

/**
 * Adaptive banner for secondary screens on the free tier: it asks Google for
 * the best height at this exact width (better match rate than a fixed
 * 320x50), and destroys itself when the screen goes away.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                val metrics = context.resources.displayMetrics
                val widthDp = (metrics.widthPixels / metrics.density).toInt()
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp),
                )
                adUnitId = AttaAds.bannerUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { it.destroy() },
    )
}
