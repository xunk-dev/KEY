package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.emoji.ui.HubTab
import com.spacekeyboard.keyboard.emoji.ui.HubTabBar
import com.spacekeyboard.keyboard.symbols.R
import com.spacekeyboard.keyboard.symbols.state.PanelState
import com.spacekeyboard.keyboard.symbols.state.PanelTab
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel

@Composable
fun SymbolPanel(
    state: PanelState,
    palette: ThemePalette,
    model: SymbolPanelModel,
    tabs: List<HubTab>,
    onTab: (HubTab) -> Unit,
    onInsert: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val insert: (String) -> Unit = {
        if (!state.recentsOpen) model.record(it)
        onInsert(it)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.searching) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp)) {
                SearchBar(state.query, palette, model::endSearch)
                ResultStrip(state, palette, insert)
            }
        } else {
            val search = if (state.tab == PanelTab.ART) null else model::startSearch
            HubTabBar(tabs, state.tab.hubTab(), palette, onTab, search, stringResource(R.string.panel_search))
            ResultGrid(state, palette, Modifier.weight(1f).padding(horizontal = 6.dp), model, insert)
            SymbolDock(state, palette, model, onDelete)
        }
    }
}

private fun PanelTab.hubTab(): HubTab = when (this) {
    PanelTab.SYMBOLS -> HubTab.SYMBOLS
    PanelTab.FACES -> HubTab.FACES
    PanelTab.ART -> HubTab.ART
}
