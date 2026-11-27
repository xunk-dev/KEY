package com.spacekeyboard.keyboard.core.text

object ScriptGuess {
    enum class Script { ARABIC, LATIN }

    const val WINDOW = 40
    private const val MIN_LETTERS = 3
    private const val DOMINANCE = 80
    private val arabicScript = setOf("ar", "fa", "az")
    private val arabicKeys = listOf("ar", "fa")
    private const val ENGLISH = "en"

    fun of(text: CharSequence): Script? {
        var arabic = 0
        var latin = 0
        for (c in text.takeLast(WINDOW)) {
            if (!c.isLetter()) continue
            if (isArabic(c)) arabic++ else if (c in 'a'..'z' || c in 'A'..'Z') latin++
        }
        val total = arabic + latin
        return when {
            total < MIN_LETTERS -> null
            arabic * 100 >= total * DOMINANCE -> Script.ARABIC
            latin * 100 >= total * DOMINANCE -> Script.LATIN
            else -> null
        }
    }

    fun layoutFor(script: Script?, current: String, enabled: List<String>): String? = when (script) {
        null -> null
        Script.ARABIC -> if (current in arabicScript) null else arabicKeys.firstOrNull { it in enabled }
        Script.LATIN -> if (current in arabicKeys) ENGLISH.takeIf { it in enabled } else null
    }

    private fun isArabic(c: Char): Boolean =
        c in '\u0600'..'\u06FF' || c in '\u0750'..'\u077F' || c in '\uFB50'..'\uFDFF' || c in '\uFE70'..'\uFEFF'
}
