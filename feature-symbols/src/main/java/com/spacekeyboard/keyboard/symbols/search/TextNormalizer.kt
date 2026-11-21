package com.spacekeyboard.keyboard.symbols.search

object TextNormalizer {
    private const val NONE = '\u0000'
    private const val SEPARATORS = "-_|,"

    fun fold(input: String): String {
        val out = StringBuilder(input.length)
        for (ch in input.lowercase()) {
            val mapped = foldChar(ch)
            if (mapped != NONE) out.append(mapped)
        }
        return out.toString()
    }

    fun normalize(input: String): String {
        val folded = fold(input)
        val out = StringBuilder(folded.length)
        for (ch in folded) out.append(if (ch in SEPARATORS) ' ' else ch)
        return out.toString()
    }

    fun tokens(input: String): List<String> = normalize(input).split(' ').filter { it.isNotEmpty() }

    fun foldedTokens(input: String): List<String> = fold(input).split(' ').filter { it.isNotEmpty() }

    private fun foldChar(ch: Char): Char = when (ch.code) {
        in 0x064B..0x065F, 0x0670, 0x0640 -> NONE
        0x0622, 0x0623, 0x0625, 0x0671 -> '\u0627'
        0x0649, 0x0626, 0x06CC -> '\u064A'
        0x0629 -> '\u0647'
        0x0624 -> '\u0648'
        0x06A9 -> '\u0643'
        in 0x0660..0x0669 -> '0' + (ch.code - 0x0660)
        in 0x06F0..0x06F9 -> '0' + (ch.code - 0x06F0)
        else -> ch
    }
}
