package com.atta.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.atta.app.data.Plans
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Google Play billing behind the app's local plan model. When Play isn't
 * reachable (emulators, dev builds) [ready] stays false and the paywall
 * falls back to setting the plan locally, so testing never blocks on a store.
 */
class AttaBilling(context: Context) : PurchasesUpdatedListener {

    /** True once Play is connected and can sell. */
    val ready: StateFlow<Boolean> get() = _ready
    private val _ready = MutableStateFlow(false)

    /** Set to a Plans.* value whenever Play reports an owned purchase. */
    val purchasedPlan: StateFlow<String?> get() = _purchasedPlan
    private val _purchasedPlan = MutableStateFlow<String?>(null)

    /**
     * Live store prices keyed by plan id, in the user's own currency —
     * what the paywall shows instead of hardcoded dollars. Empty until
     * Play answers (or forever, on devices without Play).
     */
    val prices: StateFlow<Map<String, PlanPrice>> get() = _prices
    private val _prices = MutableStateFlow<Map<String, PlanPrice>>(emptyMap())

    data class PlanPrice(val formatted: String, val perMonthApprox: String? = null)

    /**
     * The welcome-back deal: a Play offer on the yearly plan whose offer id
     * contains "welcome" (create it in the Console, e.g. "welcome50" at half
     * the first year). Null until Play reports one.
     */
    val welcomeOffer: StateFlow<WelcomeOffer?> get() = _welcomeOffer
    private val _welcomeOffer = MutableStateFlow<WelcomeOffer?>(null)

    data class WelcomeOffer(
        val discounted: String, // first-year price, formatted
        val original: String?, // the plain recurring price it undercuts
        val perMonthApprox: String?,
        val offerToken: String,
    )

    private var yearlyDetails: com.android.billingclient.api.ProductDetails? = null

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    fun connect() {
        // Testing switch: while true, Play is never marked ready, so the
        // paywall unlocks plans locally (free) even on devices that have the
        // Play Store. Flip to false once the three products exist in Play
        // Console and the app ships through Play.
        if (LocalTestingMode) return
        if (client.isReady) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                _ready.value = result.responseCode == BillingClient.BillingResponseCode.OK
                if (_ready.value) {
                    refreshPurchases()
                    refreshPrices()
                }
            }

            override fun onBillingServiceDisconnected() {
                _ready.value = false
            }
        })
    }

    fun launchPurchase(activity: Activity, planId: String) {
        val productId = productIdFor(planId)
        val type = if (planId == Plans.Lifetime) {
            BillingClient.ProductType.INAPP
        } else {
            BillingClient.ProductType.SUBS
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(type)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(params) { result, details ->
            // Billing 8: the callback now hands a QueryProductDetailsResult.
            val product = details.productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .apply {
                    product.subscriptionOfferDetails?.firstOrNull()?.let {
                        setOfferToken(it.offerToken)
                    }
                }
                .build()
            client.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productParams))
                    .build(),
            )
        }
    }

    /** "Restore purchase": re-reads what this Google account already owns. */
    fun restore() {
        if (_ready.value) refreshPurchases()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach(::handle)
        }
    }

    fun release() {
        runCatching { client.endConnection() }
    }

    private fun refreshPurchases() {
        listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP).forEach { type ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build(),
            ) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.forEach(::handle)
                }
            }
        }
    }

    /** One query per product type; results land as formatted local prices. */
    private fun refreshPrices() {
        val subs = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(WeeklyProduct, YearlyProduct).map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                },
            )
            .build()
        client.queryProductDetailsAsync(subs) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            details.productDetailsList.firstOrNull { it.productId == YearlyProduct }?.let { yearly ->
                yearlyDetails = yearly
                findWelcomeOffer(yearly)
            }
            val updates = details.productDetailsList.mapNotNull { product ->
                // The recurring price is the last pricing phase (free trials
                // and intro offers come first in the list).
                val phase = product.subscriptionOfferDetails
                    ?.firstOrNull()?.pricingPhases?.pricingPhaseList
                    ?.lastOrNull { it.priceAmountMicros > 0 } ?: return@mapNotNull null
                val plan = planFor(listOf(product.productId)) ?: return@mapNotNull null
                val perMonth = if (product.productId == YearlyProduct) {
                    formatMicros(phase.priceAmountMicros / 12, phase.priceCurrencyCode)
                } else {
                    null
                }
                plan to PlanPrice(phase.formattedPrice, perMonth)
            }
            _prices.value = _prices.value + updates
        }
        val inapp = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(LifetimeProduct)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(inapp) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            details.productDetailsList.firstOrNull()?.oneTimePurchaseOfferDetails?.let { offer ->
                _prices.value = _prices.value + (Plans.Lifetime to PlanPrice(offer.formattedPrice))
            }
        }
    }

    /** A yearly offer named welcome* becomes the returning-user deal. */
    private fun findWelcomeOffer(yearly: com.android.billingclient.api.ProductDetails) {
        val offers = yearly.subscriptionOfferDetails ?: return
        val welcome = offers.firstOrNull { it.offerId?.contains("welcome") == true } ?: return
        val discounted = welcome.pricingPhases.pricingPhaseList
            .firstOrNull { it.priceAmountMicros > 0 } ?: return
        // The plain recurring price: the last paid phase of the base offer.
        val base = offers.firstOrNull { it.offerId == null } ?: welcome
        val original = base.pricingPhases.pricingPhaseList.lastOrNull { it.priceAmountMicros > 0 }
        _welcomeOffer.value = WelcomeOffer(
            discounted = discounted.formattedPrice,
            original = original?.formattedPrice,
            perMonthApprox = formatMicros(discounted.priceAmountMicros / 12, discounted.priceCurrencyCode),
            offerToken = welcome.offerToken,
        )
    }

    /** Launches the yearly purchase on the welcome offer's token. */
    fun launchWelcomePurchase(activity: Activity) {
        val product = yearlyDetails ?: return
        val token = _welcomeOffer.value?.offerToken ?: return
        client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(token)
                            .build(),
                    ),
                )
                .build(),
        )
    }

    private fun formatMicros(micros: Long, currencyCode: String): String = runCatching {
        val format = java.text.NumberFormat.getCurrencyInstance()
        format.currency = java.util.Currency.getInstance(currencyCode)
        format.maximumFractionDigits = 2
        format.format(micros / 1_000_000.0)
    }.getOrDefault("")

    private fun handle(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.isAcknowledged) {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build(),
            ) {}
        }
        planFor(purchase.products)?.let { _purchasedPlan.value = it }
    }

    private fun productIdFor(planId: String): String = when (planId) {
        Plans.TrialWeekly -> WeeklyProduct
        Plans.TrialYearly -> YearlyProduct
        else -> LifetimeProduct
    }

    private fun planFor(products: List<String>): String? = when {
        LifetimeProduct in products -> Plans.Lifetime
        YearlyProduct in products -> Plans.TrialYearly
        WeeklyProduct in products -> Plans.TrialWeekly
        else -> null
    }

    companion object {
        /** Real Play billing. The three products exist and are active in the
         *  Console; the local-plan fallback still covers devices without Play. */
        const val LocalTestingMode = false

        // Product ids to create in Play Console before release.
        private const val WeeklyProduct = "atta_weekly"
        private const val YearlyProduct = "atta_yearly"
        private const val LifetimeProduct = "atta_lifetime"
    }
}
