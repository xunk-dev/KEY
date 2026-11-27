package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController

class SearchKeyRouter(
    private val panel: SymbolPanelModel,
    private val emoji: EmojiController,
    private val clip: ClipboardController,
    private val translate: TranslateController,
    private val snippets: SnippetController,
) {
    fun route(key: KeySpec): Boolean =
        (snippets.composing && toSnippets(key)) ||
            (emoji.snapshot.mode == EmojiMode.SEARCH && toEmoji(key)) ||
            (panel.snapshot.searching && toSymbols(key)) ||
            (clip.snapshot.mode == ClipMode.NAMING && toClip(key)) ||
            (translate.state.open && !translate.state.expanded && toTranslate(key))

    fun capturing(): Boolean =
        snippets.composing || emoji.snapshot.mode == EmojiMode.SEARCH || panel.snapshot.searching ||
            clip.snapshot.mode == ClipMode.NAMING || (translate.state.open && !translate.state.expanded)

    private fun toSnippets(key: KeySpec): Boolean = when (key.action) {
        KeyAction.COMMIT -> true.also { snippets.type(key.label) }
        KeyAction.SPACE -> true.also { snippets.type(" ") }
        KeyAction.DELETE, KeyAction.DELETE_WORD -> true.also { snippets.backspace() }
        KeyAction.ENTER -> true.also { snippets.confirm() }
        else -> false
    }

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

    private fun toTranslate(key: KeySpec): Boolean = when (key.action) {
        KeyAction.COMMIT -> true.also { translate.type(key.label) }
        KeyAction.SPACE -> true.also { translate.type(" ") }
        KeyAction.DELETE -> true.also { translate.backspace() }
        KeyAction.DELETE_WORD -> true.also { translate.deleteWord() }
        KeyAction.ENTER -> true.also { translate.submit() }
        else -> false
    }
}
