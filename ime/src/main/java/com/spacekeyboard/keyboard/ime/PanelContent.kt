package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.symbols.state.rememberPanelState
import com.spacekeyboard.keyboard.symbols.ui.SymbolPanel

@Composable
fun PanelContent(
    theme: ThemeController,
    model: SymbolPanelModel,
    onInsert: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val palette = rememberThemeSnapshot(theme).palette
    val state = rememberPanelState(model)
    if (!state.open) return
    SpaceTheme(mode = SpaceThemeMode.Dark) {
        SymbolPanel(state, palette, model, onInsert, onDelete)
    }
}
