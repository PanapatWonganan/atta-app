package com.atta.app.data

import androidx.compose.ui.graphics.Color
import com.atta.app.ui.theme.AttaPalette

/**
 * Ten focus categories (placeholder names pending the client's list). Each maps
 * to one muted accent per the brief: sage = calm, clay = self & love,
 * deep teal = strength & boundaries, plum = night.
 */
data class Category(
    val id: String,
    val en: String,
    val th: String,
    val accent: Color,
) {
    fun name(lang: String): String = if (lang == "th") th else en
}

object Categories {

    val All = listOf(
        Category("calm-mornings", "Calm mornings", "เช้าที่สงบ", AttaPalette.Sage),
        Category("self-worth", "Self-worth", "คุณค่าในตัวเอง", AttaPalette.Clay),
        Category("boundaries", "Boundaries", "ขอบเขตของใจ", AttaPalette.DeepTeal),
        Category("love", "Love", "ความรัก", AttaPalette.Clay),
        Category("focus-work", "Focus at work", "สมาธิกับงาน", AttaPalette.DeepTeal),
        Category("gratitude", "Gratitude", "ขอบคุณสิ่งที่มี", AttaPalette.Sage),
        Category("rest", "Rest", "การพักผ่อน", AttaPalette.Sage),
        Category("healing", "Healing", "การเยียวยา", AttaPalette.Clay),
        Category("courage", "Courage", "ความกล้า", AttaPalette.DeepTeal),
        Category("nights", "Nights", "ค่ำคืน", AttaPalette.Plum),
    )

    const val MaxSelected = 3

    fun byId(id: String?): Category? = All.firstOrNull { it.id == id }

    fun name(id: String?, lang: String): String = byId(id)?.name(lang) ?: ""
}
