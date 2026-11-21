package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.core.text.WordBoundary
import com.spacekeyboard.keyboard.emoji.model.GraphemeTail

object TextDeletion {
    fun deleteLast(connection: InputConnection) {
        if (!connection.getSelectedText(0).isNullOrEmpty()) {
            connection.commitText("", 1)
            return
        }
        val before = connection.getTextBeforeCursor(GraphemeTail.WINDOW, 0)
        val length = if (before.isNullOrEmpty()) 1 else GraphemeTail.lastLength(before)
        connection.deleteSurroundingText(length.coerceAtLeast(1), 0)
    }

    fun deleteWord(connection: InputConnection) {
        if (!connection.getSelectedText(0).isNullOrEmpty()) {
            connection.commitText("", 1)
            return
        }
        val before = connection.getTextBeforeCursor(WORD_WINDOW, 0)
        val length = if (before.isNullOrEmpty()) 1 else WordBoundary.lengthBefore(before)
        connection.deleteSurroundingText(length.coerceAtLeast(1), 0)
    }

    private const val WORD_WINDOW = 64
}
