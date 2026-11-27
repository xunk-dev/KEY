package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.ui.HubTab
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.symbols.state.rememberPanelState
import com.spacekeyboard.keyboard.symbols.ui.SymbolPanel

@Composable
fun PanelContent(
    theme: ThemeController,
    model: SymbolPanelModel,
    emoji: EmojiController,
    images: ImagesController,
    onTab: (HubTab) -> Unit,
    onInsert: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val palette = rememberThemeSnapshot(theme).palette
    val state = rememberPanelState(model)
    val tabs = rememberHubTabs(emoji, images)
    if (!state.open) return
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        SymbolPanel(state, palette, model, tabs, onTab, onInsert, onDelete)
    }
}
