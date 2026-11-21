package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.ui.EmojiActions
import com.spacekeyboard.keyboard.emoji.ui.EmojiPanel
import com.spacekeyboard.keyboard.emoji.ui.EmojiSearchPanel
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun EmojiPanelContent(theme: ThemeController, emoji: EmojiController, actions: EmojiActions) {
    val palette = rememberThemeSnapshot(theme).palette
    val state = rememberEmojiSnapshot(emoji)
    when (state.mode) {
        EmojiMode.BROWSE -> EmojiPanel(state, palette, actions)
        EmojiMode.SEARCH -> EmojiSearchPanel(state, palette, actions)
        EmojiMode.CLOSED -> Unit
    }
}
