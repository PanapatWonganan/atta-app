package com.atta.app.ads

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * AdMob for the free tier only — paid plans and a live day pass see nothing.
 * The star is the rewarded ad: watching one earns a 24-hour Plus pass, so
 * the paywall always offers a no-money path. Banner stays off the home feed
 * and practice; the interstitial fires at most once per app session.
 */
object AttaAds {

    /** TEMPORARY: Google's sample ad units while testing. Set false for the
     * Play release — real ids clicked in dev builds risk the AdMob account. */
    const val UseTestIds = true

    // Real units (AdMob "Manifest app"). The app id lives in the manifest.
    private const val RealBanner = "ca-app-pub-5439708053812589/7623407900"
    private const val RealInterstitial = "ca-app-pub-5439708053812589/4546652573"
    private const val RealRewarded = "ca-app-pub-5439708053812589/6500179290"
    @Suppress("unused") // created in AdMob; wire up when app-open ads are wanted
    private const val RealAppOpen = "ca-app-pub-5439708053812589/9958761358"
    @Suppress("unused") // created in AdMob; wire up when native ads are wanted
    private const val RealNative = "ca-app-pub-5439708053812589/8537868473"

    val bannerUnitId: String
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/6300978111" else RealBanner
    private val interstitialUnit
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/1033173712" else RealInterstitial
    private val rewardedUnit
        get() = if (UseTestIds) "ca-app-pub-3940256099942544/5224354917" else RealRewarded

    /** True while a rewarded ad is loaded and waiting; the paywall row shows
     * only when this is true, so the offer never dead-ends. */
    val rewardedReady: StateFlow<Boolean> get() = _rewardedReady
    private val _rewardedReady = MutableStateFlow(false)

    private var initialized = false
    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private var interstitialShownThisSession = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        runCatching { MobileAds.initialize(context.applicationContext) {} }
        loadRewarded(context)
        loadInterstitial(context)
    }

    fun loadRewarded(context: Context) {
        if (rewarded != null) return
        RewardedAd.load(
            context.applicationContext,
            rewardedUnit,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    _rewardedReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                    _rewardedReady.value = false
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
        if (interstitial != null) return
        InterstitialAd.load(
            context.applicationContext,
            interstitialUnit,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
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
}

/** A standard banner, used only on secondary screens for the free tier. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AttaAds.bannerUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
