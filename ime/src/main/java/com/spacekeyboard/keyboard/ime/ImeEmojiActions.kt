package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.model.SkinTone
import com.spacekeyboard.keyboard.emoji.ui.EmojiActions

class ImeEmojiActions(
    private val emoji: EmojiController,
    private val connection: () -> InputConnection?,
) : EmojiActions {
    override fun onPick(text: String) {
        connection()?.commitText(text, 1)
        emoji.record(text)
    }

    override fun onDelete() {
        connection()?.let(TextDeletion::deleteLast)
    }

    override fun onKeys() = emoji.close()

    override fun onSearch() = emoji.startSearch()

    override fun onCloseSearch() = emoji.stopSearch()

    override fun onGroup(id: String) = emoji.selectGroup(id)

    override fun onTone(tone: SkinTone) = emoji.setTone(tone)

    override fun onClearRecents() = emoji.clearRecents()
}
