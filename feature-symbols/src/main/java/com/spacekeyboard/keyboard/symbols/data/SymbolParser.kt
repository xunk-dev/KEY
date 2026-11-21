package com.spacekeyboard.keyboard.symbols.data

import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.FaceEntry
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolEntry

object SymbolParser {
    private const val SYMBOL_COLUMNS = 7
    private const val CATEGORY_COLUMNS = 3

    fun symbols(lines: List<String>): List<SymbolEntry> = lines.mapNotNull(::symbol)

    fun featured(lines: List<String>): List<Int> = lines.mapNotNull { line ->
        if (isData(line)) line.trim().toIntOrNull(16)?.takeIf { Character.isValidCodePoint(it) } else null
    }

    fun faces(lines: List<String>): List<FaceEntry> = lines.mapNotNull(::face)

    fun faceKeywords(lines: List<String>): Map<FaceCategory, String> {
        val result = HashMap<FaceCategory, String>()
        for (line in lines) {
            if (!isData(line)) continue
            val parts = line.split('\t')
            val category = FaceCategory.byId(parts[0])
            if (parts.size >= CATEGORY_COLUMNS && category != null) result[category] = parts[1] + " " + parts[2]
        }
        return result
    }

    private fun isData(line: String): Boolean = line.isNotEmpty() && !line.startsWith('#')

    private fun symbol(line: String): SymbolEntry? {
        if (!isData(line)) return null
        val parts = line.split('\t')
        if (parts.size < SYMBOL_COLUMNS) return null
        val code = parts[0].toIntOrNull(16) ?: return null
        val category = SymbolCategory.byId(parts[1]) ?: return null
        if (!Character.isValidCodePoint(code)) return null
        return SymbolEntry(code, category, parts[2], parts[3], words(parts[4]), parts[5], words(parts[6]))
    }

    private fun face(line: String): FaceEntry? {
        if (!isData(line)) return null
        val parts = line.split('\t', limit = 2)
        val category = FaceCategory.byId(parts[0])
        return if (parts.size == 2 && category != null && parts[1].isNotEmpty()) FaceEntry(category, parts[1]) else null
    }

    private fun words(raw: String): List<String> = raw.split('|').filter { it.isNotEmpty() }
}
