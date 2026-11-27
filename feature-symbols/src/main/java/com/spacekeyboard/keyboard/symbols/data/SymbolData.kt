package com.spacekeyboard.keyboard.symbols.data

import com.spacekeyboard.keyboard.symbols.search.ArtBook
import com.spacekeyboard.keyboard.symbols.search.FaceBook
import com.spacekeyboard.keyboard.symbols.search.SymbolBook

class SymbolData(val symbols: SymbolBook, val faces: FaceBook, val art: ArtBook = ArtBook.EMPTY) {
    companion object {
        fun parse(
            symbolLines: List<String>,
            featuredLines: List<String>,
            faceLines: List<String>,
            categoryLines: List<String>,
            artLines: List<String> = emptyList(),
        ): SymbolData = SymbolData(
            SymbolBook(SymbolParser.symbols(symbolLines), SymbolParser.featured(featuredLines)),
            FaceBook(SymbolParser.faces(faceLines), SymbolParser.faceKeywords(categoryLines)),
            ArtBook(ArtParser.parse(artLines)),
        )

        val EMPTY: SymbolData = parse(emptyList(), emptyList(), emptyList(), emptyList())
    }
}
