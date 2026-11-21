package com.spacekeyboard.keyboard.ime

import android.os.SystemClock
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.autofix.AutoFixController
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore
import com.spacekeyboard.keyboard.decorate.DecorController

class KeyTyper(
    private val decor: DecorController,
    private val autofix: AutoFixController,
    private val connection: () -> InputConnection?,
    private val imeOptions: () -> Int,
    private val sendKey: (Int) -> Unit,
    private val rawKeys: () -> Boolean = { false },
    private val clock: () -> Long = SystemClock::uptimeMillis,
) {
    val extras = TypingExtras()
    private val composer = ArabiziComposer()
    private val keyMap by lazy { KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD) }
    private var lastSpace = 0L

    fun commit(label: String, zone: Int) {
        val target = connection() ?: return
        lastSpace = 0L
        if (rawKeys()) return insertRaw(target, label)
        if (extras.arabizi && decor.activeStyle() == null && composer.takes(label)) {
            autofix.reset()
            return composer.type(target, label)
        }
        composer.finish(target)
        if (KeyLabels.isMarkOnly(label)) {
            autofix.reset()
            target.commitText(label, 1)
            return
        }
        val styled = decorated(target, label)
        if (styled == null) {
            label.forEach { autofix.type(it.toString(), zone, InputConnectionField(target)) }
        } else {
            autofix.reset()
            target.commitText(styled, 1)
        }
    }

    fun space() {
        val target = connection() ?: return
        if (rawKeys()) return insertRaw(target, " ")
        composer.finish(target)
        if (expandShortcut(target) || replaceDoubleSpace(target)) return
        val styled = decorated(target, " ")
        if (styled == null) {
            autofix.space(InputConnectionField(target))
        } else {
            autofix.reset()
            target.commitText(styled, 1)
        }
        lastSpace = clock()
    }

    fun delete() {
        val target = connection() ?: return
        lastSpace = 0L
        when {
            rawKeys() -> deleteRaw()
            composer.delete(target) -> autofix.reset()
            decor.activeStyle() == null -> autofix.delete(InputConnectionField(target))
            else -> {
                autofix.reset()
                TextDeletion.deleteLast(target)
            }
        }
    }

    fun deleteWord() {
        val target = connection() ?: return
        lastSpace = 0L
        composer.finish(target)
        autofix.reset()
        if (rawKeys()) sendKey(KeyEvent.KEYCODE_DEL) else TextDeletion.deleteWord(target)
    }

    fun replaceLast(text: String) {
        val target = connection() ?: return
        lastSpace = 0L
        composer.finish(target)
        autofix.reset()
        if (rawKeys()) return deleteRaw()
        val before = target.getTextBeforeCursor(text.length, 0)?.toString().orEmpty()
        if (before == text) target.deleteSurroundingText(text.length, 0)
    }

    fun enter() {
        val target = connection() ?: return
        lastSpace = 0L
        composer.finish(target)
        val plain = rawKeys() || decor.activeStyle() != null
        if (plain) autofix.reset() else autofix.beforeEnter(InputConnectionField(target))
        sendEnter(target)
    }

    fun ready(): Boolean = connection() != null

    fun insert(text: String) {
        val target = connection() ?: return
        lastSpace = 0L
        composer.finish(target)
        if (rawKeys()) insertRaw(target, text) else target.commitText(text, 1)
    }

    fun deletePlain() {
        val target = connection() ?: return
        lastSpace = 0L
        composer.finish(target)
        if (rawKeys()) deleteRaw() else TextDeletion.deleteLast(target)
    }

    fun settle() {
        autofix.reset()
        lastSpace = 0L
        connection()?.let { composer.finish(it) }
    }

    private fun expandShortcut(target: InputConnection): Boolean {
        if (decor.activeStyle() != null || extras.shortcuts.entries.isEmpty()) return false
        val before = target.getTextBeforeCursor(ShortcutStore.MAX_TRIGGER + 1, 0)?.toString().orEmpty()
        val hit = extras.shortcuts.expansionFor(before) ?: return false
        autofix.reset()
        target.beginBatchEdit()
        target.deleteSurroundingText(hit.delete, 0)
        target.commitText(hit.insert + " ", 1)
        target.endBatchEdit()
        lastSpace = 0L
        return true
    }

    private fun replaceDoubleSpace(target: InputConnection): Boolean {
        val recent = lastSpace != 0L && clock() - lastSpace <= DOUBLE_SPACE_MS
        if (!extras.doubleSpace || !recent || decor.activeStyle() != null) return false
        val before = target.getTextBeforeCursor(2, 0)?.toString().orEmpty()
        if (before.length != 2 || before[1] != ' ' || !before[0].isLetterOrDigit()) return false
        autofix.reset()
        target.beginBatchEdit()
        target.deleteSurroundingText(1, 0)
        target.commitText(". ", 1)
        target.endBatchEdit()
        lastSpace = 0L
        return true
    }

    private fun insertRaw(target: InputConnection, text: String) {
        autofix.reset()
        val events = keyMap.getEvents(text.toCharArray())
        if (events == null) target.commitText(text, 1) else events.forEach { target.sendKeyEvent(it) }
    }

    private fun deleteRaw() {
        autofix.reset()
        sendKey(KeyEvent.KEYCODE_DEL)
    }

    private fun decorated(target: InputConnection, text: String): String? {
        val style = decor.activeStyle() ?: return null
        val before = target.getTextBeforeCursor(DECOR_WINDOW, 0)?.toString().orEmpty()
        return style.typed(before, text)
    }

    private fun sendEnter(target: InputConnection) {
        val options = imeOptions()
        val action = options and EditorInfo.IME_MASK_ACTION
        val plain = action == EditorInfo.IME_ACTION_NONE ||
            action == EditorInfo.IME_ACTION_UNSPECIFIED ||
            (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (plain) sendKey(KeyEvent.KEYCODE_ENTER) else target.performEditorAction(action)
    }

    private companion object {
        const val DECOR_WINDOW = 32
        const val DOUBLE_SPACE_MS = 450L
    }
}
