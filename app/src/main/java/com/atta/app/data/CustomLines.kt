package com.atta.app.data

/**
 * The user's own lines, stored in prefs as "<id>\u0001<text>" entries. They
 * surface as ordinary [Affirmation]s (same text in both languages) so the
 * saved list and the practice queue treat them like the built-in ones.
 */
object CustomLines {

    const val CategoryId = "custom"
    private const val Sep = '\u0001'

    fun encode(id: String, text: String): String = "$id$Sep$text"

    fun parse(raw: Set<String>): List<Affirmation> =
        raw.mapNotNull { entry ->
            val split = entry.indexOf(Sep)
            if (split <= 0) return@mapNotNull null
            val text = entry.substring(split + 1)
            Affirmation(
                id = entry.substring(0, split),
                en = text,
                th = text,
                categoryId = CategoryId,
            )
        }.sortedByDescending { it.id }

    fun newId(): String = "custom-${System.currentTimeMillis()}"
}
