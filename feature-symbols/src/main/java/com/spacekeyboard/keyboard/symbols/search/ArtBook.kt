package com.spacekeyboard.keyboard.symbols.search

import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.model.ArtEntry

class ArtBook(val pieces: List<ArtEntry>) {
    private val byCategory: Map<ArtCategory, List<ArtEntry>> = pieces.groupBy { it.category }

    fun category(category: ArtCategory): List<ArtEntry> = byCategory[category].orEmpty()

    companion object {
        val EMPTY = ArtBook(emptyList())
    }
}
