package com.spacekeyboard.keyboard.core.hardware

object HardwareLayout {
    private const val FATHA = "َ"
    private const val TANWIN_FATH = "ً"
    private const val DAMMA = "ُ"
    private const val TANWIN_DAMM = "ٌ"
    private const val KASRA = "ِ"
    private const val TANWIN_KASR = "ٍ"
    private const val SUKUN = "ْ"
    private const val ZWNJ = "‌"

    private val arabic: Map<Char, String> = build(
        "qwertyuiop[]asdfghjkl;'zxcvbnm,./`",
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د", "ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط", "ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ", "ذ"),
        "QWERTYUIOP{}ASDFGHJKL:\"ZXCVBNM<>?~",
        listOf(FATHA, TANWIN_FATH, DAMMA, TANWIN_DAMM, "لإ", "إ", "‘", "÷", "×", "؛", "<", ">", KASRA, TANWIN_KASR, "]", "[", "لأ", "أ", "ـ", "،", "/", ":", "\"", "~", SUKUN, "}", "{", "لآ", "آ", "’", ",", ".", "؟", "~"),
    )

    private val persian: Map<Char, String> = build(
        "qwertyuiop[]asdfghjkl;'zxcvbnm,./`",
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ", "ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ", "ظ", "ط", "ز", "ر", "ذ", "د", "پ", "و", ".", "/", ZWNJ),
        "CZ:<?",
        listOf("ژ", "ك", "؛", "،", "؟"),
    )

    fun translate(language: String, typed: Char): String = when (language) {
        "ar" -> arabic[typed]
        "fa" -> persian[typed]
        else -> null
    } ?: typed.toString()

    fun textFor(language: String, code: Int): String? {
        if (code <= 0 || Character.isISOControl(code)) return null
        if (maps(language) && code <= Char.MAX_VALUE.code) return translate(language, code.toChar())
        return String(Character.toChars(code))
    }

    fun maps(language: String): Boolean = language == "ar" || language == "fa"

    private fun build(plain: String, plainOut: List<String>, shifted: String, shiftedOut: List<String>): Map<Char, String> {
        require(plain.length == plainOut.size && shifted.length == shiftedOut.size)
        val map = HashMap<Char, String>()
        plain.forEachIndexed { i, ch -> map[ch] = plainOut[i] }
        shifted.forEachIndexed { i, ch -> map[ch] = shiftedOut[i] }
        return map
    }
}
