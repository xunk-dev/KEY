package com.spacekeyboard.keyboard.ime

import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.ime.layout.LayoutController

internal class EditCommands(
    private val layouts: LayoutController,
    private val connection: () -> InputConnection?,
    private val clock: () -> Long = SystemClock::uptimeMillis,
) {
    fun run(action: KeyAction) {
        if (action == KeyAction.SELECT_MODE) return run { layouts.toggleSelecting() }
        val target = connection() ?: return
        when (action) {
            KeyAction.SELECT_ALL -> menu(target, android.R.id.selectAll)
            KeyAction.COPY -> menu(target, android.R.id.copy)
            KeyAction.CUT -> menu(target, android.R.id.cut)
            KeyAction.PASTE -> menu(target, android.R.id.paste)
            KeyAction.UNDO -> history(target, false)
            KeyAction.REDO -> history(target, true)
            KeyAction.LEFT -> move(target, KeyEvent.KEYCODE_DPAD_LEFT, 0)
            KeyAction.RIGHT -> move(target, KeyEvent.KEYCODE_DPAD_RIGHT, 0)
            KeyAction.UP -> move(target, KeyEvent.KEYCODE_DPAD_UP, 0)
            KeyAction.DOWN -> move(target, KeyEvent.KEYCODE_DPAD_DOWN, 0)
            KeyAction.WORD_LEFT -> move(target, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.META_CTRL_ON)
            KeyAction.WORD_RIGHT -> move(target, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.META_CTRL_ON)
            KeyAction.LINE_START -> move(target, KeyEvent.KEYCODE_MOVE_HOME, 0)
            KeyAction.LINE_END -> move(target, KeyEvent.KEYCODE_MOVE_END, 0)
            else -> Unit
        }
    }

    private fun menu(target: InputConnection, id: Int) {
        target.performContextMenuAction(id)
        layouts.clearSelecting()
    }

    private fun history(target: InputConnection, redo: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            target.performContextMenuAction(if (redo) android.R.id.redo else android.R.id.undo)
            return
        }
        val shift = if (redo) KeyEvent.META_SHIFT_ON else 0
        move(target, KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON or shift)
    }

    private fun move(target: InputConnection, code: Int, meta: Int) {
        val selecting = layouts.snapshot.selecting
        val shift = if (selecting) KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON else 0
        val state = meta or shift
        val now = clock()
        if (selecting) target.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, shift))
        target.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0, state))
        target.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, code, 0, state))
        if (selecting) target.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, meta))
    }
}
