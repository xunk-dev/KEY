package com.spacekeyboard.keyboard.ime

import android.graphics.Canvas
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconCanvas

internal class KeyGlyph(val icon: SpaceIcon, val mirror: Boolean = false)

class KeyIcons {
    private val painter = SpaceIconCanvas()

    fun handles(action: KeyAction): Boolean = action in DRAWN

    fun draw(canvas: Canvas, action: KeyAction, box: Box, color: Int, pointsLeft: Boolean) {
        val glyph = glyphFor(action, pointsLeft) ?: return
        val size = minOf(box.width, box.height) * SIZE_RATIO
        painter.draw(canvas, glyph.icon, box.centerX, box.centerY, size, color, glyph.mirror)
    }

    private companion object {
        const val SIZE_RATIO = 0.52f
        val DRAWN = setOf(
            KeyAction.DELETE, KeyAction.ENTER, KeyAction.EXPAND, KeyAction.FLIP, KeyAction.LEFT, KeyAction.RIGHT,
            KeyAction.UP, KeyAction.DOWN, KeyAction.WORD_LEFT, KeyAction.WORD_RIGHT, KeyAction.LINE_START, KeyAction.LINE_END,
            KeyAction.UNDO, KeyAction.REDO,
        )
    }
}

internal fun glyphFor(action: KeyAction, pointsLeft: Boolean): KeyGlyph? = when (action) {
    KeyAction.DELETE -> KeyGlyph(SpaceIcon.DELETE)
    KeyAction.ENTER -> KeyGlyph(SpaceIcon.ENTER)
    KeyAction.EXPAND -> KeyGlyph(SpaceIcon.EXPAND, !pointsLeft)
    KeyAction.FLIP -> KeyGlyph(SpaceIcon.SWAP_SIDE)
    KeyAction.LEFT -> KeyGlyph(SpaceIcon.LEFT)
    KeyAction.RIGHT -> KeyGlyph(SpaceIcon.RIGHT)
    KeyAction.UP -> KeyGlyph(SpaceIcon.UP)
    KeyAction.DOWN -> KeyGlyph(SpaceIcon.DOWN)
    KeyAction.WORD_LEFT -> KeyGlyph(SpaceIcon.WORD_BACK)
    KeyAction.WORD_RIGHT -> KeyGlyph(SpaceIcon.WORD_BACK, true)
    KeyAction.LINE_START -> KeyGlyph(SpaceIcon.LINE_START)
    KeyAction.LINE_END -> KeyGlyph(SpaceIcon.LINE_START, true)
    KeyAction.UNDO -> KeyGlyph(SpaceIcon.UNDO)
    KeyAction.REDO -> KeyGlyph(SpaceIcon.REDO)
    else -> null
}
