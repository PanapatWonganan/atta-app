package com.atta.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.atta.app.R

val NotoSerifThai = FontFamily(
    Font(R.font.noto_serif_thai_light, FontWeight.Light),
    Font(R.font.noto_serif_thai_regular, FontWeight.Normal),
    Font(R.font.noto_serif_thai_medium, FontWeight.Medium),
    Font(R.font.noto_serif_thai_semibold, FontWeight.SemiBold),
)

val PlexSansThai = FontFamily(
    Font(R.font.ibm_plex_sans_thai_light, FontWeight.Light),
    Font(R.font.ibm_plex_sans_thai_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_thai_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_thai_semibold, FontWeight.SemiBold),
)

/**
 * atta.type.* — line heights are generous by design: Thai upper tone marks need
 * >= 1.6x on serif display sizes or they collide with the ascender band above.
 */
object AttaType {
    val display = TextStyle(
        fontFamily = NotoSerifThai,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 54.sp,
        letterSpacing = (-0.2).sp,
    )
    val displaySm = TextStyle(
        fontFamily = NotoSerifThai,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 42.sp,
    )
    val title = TextStyle(
        fontFamily = PlexSansThai,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 32.sp,
    )
    val body = TextStyle(
        fontFamily = PlexSansThai,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 27.sp,
    )
    val label = TextStyle(
        fontFamily = PlexSansThai,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.1.sp,
    )
    val caption = TextStyle(
        fontFamily = PlexSansThai,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
    )
    val eyebrow = TextStyle(
        fontFamily = PlexSansThai,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp,
    )
}
