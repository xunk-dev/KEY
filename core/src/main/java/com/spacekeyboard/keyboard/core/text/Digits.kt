package com.spacekeyboard.keyboard.core.text

enum class DigitStyle(val zero: Char) {
    WESTERN('0'),
    ARABIC_INDIC('٠'),
    PERSIAN('۰'),
}

object Digits {
    fun valueOf(ch: Char): Int = when (ch) {
        in '0'..'9' -> ch - '0'
        in '٠'..'٩' -> ch - '٠'
        in '۰'..'۹' -> ch - '۰'
        else -> -1
    }

    fun isDigit(ch: Char): Boolean = valueOf(ch) >= 0

    fun styleOf(ch: Char): DigitStyle? = when (ch) {
        in '0'..'9' -> DigitStyle.WESTERN
        in '٠'..'٩' -> DigitStyle.ARABIC_INDIC
        in '۰'..'۹' -> DigitStyle.PERSIAN
        else -> null
    }

    fun styles(text: String): Set<DigitStyle> = text.mapNotNullTo(LinkedHashSet(), ::styleOf)

    fun convert(text: String, style: DigitStyle): String {
        val out = StringBuilder(text.length)
        for (ch in text) {
            val value = valueOf(ch)
            out.append(if (value < 0) ch else style.zero + value)
        }
        return out.toString()
    }

    fun targetsFor(text: String): List<DigitStyle> {
        val present = styles(text)
        if (present.isEmpty()) return emptyList()
        if (present == setOf(DigitStyle.WESTERN)) return listOf(DigitStyle.ARABIC_INDIC)
        if (present == setOf(DigitStyle.ARABIC_INDIC)) return listOf(DigitStyle.WESTERN)
        return listOf(DigitStyle.WESTERN, DigitStyle.ARABIC_INDIC)
    }
}
