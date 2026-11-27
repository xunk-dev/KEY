package com.spacekeyboard.keyboard.ime

import android.graphics.Canvas
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconCanvas
import com.spacekeyboard.keyboard.ime.layout.Shift

internal class KeyGlyph(val icon: SpaceIcon, val mirror: Boolean = false)

enum class PagePlace { LETTERS, NUMBERS, SYMBOLS }

internal fun pagePlaceOf(id: String?): PagePlace = when (id) {
    LayoutCatalog.numbers.id -> PagePlace.NUMBERS
    LayoutCatalog.symbols.id -> PagePlace.SYMBOLS
    else -> PagePlace.LETTERS
}

class KeyIcons {
    private val painter = SpaceIconCanvas()

    fun handles(action: KeyAction): Boolean = action in DRAWN

    fun draw(canvas: Canvas, action: KeyAction, box: Box, color: Int, accent: Int, pointsLeft: Boolean, unit: Float, rtl: Boolean = false, enter: EnterKind = EnterKind.ENTER, page: PagePlace = PagePlace.LETTERS) {
        val glyph = glyphFor(action, pointsLeft, rtl, enter, page) ?: return
        val size = fitted(unit, box)
        painter.draw(canvas, glyph.icon, box.centerX, box.centerY, size, color, glyph.mirror, accent)
    }

    fun drawIcon(canvas: Canvas, icon: SpaceIcon, centerX: Float, centerY: Float, size: Float, color: Int, accent: Int = color) =
        painter.draw(canvas, icon, centerX, centerY, size, color, accent = accent)

    fun drawShift(canvas: Canvas, shift: Shift, box: Box, color: Int, accent: Int, unit: Float) =
        drawIcon(canvas, shiftIcon(shift), box.centerX, box.centerY, fitted(unit, box), color, accent)

    private fun fitted(unit: Float, box: Box): Float =
        minOf(unit * SIZE_RATIO, minOf(box.width, box.height) * MAX_OF_KEY)

    private companion object {
        const val SIZE_RATIO = 0.48f
        const val MAX_OF_KEY = 0.62f
        val DRAWN = setOf(
            KeyAction.DELETE, KeyAction.ENTER, KeyAction.EXPAND, KeyAction.FLIP, KeyAction.LEFT, KeyAction.RIGHT,
            KeyAction.UP, KeyAction.DOWN, KeyAction.WORD_LEFT, KeyAction.WORD_RIGHT, KeyAction.LINE_START, KeyAction.LINE_END,
            KeyAction.UNDO, KeyAction.REDO, KeyAction.OPEN_SYMBOLS, KeyAction.SYMBOLS, KeyAction.NUMBERS,
            KeyAction.EMOJI, KeyAction.CLIPBOARD, KeyAction.TRANSLATE,
        )
    }
}

internal fun shiftIcon(shift: Shift): SpaceIcon = when (shift) {
    Shift.OFF -> SpaceIcon.SHIFT
    Shift.ONCE -> SpaceIcon.SHIFT_ONCE
    Shift.LOCKED -> SpaceIcon.SHIFT_LOCK
}

internal fun enterGlyph(kind: EnterKind, rtl: Boolean): KeyGlyph = when (kind) {
    EnterKind.ENTER -> KeyGlyph(SpaceIcon.ENTER)
    EnterKind.GO -> KeyGlyph(SpaceIcon.RIGHT, rtl)
    EnterKind.SEARCH -> KeyGlyph(SpaceIcon.SEARCH)
    EnterKind.SEND -> KeyGlyph(SpaceIcon.SEND, rtl)
    EnterKind.NEXT -> KeyGlyph(SpaceIcon.NEXT, !rtl)
    EnterKind.DONE -> KeyGlyph(SpaceIcon.DONE)
    EnterKind.PREVIOUS -> KeyGlyph(SpaceIcon.NEXT, rtl)
}

internal fun glyphFor(action: KeyAction, pointsLeft: Boolean, rtl: Boolean = false, enter: EnterKind = EnterKind.ENTER, page: PagePlace = PagePlace.LETTERS): KeyGlyph? = when (action) {
    KeyAction.DELETE -> KeyGlyph(SpaceIcon.DELETE, rtl)
    KeyAction.ENTER -> enterGlyph(enter, rtl)
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
    KeyAction.OPEN_SYMBOLS -> KeyGlyph(SpaceIcon.SYMBOLS)
    KeyAction.EMOJI -> KeyGlyph(SpaceIcon.EMOJI)
    KeyAction.CLIPBOARD -> KeyGlyph(SpaceIcon.CLIPBOARD)
    KeyAction.TRANSLATE -> KeyGlyph(SpaceIcon.TRANSLATE)
    KeyAction.SYMBOLS -> KeyGlyph(if (page == PagePlace.SYMBOLS) SpaceIcon.NUMPAD else SpaceIcon.SYMBOLS)
    KeyAction.NUMBERS -> KeyGlyph(if (page == PagePlace.LETTERS) SpaceIcon.NUMPAD else SpaceIcon.KEYS)
    else -> null
}
