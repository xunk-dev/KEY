package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel

class SearchKeyRouter(
    private val panel: SymbolPanelModel,
    private val emoji: EmojiController,
    private val clip: ClipboardController,
) {
    fun route(key: KeySpec): Boolean =
        (emoji.snapshot.mode == EmojiMode.SEARCH && toEmoji(key)) ||
            (panel.snapshot.searching && toSymbols(key)) ||
            (clip.snapshot.mode == ClipMode.NAMING && toClip(key))

    private fun toSymbols(key: KeySpec): Boolean = when (key.action) {
        KeyAction.COMMIT -> true.also { panel.type(key.label) }
        KeyAction.SPACE -> true.also { panel.type(" ") }
        KeyAction.DELETE, KeyAction.DELETE_WORD -> true.also { panel.backspace() }
        KeyAction.ENTER -> true
        else -> false
    }

    private fun toEmoji(key: KeySpec): Boolean = when (key.action) {
        KeyAction.COMMIT -> true.also { emoji.type(key.label) }
        KeyAction.SPACE -> true.also { emoji.type(" ") }
        KeyAction.DELETE, KeyAction.DELETE_WORD -> true.also { emoji.backspace() }
        KeyAction.ENTER -> true.also { emoji.stopSearch() }
        else -> false
    }

    private fun toClip(key: KeySpec): Boolean = when (key.action) {
        KeyAction.COMMIT -> true.also { clip.type(key.label) }
        KeyAction.SPACE -> true.also { clip.type(" ") }
        KeyAction.DELETE, KeyAction.DELETE_WORD -> true.also { clip.backspace() }
        KeyAction.ENTER -> true.also { clip.confirmName() }
        else -> false
    }
}
