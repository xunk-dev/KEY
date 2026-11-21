package com.spacekeyboard.keyboard.ime

import android.content.Context
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot

internal fun KeySpec.firesOnDown(): Boolean = when (action) {
    KeyAction.COMMIT -> true
    KeyAction.DELETE, KeyAction.ENTER, KeyAction.SHIFT, KeyAction.NUMBERS, KeyAction.EDIT -> true
    else -> action.isEditCommand
}

internal fun KeySpec.hasLongPress(): Boolean =
    action == KeyAction.LANGUAGE || (action == KeyAction.COMMIT && alts.isNotEmpty())

internal fun describeKey(context: Context, snapshot: LayoutSnapshot, key: KeySpec): String = when (key.action) {
    KeyAction.SPACE -> spaceDescription(context, snapshot)
    KeyAction.DELETE -> context.getString(R.string.a11y_delete)
    KeyAction.ENTER -> context.getString(R.string.a11y_enter)
    KeyAction.LANGUAGE -> context.getString(R.string.a11y_language)
    KeyAction.SHIFT -> context.getString(R.string.a11y_shift)
    KeyAction.NUMBERS -> context.getString(R.string.a11y_numbers)
    KeyAction.EXPAND -> context.getString(R.string.a11y_expand)
    KeyAction.FLIP -> context.getString(R.string.a11y_flip)
    KeyAction.EDIT -> context.getString(R.string.a11y_edit_close)
    KeyAction.LEFT -> context.getString(R.string.a11y_cursor_left)
    KeyAction.RIGHT -> context.getString(R.string.a11y_cursor_right)
    KeyAction.UP -> context.getString(R.string.a11y_cursor_up)
    KeyAction.DOWN -> context.getString(R.string.a11y_cursor_down)
    KeyAction.WORD_LEFT -> context.getString(R.string.a11y_word_left)
    KeyAction.WORD_RIGHT -> context.getString(R.string.a11y_word_right)
    KeyAction.LINE_START -> context.getString(R.string.a11y_line_start)
    KeyAction.LINE_END -> context.getString(R.string.a11y_line_end)
    KeyAction.UNDO -> context.getString(R.string.a11y_undo)
    KeyAction.REDO -> context.getString(R.string.a11y_redo)
    else -> snapshot.shown(key)
}

private fun spaceDescription(context: Context, snapshot: LayoutSnapshot): String {
    val language = snapshot.spaceLabel()
    return if (language.isEmpty()) context.getString(R.string.a11y_space) else context.getString(R.string.a11y_space_language, language)
}

internal object KeyTiming {
    const val ALT_ID = "alt"
    const val WORD_ID = "word"
    const val REPLACE_ID = "replace"
    const val SWIPE_DELETE_DP = 26f
    const val WORD_AFTER = 24
    const val REPEAT_WORD_MS = 130L
    const val LONG_PRESS_MS = 380L
    const val REPEAT_DELAY_MS = 380L
    const val REPEAT_MS = 75L
    const val REPEAT_FAST_MS = 40L
    const val FAST_AFTER = 12
    const val SWIPE_STEP_DP = 14f
    const val CANCEL_LONG_RATIO = 1.5f
}
