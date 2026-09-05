package com.atta.app.data

enum class Daypart { MORNING, NIGHT, ANY }

/**
 * Affirmations are hand-broken, not auto-wrapped: each line is authored as a
 * phrase and stored with its breaks. Never re-wrap these — the default
 * character wrap splits Thai vowel clusters.
 */
data class Affirmation(
    val id: String,
    val en: String,
    val th: String,
    val categoryId: String,
    val daypart: Daypart = Daypart.ANY,
) {
    fun text(lang: String): String = if (lang == "th") th else en
}

object Affirmations {

    val All = listOf(
        Affirmation(
            id = "begin-gently",
            en = "Begin gently.\nNothing is behind you.",
            th = "เริ่มอย่างอ่อนโยน\nไม่มีอะไรค้างอยู่ข้างหลัง",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "clarity-after-quiet",
            en = "Clarity comes after\nthe quiet, not before it.",
            th = "ความชัดเจนมาหลังความเงียบ\nไม่ใช่ก่อนหน้ามัน",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "rest-is-work",
            en = "Rest is not the reward.\nIt is part of the work.",
            th = "การพักไม่ใช่รางวัล\nแต่คือส่วนหนึ่งของงาน",
            categoryId = "rest",
        ),
        Affirmation(
            id = "set-it-down",
            en = "Set it down.\nThe day is finished with you.",
            th = "วางมันลงเถอะ\nวันนี้ทำหน้าที่ของมันจบแล้ว",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "owe-no-restlessness",
            en = "You do not owe anyone\nyour restlessness.",
            th = "คุณไม่ได้ติดค้าง\nความกระวนกระวายให้ใคร",
            categoryId = "boundaries", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "steady-pace",
            en = "Steady is a pace,\nnot a personality.",
            th = "ความสม่ำเสมอคือจังหวะ\nไม่ใช่นิสัย",
            categoryId = "calm-mornings",
        ),
        Affirmation(
            id = "being-known",
            en = "Being known is worth\nthe risk of being seen.",
            th = "การมีคนเข้าใจ คุ้มค่ากับ\nความเสี่ยงที่จะถูกมองเห็น",
            categoryId = "love",
        ),
        Affirmation(
            id = "no-complete-sentence",
            en = "No is a complete\nsentence, said kindly.",
            th = "คำว่าไม่ คือประโยคที่สมบูรณ์\nเมื่อพูดอย่างอ่อนโยน",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "did-enough-today",
            en = "You did enough today,\neven the parts no one saw.",
            th = "วันนี้คุณทำมากพอแล้ว\nแม้ในส่วนที่ไม่มีใครเห็น",
            categoryId = "self-worth", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "speed-that-keeps-you-whole",
            en = "You are allowed to move\nat the speed that keeps you whole.",
            th = "คุณมีสิทธิ์เดินหน้า\nในจังหวะที่ไม่ทำให้ตัวเองพัง",
            categoryId = "healing",
        ),
        Affirmation(
            id = "still-becoming",
            en = "The version of you that\nis still becoming deserves\nthe same kindness",
            th = "คนที่ยังเติบโตอยู่\nก็สมควรได้รับความอ่อนโยน\nเท่ากับคนที่ไปถึงแล้ว",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "always-recover",
            en = "You can always recover.\nThe one who rests goes furthest.",
            th = "ฟื้นตัวได้เสมอ ผู้ที่รู้จักพัก\nคือผู้ที่ไปได้ไกลที่สุด",
            categoryId = "rest",
        ),
        Affirmation(
            id = "start-slowly",
            en = "Today is allowed\nto start slowly.",
            th = "วันนี้ได้รับอนุญาต\nให้เริ่มช้า ๆ",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "brave-is-quiet",
            en = "Brave is quiet.\nIt shows up anyway.",
            th = "ความกล้าหาญมักเงียบ\nแต่มาปรากฏตัวเสมอ",
            categoryId = "courage",
        ),
        Affirmation(
            id = "once-what-you-hoped",
            en = "What you already have\nwas once what you hoped for.",
            th = "สิ่งที่คุณมีอยู่ตอนนี้\nครั้งหนึ่งคือสิ่งที่คุณเคยหวัง",
            categoryId = "gratitude",
        ),
    )

    fun byId(id: String?): Affirmation? = All.firstOrNull { it.id == id }
}
