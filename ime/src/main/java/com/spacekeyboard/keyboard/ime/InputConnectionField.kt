package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.autofix.TextField

internal class InputConnectionField(private val connection: InputConnection) : TextField {
    override fun before(length: Int): String =
        connection.getTextBeforeCursor(length, 0)?.toString().orEmpty()

    override fun after(length: Int): String =
        connection.getTextAfterCursor(length, 0)?.toString().orEmpty()

    override fun commit(text: String) {
        connection.commitText(text, 1)
    }

    override fun deleteBefore(length: Int) {
        connection.deleteSurroundingText(length, 0)
    }

    override fun deleteLast() = TextDeletion.deleteLast(connection)

    override fun hasSelection(): Boolean = !connection.getSelectedText(0).isNullOrEmpty()

    override fun batch(block: () -> Unit) {
        connection.beginBatchEdit()
        try {
            block()
        } finally {
            connection.endBatchEdit()
        }
    }
}
