package com.spacekeyboard.keyboard.symbols.model

data class SymbolEntry(
    val codePoint: Int,
    val category: SymbolCategory,
    val unicodeName: String,
    val english: String,
    val englishKeywords: List<String>,
    val arabic: String,
    val arabicKeywords: List<String>,
) {
    val text: String get() = codePoint.asText()

    val hex: String get() = "U+" + codePoint.toString(16).uppercase().padStart(4, '0')

    fun label(arabicFirst: Boolean): String = when {
        arabicFirst && arabic.isNotEmpty() -> arabic
        english.isNotEmpty() -> english
        else -> unicodeName
    }
}
