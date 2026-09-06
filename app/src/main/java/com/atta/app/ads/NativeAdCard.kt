package com.atta.app.ads

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.atta.app.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * The home-feed ad card: one quiet page every few lines on the free tier.
 * Dressed like the rest of the feed (Linen ground, serif headline) but
 * plainly labelled SPONSORED, with Google's required MediaView, attribution
 * and AdChoices in place.
 */
@Composable
fun NativeAdCard(ad: NativeAd, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context -> buildAdView(context) },
        update = { view -> bind(view, ad) },
    )
}

private const val Ink = 0xFF1C1917.toInt()
private const val Canvas = 0xFFF7F4EF.toInt()
private const val ChampagneDeep = 0xFF8A6F45.toInt()

private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

private fun buildAdView(context: Context): NativeAdView {
    val serif = ResourcesCompat.getFont(context, R.font.noto_serif_thai_regular)
        ?: Typeface.SERIF
    val sans = ResourcesCompat.getFont(context, R.font.ibm_plex_sans_thai_medium)
        ?: Typeface.DEFAULT

    val adView = NativeAdView(context)
    adView.setBackgroundColor(Canvas)

    val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(context.dp(24), context.dp(48), context.dp(24), context.dp(48))
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }

    val attribution = TextView(context).apply {
        text = "SPONSORED"
        typeface = sans
        textSize = 10f
        letterSpacing = 0.2f
        setTextColor(0x801C1917.toInt())
    }
    column.addView(attribution)

    val media = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            context.dp(200),
        ).apply { topMargin = context.dp(20) }
    }
    column.addView(media)

    val headline = TextView(context).apply {
        typeface = serif
        textSize = 24f
        setTextColor(Ink)
        setLineSpacing(0f, 1.4f)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = context.dp(24) }
    }
    column.addView(headline)

    val body = TextView(context).apply {
        typeface = sans
        textSize = 13f
        setTextColor(0xB31C1917.toInt())
        setLineSpacing(0f, 1.35f)
        maxLines = 3
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = context.dp(10) }
    }
    column.addView(body)

    val cta = Button(context, null, 0, android.R.style.Widget_Material_Button_Borderless).apply {
        typeface = sans
        textSize = 13f
        setTextColor(ChampagneDeep)
        setBackgroundColor(Color.TRANSPARENT)
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setPadding(0, context.dp(14), 0, 0)
    }
    column.addView(cta)

    adView.addView(column)
    adView.mediaView = media
    adView.headlineView = headline
    adView.bodyView = body
    adView.callToActionView = cta
    return adView
}

private fun bind(adView: NativeAdView, ad: NativeAd) {
    (adView.headlineView as TextView).text = ad.headline
    val body = adView.bodyView as TextView
    body.text = ad.body ?: ""
    body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    val cta = adView.callToActionView as Button
    cta.text = ad.callToAction ?: "Learn more"
    ad.mediaContent?.let { adView.mediaView?.mediaContent = it }
    adView.setNativeAd(ad)
}
