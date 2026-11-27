package com.spacekeyboard.keyboard.ime

import android.view.KeyEvent
import com.spacekeyboard.keyboard.core.hardware.HardwareLayout
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.ime.layout.LayoutController

internal class HardwareKeys(
    private val layouts: LayoutController,
    private val press: (KeySpec) -> Unit,
    private val settle: () -> Unit,
    private val ready: () -> Boolean,
) {
    private val taken = HashSet<Int>()

    fun down(event: KeyEvent): Boolean {
        if (!ready() || !layouts.snapshot.prefs.typing.hardwareKeys) return false
        val key = specOf(event)
        if (key == null) {
            if (!KeyEvent.isModifierKey(event.keyCode)) settle()
            return false
        }
        taken += event.keyCode
        press(key)
        return true
    }

    fun up(event: KeyEvent): Boolean = taken.remove(event.keyCode)

    private fun specOf(event: KeyEvent): KeySpec? {
        if (event.isCtrlPressed || event.isAltPressed || event.isMetaPressed) return null
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DEL -> plain("hw_delete", KeyAction.DELETE)
            KeyEvent.KEYCODE_SPACE -> plain("hw_space", KeyAction.SPACE)
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> plain("hw_enter", KeyAction.ENTER)
            else -> printable(event)
        }
    }

    private fun printable(event: KeyEvent): KeySpec? {
        val language = layouts.snapshot.language
        val mapped = HardwareLayout.maps(language)
        val meta = if (mapped) event.metaState and KeyEvent.META_CAPS_LOCK_ON.inv() else event.metaState
        val text = HardwareLayout.textFor(language, event.getUnicodeChar(meta)) ?: return null
        return KeySpec(HARDWARE_ID, text, KeyAction.COMMIT, 1f, false)
    }

    private fun plain(id: String, action: KeyAction) = KeySpec(id, "", action, 1f, false)

    private companion object {
        const val HARDWARE_ID = "hw_key"
    }
}
