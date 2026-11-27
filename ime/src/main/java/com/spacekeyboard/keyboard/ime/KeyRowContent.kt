package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.rememberClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ui.ClipActions
import com.spacekeyboard.keyboard.clipboard.ui.ClipNameSuggestions
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.ui.EmojiActions
import com.spacekeyboard.keyboard.emoji.ui.EmojiResultsRow
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun KeyRowContent(
    theme: ThemeController,
    emoji: EmojiController,
    clip: ClipboardController,
    emojiActions: EmojiActions,
    clipActions: ClipActions,
) {
    val palette = rememberThemeSnapshot(theme).palette
    val emojiState = rememberEmojiSnapshot(emoji)
    val clipState = rememberClipSnapshot(clip)
    when {
        emojiState.mode == EmojiMode.SEARCH -> EmojiResultsRow(emojiState, palette, emojiActions)
        clipState.mode == ClipMode.NAMING -> ClipNameSuggestions(clipState, palette, clipActions)
    }
}
