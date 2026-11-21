package com.spacekeyboard.keyboard.decorate

object ArabicJoin {
    const val TATWEEL = 'ـ'
    private const val DIACRITICS_FIRST = 'ً'
    private const val DIACRITICS_LAST = 'ْ'
    private const val DUAL = "بتثجحخسشصضطظعغفقكلمنهيئکھی"
    private const val RIGHT_ONLY = "اأإآدذرزوؤةىٱۃ"

    val lookalikes: Map<Char, Char> = mapOf('ك' to 'ک', 'ي' to 'ی', 'ا' to 'ٱ', 'ة' to 'ۃ', 'ه' to 'ھ')

    private val reverse: Map<Char, Char> = lookalikes.entries.associate { it.value to it.key }

    fun isDual(c: Char): Boolean = c in DUAL

    fun isLetter(c: Char): Boolean = c in DUAL || c in RIGHT_ONLY || c == 'ء'

    fun isDiacritic(c: Char): Boolean = c in DIACRITICS_FIRST..DIACRITICS_LAST

    fun plain(text: String): String =
        text.filter { it != TATWEEL && !isDiacritic(it) }.map { reverse[it] ?: it }.joinToString("")

    fun lastBase(before: String): Char? = before.lastOrNull { !isDiacritic(it) }
}
