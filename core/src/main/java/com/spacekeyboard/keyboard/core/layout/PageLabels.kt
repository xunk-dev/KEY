package com.spacekeyboard.keyboard.core.layout

object PageLabels {
    private const val ARABIC_QUESTION = "؟"
    private const val ARABIC_SEMICOLON = "؛"
    private const val ARABIC_COMMA = "،"
    private val RTL_PUNCTUATION = mapOf("?" to ARABIC_QUESTION, ";" to ARABIC_SEMICOLON, "," to ARABIC_COMMA)

    fun toNumbers(language: String): String = "123"

    fun toLetters(language: String): String = when (language) {
        "ar" -> "أبج"
        "fa" -> "ابپ"
        else -> "ABC"
    }

    fun letters(spec: LayoutSpec, language: String): LayoutSpec =
        remap(spec) { key -> if (key.action == KeyAction.NUMBERS) key.copy(label = toNumbers(language)) else key }

    fun page(spec: LayoutSpec, language: String, native: Boolean = false): LayoutSpec {
        val rtl = LayoutCatalog.isRtl(language)
        return remap(spec) { key ->
            when {
                key.action == KeyAction.NUMBERS -> key.copy(label = toLetters(language))
                key.action == KeyAction.COMMIT && isDigit(key.label) -> digit(key, language, native)
                rtl && key.action == KeyAction.COMMIT -> localPunctuation(key)
                else -> key
            }
        }
    }

    private fun isDigit(label: String) = label.length == 1 && label[0] in '0'..'9'

    private fun digit(key: KeySpec, language: String, native: Boolean): KeySpec {
        val index = if (key.label[0] == '0') 9 else key.label[0] - '1'
        val own = NumberRow.digits(language, native)[index]
        val other = NumberRow.alternates(language, native).getOrNull(index)
        return key.copy(label = own, alts = listOfNotNull(other).filter { it != own })
    }

    private fun localPunctuation(key: KeySpec): KeySpec {
        val local = RTL_PUNCTUATION[key.label] ?: return key
        return key.copy(label = local, alts = listOf(key.label) + key.alts.filter { it != local && it != key.label })
    }

    private fun remap(spec: LayoutSpec, change: (KeySpec) -> KeySpec): LayoutSpec =
        LayoutSpec(spec.id, spec.rows.map { row -> row.map(change) })
}
