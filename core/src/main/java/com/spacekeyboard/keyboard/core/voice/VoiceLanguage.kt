package com.spacekeyboard.keyboard.core.voice

object VoiceLanguage {
    const val DEFAULT_ARABIC = "ar-SA"
    const val PERSIAN = "fa-IR"
    const val ENGLISH = "en-US"

    val arabicTags = listOf(
        "ar-SA", "ar-EG", "ar-AE", "ar-KW", "ar-QA", "ar-IQ",
        "ar-JO", "ar-LB", "ar-MA", "ar-DZ", "ar-TN",
    )

    fun arabicOrDefault(tag: String?): String = tag?.takeIf { it in arabicTags } ?: DEFAULT_ARABIC

    fun tagFor(language: String, arabicTag: String?): String = when (language) {
        "ar", "az" -> arabicOrDefault(arabicTag)
        "fa" -> PERSIAN
        else -> ENGLISH
    }

    fun extraLanguages(tag: String): List<String> = if (tag.startsWith("en")) emptyList() else listOf(ENGLISH)
}
