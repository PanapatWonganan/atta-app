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

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    fun connect() {
        if (client.isReady) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                _ready.value = result.responseCode == BillingClient.BillingResponseCode.OK
                if (_ready.value) refreshPurchases()
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
            val product = details.firstOrNull() ?: return@queryProductDetailsAsync
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

    private companion object {
        // Product ids to create in Play Console before release.
        const val WeeklyProduct = "atta_weekly"
        const val YearlyProduct = "atta_yearly"
        const val LifetimeProduct = "atta_lifetime"
    }
}
