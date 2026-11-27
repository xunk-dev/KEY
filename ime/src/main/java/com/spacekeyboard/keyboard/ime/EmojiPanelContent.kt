package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.ui.EmojiActions
import com.spacekeyboard.keyboard.emoji.ui.EmojiPanel
import com.spacekeyboard.keyboard.emoji.ui.EmojiSearchPanel
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun EmojiPanelContent(
    theme: ThemeController,
    emoji: EmojiController,
    images: ImagesController,
    layouts: LayoutController,
    actions: EmojiActions,
) {
    val palette = rememberThemeSnapshot(theme).palette
    val state = rememberEmojiSnapshot(emoji)
    val tabs = rememberHubTabs(emoji, images)
    val resultsBelow = rememberLayoutSnapshot(layouts).digitsOnTop
    when (state.mode) {
        EmojiMode.BROWSE -> EmojiPanel(state, palette, actions, tabs)
        EmojiMode.SEARCH -> EmojiSearchPanel(state, palette, actions, resultsBelow)
        EmojiMode.CLOSED -> Unit
    }
}
