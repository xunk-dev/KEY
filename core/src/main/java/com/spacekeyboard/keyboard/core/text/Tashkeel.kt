package com.spacekeyboard.keyboard.core.text

object Tashkeel {
    const val TATWEEL = "\u0640"
    const val CHIP = "\u0640\u064E"

    val MARKS: List<String> = listOf(
        "\u0651", "\u064E", "\u064F", "\u0650", "\u0652", "\u064B", "\u064C", "\u064D",
    )

    val LABELS: List<String> = MARKS.map { TATWEEL + it }

    fun canMark(before: String): Boolean {
        val last = before.lastOrNull() ?: return false
        return isArabicLetter(last) || isMark(last)
    }

    fun isMark(ch: Char): Boolean = ch in FIRST_MARK..LAST_MARK || ch == SUPERSCRIPT_ALEF

    fun isArabicLetter(ch: Char): Boolean = ch in FIRST_LETTER..LAST_LETTER

    private const val FIRST_LETTER = '\u0621'
    private const val LAST_LETTER = '\u064A'
    private const val FIRST_MARK = '\u064B'
    private const val LAST_MARK = '\u0652'
    private const val SUPERSCRIPT_ALEF = '\u0670'
}
