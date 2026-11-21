package com.spacekeyboard.keyboard.symbols.search

import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolEntry
import com.spacekeyboard.keyboard.symbols.model.codePointList

class SymbolBook(val symbols: List<SymbolEntry>, featuredCodes: List<Int>) {
    private val byCode: Map<Int, SymbolEntry> = symbols.associateBy { it.codePoint }
    private val byCategory: Map<SymbolCategory, List<SymbolEntry>> = symbols.groupBy { it.category }
    private val haystacks: List<String> = symbols.map(::haystack)

    val featured: List<SymbolEntry> = featuredCodes.distinct().mapNotNull { byCode[it] }

    fun category(category: SymbolCategory): List<SymbolEntry> =
        if (category == SymbolCategory.FEATURED) featured else byCategory[category].orEmpty()

    fun find(codePoint: Int): SymbolEntry? = byCode[codePoint]

    fun search(raw: String, limit: Int = LIMIT): List<SymbolEntry> {
        val query = raw.trim()
        if (query.isEmpty()) return emptyList()
        return byCodeText(query) ?: bySingleCharacter(query) ?: byWords(query, limit)
    }

    private fun byCodeText(query: String): List<SymbolEntry>? {
        val match = CODE_PATTERN.matchEntire(query) ?: return null
        val prefixed = match.groupValues[1].isNotEmpty()
        val digits = match.groupValues[2]
        if (!prefixed && (digits.length < MIN_BARE_DIGITS || digits.none { it.isDigit() })) return null
        return listOfNotNull(byCode[digits.toInt(16)]).takeIf { it.isNotEmpty() }
    }

    private fun bySingleCharacter(query: String): List<SymbolEntry>? {
        val codes = query.codePointList()
        if (codes.size != 1) return null
        return listOfNotNull(byCode[codes[0]]).takeIf { it.isNotEmpty() }
    }

    private fun byWords(query: String, limit: Int): List<SymbolEntry> {
        val tokens = TextNormalizer.tokens(query)
        if (tokens.isEmpty()) return emptyList()
        val scored = ArrayList<Scored>()
        for (index in haystacks.indices) {
            val score = score(haystacks[index], tokens)
            if (score > 0) scored += Scored(index, score)
        }
        scored.sortWith(compareByDescending<Scored> { it.score }.thenBy { it.index })
        return scored.take(limit).map { symbols[it.index] }
    }

    private fun score(haystack: String, tokens: List<String>): Int {
        var total = 0
        for (token in tokens) {
            total += when {
                haystack.contains(" $token") -> WORD_START
                haystack.contains(token) -> INSIDE
                else -> return 0
            }
        }
        return total
    }

    private fun haystack(entry: SymbolEntry): String {
        val parts = listOf(
            entry.unicodeName,
            entry.english,
            entry.englishKeywords.joinToString(" "),
            entry.arabic,
            entry.arabicKeywords.joinToString(" "),
        )
        return " " + TextNormalizer.normalize(parts.joinToString(" ")) + " "
    }

    private class Scored(val index: Int, val score: Int)

    companion object {
        const val LIMIT = 300
        private const val WORD_START = 2
        private const val INSIDE = 1
        private const val MIN_BARE_DIGITS = 4
        private val CODE_PATTERN = Regex("^(u\\+|0x|\\\\u)?([0-9a-fA-F]{1,6})$", RegexOption.IGNORE_CASE)
    }
}
