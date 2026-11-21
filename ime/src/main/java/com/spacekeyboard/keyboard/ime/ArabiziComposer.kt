package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.core.arabizi.Arabizi

internal class ArabiziComposer {
    private val buffer = StringBuilder()
    private var shown = ""

    fun takes(label: String): Boolean = label.length == 1 && Arabizi.accepts(label[0])

    fun type(target: InputConnection, label: String) {
        syncWith(target)
        buffer.append(label.lowercase())
        render(target)
    }

    fun delete(target: InputConnection): Boolean {
        syncWith(target)
        if (buffer.isEmpty()) return false
        buffer.setLength(buffer.length - 1)
        render(target)
        return true
    }

    fun finish(target: InputConnection) {
        if (buffer.isEmpty()) return
        target.finishComposingText()
        reset()
    }

    fun reset() {
        buffer.setLength(0)
        shown = ""
    }

    private fun render(target: InputConnection) {
        if (buffer.isEmpty()) {
            target.commitText("", 1)
            shown = ""
            return
        }
        shown = Arabizi.convert(buffer.toString())
        target.setComposingText(shown, 1)
    }

    private fun syncWith(target: InputConnection) {
        if (buffer.isEmpty()) return
        val tail = target.getTextBeforeCursor(shown.length, 0)?.toString()
        if (tail == shown) return
        target.finishComposingText()
        reset()
    }
}
