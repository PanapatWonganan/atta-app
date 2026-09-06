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

        // calm-mornings
        Affirmation(
            id = "morning-asks-little",
            en = "The morning asks for\nnothing but your presence.",
            th = "เช้าวันนี้ไม่ได้ขออะไร\nนอกจากให้คุณอยู่ตรงนี้",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "one-thing-first",
            en = "One thing first.\nThe rest can wait in line.",
            th = "ทีละอย่างก่อน\nที่เหลือรอคิวได้",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "quiet-before-noise",
            en = "Give the first minutes\nto yourself, not the noise.",
            th = "มอบนาทีแรกของวัน\nให้ตัวเอง ไม่ใช่ให้เสียงรบกวน",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "day-not-race",
            en = "Today is a day,\nnot a race.",
            th = "วันนี้คือหนึ่งวัน\nไม่ใช่การแข่งขัน",
            categoryId = "calm-mornings",
        ),
        Affirmation(
            id = "soft-start-strong-day",
            en = "A soft start can still\ncarry a strong day.",
            th = "การเริ่มต้นที่นุ่มนวล\nก็พาวันที่มั่นคงมาได้",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "breath-before-plans",
            en = "Breathe first.\nPlan second.",
            th = "หายใจก่อน\nค่อยวางแผน",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "unhurried-is-present",
            en = "Unhurried is not behind.\nIt is simply present.",
            th = "ความไม่รีบร้อนไม่ใช่ความล่าช้า\nแต่คือการอยู่กับปัจจุบัน",
            categoryId = "calm-mornings",
        ),
        Affirmation(
            id = "new-page",
            en = "The day is a new page.\nWrite it slowly.",
            th = "วันนี้คือหน้ากระดาษใหม่\nค่อย ๆ เขียนมัน",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "light-before-worry",
            en = "Let the light arrive\nbefore the worry does.",
            th = "ให้แสงมาถึง\nก่อนความกังวล",
            categoryId = "calm-mornings", daypart = Daypart.MORNING,
        ),

        // self-worth
        Affirmation(
            id = "worth-not-output",
            en = "Your worth was never\nmeasured in output.",
            th = "คุณค่าของคุณไม่เคย\nถูกวัดด้วยผลงาน",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "enough-before-proving",
            en = "You were enough\nbefore you proved anything.",
            th = "คุณมีค่ามากพอ\nตั้งแต่ก่อนจะพิสูจน์อะไร",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "quiet-progress",
            en = "Quiet progress\nis still progress.",
            th = "ความก้าวหน้าที่เงียบงัน\nก็ยังคือความก้าวหน้า",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "not-for-everyone",
            en = "You are not for everyone.\nYou never needed to be.",
            th = "คุณไม่จำเป็นต้องถูกใจทุกคน\nและไม่เคยจำเป็นเลย",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "speak-kindly-inward",
            en = "Speak to yourself the way\nyou speak to someone you love.",
            th = "พูดกับตัวเอง เหมือนที่พูด\nกับคนที่คุณรัก",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "small-wins-count",
            en = "Small wins count.\nCount them.",
            th = "ชัยชนะเล็ก ๆ ก็มีความหมาย\nนับมันด้วย",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "pace-of-others",
            en = "Someone else's pace\nsays nothing about yours.",
            th = "จังหวะของคนอื่น\nไม่ได้บอกอะไรเกี่ยวกับคุณ",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "tender-is-strong",
            en = "Being tender with yourself\nis a kind of strength.",
            th = "การอ่อนโยนกับตัวเอง\nก็คือความแข็งแกร่งแบบหนึ่ง",
            categoryId = "self-worth",
        ),
        Affirmation(
            id = "no-verdict-tonight",
            en = "You are allowed to end the day\nwithout a verdict.",
            th = "คุณจบวันได้\nโดยไม่ต้องตัดสินตัวเอง",
            categoryId = "self-worth", daypart = Daypart.NIGHT,
        ),

        // boundaries
        Affirmation(
            id = "peace-no-explanation",
            en = "Your peace does not\nneed an explanation.",
            th = "ความสงบของคุณ\nไม่ต้องการคำอธิบาย",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "door-you-close",
            en = "A boundary is a door\nyou choose to close.",
            th = "ขอบเขตคือประตู\nที่คุณเลือกจะปิดเอง",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "energy-is-finite",
            en = "Your energy is finite.\nSpend it like it matters.",
            th = "พลังของคุณมีจำกัด\nใช้มันกับสิ่งที่สำคัญ",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "not-your-emergency",
            en = "Not every urgency\nis yours to carry.",
            th = "ไม่ใช่ทุกความเร่งด่วน\nที่คุณต้องแบก",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "kind-and-firm",
            en = "You can be kind\nand still be firm.",
            th = "คุณใจดีได้\nโดยไม่ต้องใจอ่อน",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "distance-kindest-answer",
            en = "Sometimes distance\nis the kindest answer.",
            th = "บางครั้งระยะห่าง\nคือคำตอบที่อ่อนโยนที่สุด",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "protect-your-quiet",
            en = "You do not need permission\nto protect your quiet.",
            th = "คุณไม่ต้องขออนุญาตใคร\nเพื่อปกป้องความสงบของตัวเอง",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "leave-the-table",
            en = "You may leave any table\nthat costs you yourself.",
            th = "คุณลุกจากวงไหนก็ได้\nที่ต้องแลกด้วยตัวตนของคุณ",
            categoryId = "boundaries",
        ),
        Affirmation(
            id = "world-can-wait",
            en = "The world can wait\nuntil morning.",
            th = "โลกรอได้\nจนถึงเช้า",
            categoryId = "boundaries", daypart = Daypart.NIGHT,
        ),

        // love
        Affirmation(
            id = "love-is-consistent",
            en = "Real love is rarely loud.\nIt is consistent.",
            th = "รักแท้ไม่ค่อยเสียงดัง\nแต่สม่ำเสมอ",
            categoryId = "love",
        ),
        Affirmation(
            id = "arrive-whole",
            en = "Come to love whole,\nnot to be completed.",
            th = "มาหารักทั้งที่เต็มอยู่แล้ว\nไม่ใช่เพื่อให้ใครมาเติม",
            categoryId = "love",
        ),
        Affirmation(
            id = "quiet-together",
            en = "Choose the person\nyou can be quiet with.",
            th = "เลือกคนที่คุณ\nอยู่เงียบ ๆ ด้วยกันได้",
            categoryId = "love",
        ),
        Affirmation(
            id = "teach-others-care",
            en = "How you treat yourself\nteaches others how to.",
            th = "วิธีที่คุณดูแลตัวเอง\nคือบทเรียนให้คนอื่นดูแลคุณ",
            categoryId = "love",
        ),
        Affirmation(
            id = "attention-is-love",
            en = "Attention is the oldest\nlanguage of love.",
            th = "ความใส่ใจคือภาษารัก\nที่เก่าแก่ที่สุด",
            categoryId = "love",
        ),
        Affirmation(
            id = "missing-is-proof",
            en = "Missing someone is proof\nthe love was real.",
            th = "ความคิดถึงคือหลักฐาน\nว่ารักนั้นมีจริง",
            categoryId = "love",
        ),
        Affirmation(
            id = "well-repaired-love",
            en = "Strong love is not unbroken.\nIt is well-repaired.",
            th = "รักที่แข็งแรงไม่ใช่รักที่ไม่เคยร้าว\nแต่คือรักที่ซ่อมกันเป็น",
            categoryId = "love",
        ),
        Affirmation(
            id = "loved-at-ordinary-pace",
            en = "Let yourself be loved\nat an ordinary pace.",
            th = "ปล่อยให้ตัวเองถูกรัก\nในจังหวะธรรมดา ๆ",
            categoryId = "love",
        ),
        Affirmation(
            id = "kindness-at-home",
            en = "Kindness given at home\ncounts double.",
            th = "ความอ่อนโยนที่ให้คนใกล้ตัว\nมีค่าสองเท่า",
            categoryId = "love",
        ),

        // focus-work
        Affirmation(
            id = "one-honest-hour",
            en = "One honest hour beats\na scattered afternoon.",
            th = "หนึ่งชั่วโมงที่จดจ่อ\nชนะทั้งบ่ายที่ฟุ้งซ่าน",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "begin-badly",
            en = "Begin badly if you must.\nJust begin.",
            th = "เริ่มแบบไม่สมบูรณ์ก็ได้\nขอแค่ได้เริ่ม",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "finished-teaches",
            en = "Finished teaches more\nthan perfect ever will.",
            th = "งานที่เสร็จสอนคุณ\nมากกว่างานที่สมบูรณ์แบบ",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "busy-fog-focus-path",
            en = "Busy is a fog.\nFocus is a path.",
            th = "ความยุ่งคือหมอก\nสมาธิคือทาง",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "attention-currency",
            en = "Your attention is\nthe day's true currency.",
            th = "ความสนใจของคุณ\nคือเงินตราที่แท้จริงของวัน",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "stepping-away-craft",
            en = "Stepping away\nis part of the craft.",
            th = "การถอยออกมาพัก\nคือส่วนหนึ่งของฝีมือ",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "only-next-step",
            en = "You do not need the whole path,\nonly the next step.",
            th = "คุณไม่ต้องเห็นทางทั้งเส้น\nแค่ก้าวถัดไปก็พอ",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "clear-one-corner",
            en = "Clear one corner.\nThe mind follows.",
            th = "เคลียร์มุมเล็ก ๆ หนึ่งมุม\nแล้วใจจะตามมา",
            categoryId = "focus-work",
        ),
        Affirmation(
            id = "work-ends-you-continue",
            en = "Work ends. You continue.\nDo not confuse the two.",
            th = "งานมีเวลาจบ แต่คุณไปต่อ\nอย่าสับสนสองสิ่งนี้",
            categoryId = "focus-work", daypart = Daypart.NIGHT,
        ),

        // gratitude
        Affirmation(
            id = "ordinary-fortune",
            en = "An ordinary day\nis a quiet fortune.",
            th = "วันธรรมดา ๆ\nคือโชคลาภที่เงียบงัน",
            categoryId = "gratitude",
        ),
        Affirmation(
            id = "body-carried-you",
            en = "Your body carried you\nthrough every hard day so far.",
            th = "ร่างกายนี้พาคุณผ่าน\nทุกวันที่ยากมาแล้ว",
            categoryId = "gratitude",
        ),
        Affirmation(
            id = "name-three-things",
            en = "Name three small things.\nWatch the day soften.",
            th = "ลองนึกถึงสิ่งดี ๆ สามอย่าง\nแล้ววันจะอ่อนโยนลง",
            categoryId = "gratitude", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "enough-on-the-table",
            en = "There is enough\non your table tonight.",
            th = "สิ่งที่มีอยู่บนโต๊ะคืนนี้\nก็เพียงพอแล้ว",
            categoryId = "gratitude", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "someones-hope",
            en = "Someone once hoped\nfor the life you live.",
            th = "ชีวิตที่คุณใช้อยู่ตอนนี้\nคือความหวังของใครบางคน",
            categoryId = "gratitude",
        ),
        Affirmation(
            id = "size-of-a-breath",
            en = "Gratitude begins\nat the size of a breath.",
            th = "ความขอบคุณเริ่มต้นได้\nแค่ขนาดหนึ่งลมหายใจ",
            categoryId = "gratitude",
        ),
        Affirmation(
            id = "count-what-remains",
            en = "Count what remains,\nnot what is missing.",
            th = "นับสิ่งที่ยังอยู่\nไม่ใช่สิ่งที่หายไป",
            categoryId = "gratitude",
        ),
        Affirmation(
            id = "every-season-kept-you",
            en = "Every season kept you.\nThis one will too.",
            th = "ทุกฤดูที่ผ่านมาดูแลคุณไว้\nฤดูนี้ก็เช่นกัน",
            categoryId = "gratitude",
        ),
        Affirmation(
            id = "thank-who-stayed",
            en = "The people who stayed\nare worth a quiet thank you.",
            th = "คนที่ยังอยู่ข้าง ๆ\nคู่ควรกับคำขอบคุณเงียบ ๆ",
            categoryId = "gratitude",
        ),

        // rest
        Affirmation(
            id = "rest-not-earned",
            en = "Rest does not need\nto be earned.",
            th = "การพักผ่อน\nไม่ต้องแลกมาด้วยอะไร",
            categoryId = "rest",
        ),
        Affirmation(
            id = "tired-is-information",
            en = "Tired is information,\nnot weakness.",
            th = "ความเหนื่อยคือสัญญาณ\nไม่ใช่ความอ่อนแอ",
            categoryId = "rest",
        ),
        Affirmation(
            id = "doing-nothing-vital",
            en = "Doing nothing\nis doing something vital.",
            th = "การไม่ทำอะไรเลย\nก็คือการทำสิ่งสำคัญอยู่",
            categoryId = "rest",
        ),
        Affirmation(
            id = "rest-before-empty",
            en = "Rest before you are empty.\nThat is wisdom, not laziness.",
            th = "พักก่อนที่จะหมดแรง\nนั่นคือปัญญา ไม่ใช่ความขี้เกียจ",
            categoryId = "rest",
        ),
        Affirmation(
            id = "evening-be-slow",
            en = "Let the evening be slow.\nYou have done the day.",
            th = "ปล่อยให้ค่ำคืนช้าลง\nคุณผ่านวันนี้มาแล้ว",
            categoryId = "rest", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "rest-not-on-screen",
            en = "Real rest is rarely\nfound on a screen.",
            th = "การพักที่แท้จริง\nแทบไม่เคยอยู่บนหน้าจอ",
            categoryId = "rest",
        ),
        Affirmation(
            id = "ten-quiet-minutes",
            en = "Ten quiet minutes\ncan return a whole hour.",
            th = "สิบนาทีที่เงียบสงบ\nคืนพลังให้ได้ทั้งชั่วโมง",
            categoryId = "rest",
        ),
        Affirmation(
            id = "sea-rests-between-waves",
            en = "Even the sea\nrests between waves.",
            th = "แม้แต่ทะเล\nยังพักระหว่างคลื่น",
            categoryId = "rest",
        ),
        Affirmation(
            id = "only-task-soften",
            en = "Tonight, your only task\nis to soften.",
            th = "คืนนี้ หน้าที่เดียวของคุณ\nคือผ่อนลง",
            categoryId = "rest", daypart = Daypart.NIGHT,
        ),

        // healing
        Affirmation(
            id = "healing-circles-back",
            en = "Healing circles back.\nThat is still forward.",
            th = "การเยียวยาเดินวนบ้าง\nแต่นั่นก็ยังคือการไปต่อ",
            categoryId = "healing",
        ),
        Affirmation(
            id = "scar-learned-to-close",
            en = "A scar is proof\nthe skin learned to close.",
            th = "รอยแผลเป็นคือหลักฐาน\nว่าร่างกายเรียนรู้ที่จะสมาน",
            categoryId = "healing",
        ),
        Affirmation(
            id = "grief-own-calendar",
            en = "Grief keeps its own calendar.\nLet it.",
            th = "ความเศร้ามีปฏิทินของมันเอง\nปล่อยให้เป็นไป",
            categoryId = "healing",
        ),
        Affirmation(
            id = "not-the-hands",
            en = "You are not the hands\nthat hurt you.",
            th = "คุณไม่ใช่มือ\nที่เคยทำร้ายคุณ",
            categoryId = "healing",
        ),
        Affirmation(
            id = "next-chapter-understands",
            en = "Some chapters are only\nunderstood from the next one.",
            th = "บางบทของชีวิต เข้าใจได้\nก็ต่อเมื่ออ่านบทถัดไป",
            categoryId = "healing",
        ),
        Affirmation(
            id = "beginning-again",
            en = "Beginning again\nis not starting over.",
            th = "การเริ่มใหม่อีกครั้ง\nไม่ใช่การนับหนึ่งใหม่",
            categoryId = "healing",
        ),
        Affirmation(
            id = "gentle-with-then",
            en = "Be gentle with the person\nyou had to be back then.",
            th = "อ่อนโยนกับตัวเอง\nในวันที่ต้องเป็นแบบนั้น",
            categoryId = "healing",
        ),
        Affirmation(
            id = "quiet-days-mend",
            en = "Quiet days mend things\nthat loud ones broke.",
            th = "วันที่เงียบสงบ ซ่อมแซมสิ่งที่\nวันอันวุ่นวายทำพัง",
            categoryId = "healing",
        ),
        Affirmation(
            id = "time-with-kindness",
            en = "Time alone does not heal.\nTime with kindness does.",
            th = "เวลาอย่างเดียวไม่ได้เยียวยา\nเวลาที่มาพร้อมความอ่อนโยนต่างหาก",
            categoryId = "healing",
        ),

        // courage
        Affirmation(
            id = "asking-is-braver",
            en = "Asking is braver\nthan pretending to know.",
            th = "การกล้าถาม กล้าหาญกว่า\nการแกล้งทำเป็นรู้",
            categoryId = "courage",
        ),
        Affirmation(
            id = "whisper-volume-brave",
            en = "Do one brave thing\nat whisper volume.",
            th = "ทำสิ่งกล้าหาญหนึ่งอย่าง\nในระดับเสียงกระซิบ",
            categoryId = "courage",
        ),
        Affirmation(
            id = "fear-does-not-steer",
            en = "Fear may ride along.\nIt does not steer.",
            th = "ความกลัวนั่งมาด้วยได้\nแต่ห้ามจับพวงมาลัย",
            categoryId = "courage",
        ),
        Affirmation(
            id = "start-scared",
            en = "You can start scared.\nMost good things do.",
            th = "เริ่มทั้งที่กลัวก็ได้\nสิ่งดี ๆ ส่วนใหญ่เริ่มแบบนั้น",
            categoryId = "courage",
        ),
        Affirmation(
            id = "truth-said-softly",
            en = "Telling the truth softly\nstill counts as courage.",
            th = "การพูดความจริงอย่างนุ่มนวล\nก็นับเป็นความกล้า",
            categoryId = "courage",
        ),
        Affirmation(
            id = "braver-entrance",
            en = "Returning after failing\nis the braver entrance.",
            th = "การกลับมาหลังล้มเหลว\nคือการก้าวเข้ามาที่กล้าหาญกว่า",
            categoryId = "courage",
        ),
        Affirmation(
            id = "standing-still-stance",
            en = "Standing still in the storm\nis also a stance.",
            th = "การยืนนิ่งกลางพายุ\nก็คือจุดยืนแบบหนึ่ง",
            categoryId = "courage",
        ),
        Affirmation(
            id = "courage-gets-up",
            en = "Some courage looks like\ngetting up on time.",
            th = "ความกล้าบางแบบ หน้าตาเหมือน\nการลุกจากที่นอนตรงเวลา",
            categoryId = "courage", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "future-self-thankful",
            en = "Somewhere ahead, you are\nthankful you began today.",
            th = "ตัวคุณในวันข้างหน้า\nกำลังขอบคุณที่วันนี้คุณเริ่ม",
            categoryId = "courage",
        ),

        // nights
        Affirmation(
            id = "rest-in-the-night",
            en = "You do not have to solve\nthe night. Only rest in it.",
            th = "คุณไม่ต้องหาคำตอบให้ค่ำคืน\nแค่พักอยู่ในนั้นก็พอ",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "tomorrows-problems",
            en = "Tomorrow's problems\nkeep until tomorrow.",
            th = "ปัญหาของพรุ่งนี้\nเก็บไว้ถึงพรุ่งนี้ได้",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "leave-replay-unwatched",
            en = "You may leave the replay\nunwatched tonight.",
            th = "คืนนี้ไม่ต้องเปิดฉายซ้ำ\nเรื่องราวของวันก็ได้",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "lay-the-body-down",
            en = "Lay the body down.\nThe mind will learn from it.",
            th = "วางร่างกายลงก่อน\nแล้วใจจะเรียนรู้ตาม",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "dark-is-soft",
            en = "The dark is not empty.\nIt is soft.",
            th = "ความมืดไม่ได้ว่างเปล่า\nมันอ่อนนุ่ม",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "unfinished-place-to-sleep",
            en = "Unfinished is a place\nto sleep, too.",
            th = "สิ่งที่ยังไม่เสร็จ\nก็เป็นที่ให้หลับได้เหมือนกัน",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "forgive-todays-edges",
            en = "Forgive today\nits rough edges.",
            th = "ให้อภัยวันนี้\nในความไม่เรียบร้อยของมัน",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "enough-for-now",
            en = "Night is how the world\nsays: enough for now.",
            th = "กลางคืนคือวิธีที่โลกบอกว่า\nพอแค่นี้ก่อน",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "sleep-is-trust",
            en = "Sleep is an act of trust\nthat morning will come.",
            th = "การนอนหลับคือความเชื่อใจ\nว่าเช้าวันใหม่จะมาถึง",
            categoryId = "nights", daypart = Daypart.NIGHT,
        ),

        // manifest
        Affirmation(
            id = "name-it-quietly",
            en = "Name what you want\nquietly, and mean it.",
            th = "เอ่ยถึงสิ่งที่ต้องการ\nเบา ๆ แต่หมายความอย่างนั้นจริง ๆ",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "learning-your-name",
            en = "What is meant for you\nis already learning your name.",
            th = "สิ่งที่ใช่สำหรับคุณ\nกำลังเรียนรู้ชื่อของคุณอยู่",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "make-room-first",
            en = "Make room first.\nGood things need somewhere to land.",
            th = "เคลียร์ที่ว่างไว้ก่อน\nสิ่งดี ๆ ต้องการที่ลงจอด",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "answer-already-yes",
            en = "Live today like the answer\nis already yes.",
            th = "ใช้ชีวิตวันนี้ราวกับคำตอบ\nคือใช่ไปแล้ว",
            categoryId = "manifest", daypart = Daypart.MORNING,
        ),
        Affirmation(
            id = "picture-then-step",
            en = "Picture it clearly.\nThen take one ordinary step.",
            th = "เห็นภาพให้ชัด\nแล้วก้าวหนึ่งก้าวธรรมดา ๆ",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "dream-would-recognize",
            en = "Grow into the person\nyour dream would recognize.",
            th = "เติบโตเป็นคนที่\nความฝันของคุณจำได้",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "small-yeses",
            en = "Notice the small yeses.\nThey are the path.",
            th = "สังเกตคำตอบรับเล็ก ๆ\nนั่นแหละคือเส้นทาง",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "speak-future-gently",
            en = "Speak about your future\nthe way you want it to feel.",
            th = "พูดถึงอนาคต\nแบบเดียวกับที่อยากให้มันรู้สึก",
            categoryId = "manifest",
        ),
        Affirmation(
            id = "what-you-water",
            en = "What you give attention to\nlearns to grow.",
            th = "สิ่งที่คุณรดน้ำด้วยความใส่ใจ\nย่อมเรียนรู้ที่จะเติบโต",
            categoryId = "manifest",
        ),

        // business
        Affirmation(
            id = "noise-becomes-optional",
            en = "Build so well that\nnoise becomes optional.",
            th = "สร้างให้ดีจนไม่จำเป็น\nต้องเสียงดัง",
            categoryId = "business",
        ),
        Affirmation(
            id = "serve-one-deeply",
            en = "Serve one person deeply.\nScale comes later.",
            th = "ดูแลลูกค้าหนึ่งคนให้ลึกซึ้ง\nการเติบโตค่อยตามมา",
            categoryId = "business",
        ),
        Affirmation(
            id = "boring-consistency",
            en = "Most empires are built\non boring consistency.",
            th = "อาณาจักรส่วนใหญ่สร้างจาก\nความสม่ำเสมอที่แสนธรรมดา",
            categoryId = "business",
        ),
        Affirmation(
            id = "complaint-open-door",
            en = "Every complaint you hear\nis a door left open.",
            th = "ทุกเสียงบ่นที่ได้ยิน\nคือประตูที่ยังเปิดรออยู่",
            categoryId = "business",
        ),
        Affirmation(
            id = "patience-strategy",
            en = "Patience is a strategy\nmost competitors cannot afford.",
            th = "ความอดทนคือกลยุทธ์\nที่คู่แข่งส่วนใหญ่จ่ายไม่ไหว",
            categoryId = "business",
        ),
        Affirmation(
            id = "small-bets",
            en = "Make small bets often.\nProtect the downside.",
            th = "ลงเดิมพันเล็ก ๆ บ่อย ๆ\nและกันขาลงไว้เสมอ",
            categoryId = "business",
        ),
        Affirmation(
            id = "rested-founder",
            en = "A rested founder\nsees further.",
            th = "เจ้าของกิจการที่ได้พัก\nมองได้ไกลกว่า",
            categoryId = "business", daypart = Daypart.NIGHT,
        ),
        Affirmation(
            id = "trust-compounds",
            en = "Trust compounds\nfaster than capital.",
            th = "ความไว้ใจทบต้น\nเร็วกว่าเงินทุน",
            categoryId = "business",
        ),
        Affirmation(
            id = "harder-to-replace",
            en = "End each week\na little harder to replace.",
            th = "จบแต่ละสัปดาห์ให้ตัวเอง\nถูกแทนที่ยากขึ้นอีกนิด",
            categoryId = "business",
        ),

        // motivation
        Affirmation(
            id = "momentum-is-built",
            en = "Momentum is built,\nnever found.",
            th = "โมเมนตัมคือสิ่งที่สร้างขึ้น\nไม่ใช่สิ่งที่บังเอิญเจอ",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "five-minutes-first",
            en = "Give it five minutes.\nMost walls are doors.",
            th = "ให้เวลามันสักห้านาที\nกำแพงส่วนใหญ่ที่จริงคือประตู",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "discipline-remembers",
            en = "Discipline is remembering\nwhat you asked for.",
            th = "วินัยคือการจำได้ว่า\nตัวเองขออะไรไว้",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "slow-days-count",
            en = "Slow days still count.\nShowing up is the win.",
            th = "วันที่เชื่องช้าก็ยังนับ\nแค่มาถึงก็ชนะแล้ว",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "assembled-from-afternoons",
            en = "Tomorrow is assembled\nfrom afternoons like this one.",
            th = "พรุ่งนี้ถูกประกอบขึ้น\nจากบ่ายวันแบบนี้เอง",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "one-more-honest-push",
            en = "One more honest push,\nthen rest without guilt.",
            th = "ตั้งใจผลักอีกหนึ่งครั้ง\nแล้วพักโดยไม่ต้องรู้สึกผิด",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "race-yesterday-gently",
            en = "Race the person you were\nyesterday. Gently.",
            th = "แข่งกับตัวเองของเมื่อวาน\nอย่างอ่อนโยน",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "pace-is-fine",
            en = "Your pace is fine.\nJust do not stop the walk.",
            th = "จังหวะของคุณดีอยู่แล้ว\nแค่อย่าหยุดเดิน",
            categoryId = "motivation",
        ),
        Affirmation(
            id = "clean-scoreboard",
            en = "Every morning is\na clean scoreboard.",
            th = "ทุกเช้าคือกระดานคะแนน\nที่ถูกลบใหม่",
            categoryId = "motivation", daypart = Daypart.MORNING,
        ),
    )

    fun byId(id: String?): Affirmation? = All.firstOrNull { it.id == id }
}
