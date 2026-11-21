package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.symbols.state.PanelState
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel

@Composable
fun SymbolPanel(
    state: PanelState,
    palette: ThemePalette,
    model: SymbolPanelModel,
    onInsert: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp)) {
        if (state.searching) {
            SearchBar(state.query, palette, model::endSearch)
            ResultStrip(state, palette, onInsert)
        } else {
            PanelBar(state, palette, model, onDelete)
            CategoryStrip(state, palette, model)
            ResultGrid(state, palette, Modifier.weight(1f), onInsert)
        }
    }
}
