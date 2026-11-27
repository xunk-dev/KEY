package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.decorate.SourceKind

internal object DecorInput {
    private const val WINDOW = 32

    fun read(connection: InputConnection?): Pair<String, SourceKind> {
        val selected = connection?.getSelectedText(0)?.toString().orEmpty()
        val before = connection?.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        val word = before.takeLastWhile { !it.isWhitespace() }
        return when {
            selected.isNotEmpty() -> selected to SourceKind.SELECTION
            word.isNotEmpty() -> word to SourceKind.BEFORE
            else -> "" to SourceKind.NONE
        }
    }
}
