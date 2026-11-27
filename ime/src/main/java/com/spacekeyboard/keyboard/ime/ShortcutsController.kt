package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ShortcutsController {
    var open by mutableStateOf(false)
        private set

    var onChange: (() -> Unit)? = null

    fun toggle() = set(!open)

    fun show() = set(true)

    fun close() = set(false)

    private fun set(value: Boolean) {
        if (open == value) return
        open = value
        onChange?.invoke()
    }
}
