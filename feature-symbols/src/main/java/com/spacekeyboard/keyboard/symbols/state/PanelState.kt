package com.spacekeyboard.keyboard.symbols.state

import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.model.ArtEntry
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.FaceEntry
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolEntry

enum class PanelTab { SYMBOLS, FACES, ART }

object PanelMetrics {
    const val BAR_DP = 44
    const val SEARCH_DP = 100
}

data class PanelState(
    val open: Boolean = false,
    val tab: PanelTab = PanelTab.SYMBOLS,
    val category: SymbolCategory = SymbolCategory.FEATURED,
    val faceCategory: FaceCategory = FaceCategory.CLASSIC,
    val artCategory: ArtCategory = ArtCategory.HEARTS,
    val searching: Boolean = false,
    val query: String = "",
    val loaded: Boolean = false,
    val symbols: List<SymbolEntry> = emptyList(),
    val faces: List<FaceEntry> = emptyList(),
    val art: List<ArtEntry> = emptyList(),
    val recentsOpen: Boolean = false,
    val editing: Boolean = false,
    val recents: List<String> = emptyList(),
)
