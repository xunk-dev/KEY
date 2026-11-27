package com.spacekeyboard.keyboard.symbols.search

import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.FaceEntry

class FaceBook(val faces: List<FaceEntry>, keywords: Map<FaceCategory, String>) {
    private val byCategory: Map<FaceCategory, List<FaceEntry>> = faces.groupBy { it.category }
    private val haystacks: List<String> = faces.map { face ->
        " " + TextNormalizer.fold(face.text) + " " + TextNormalizer.normalize(keywords[face.category].orEmpty()) + " "
    }

    fun category(category: FaceCategory): List<FaceEntry> = byCategory[category].orEmpty()

    fun search(raw: String, limit: Int = LIMIT): List<FaceEntry> {
        val tokens = TextNormalizer.foldedTokens(raw)
        if (tokens.isEmpty()) return emptyList()
        val found = ArrayList<FaceEntry>()
        for (index in haystacks.indices) {
            if (tokens.all { haystacks[index].contains(it) }) found += faces[index]
            if (found.size >= limit) break
        }
        return found
    }

    companion object {
        const val LIMIT = 300
    }
}
