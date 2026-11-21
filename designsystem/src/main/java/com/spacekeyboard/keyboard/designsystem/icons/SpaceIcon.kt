package com.spacekeyboard.keyboard.designsystem.icons

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser

enum class SpaceIcon(val mirrors: Boolean = false) {
    THEME, MODE, SYMBOLS, EMOJI, DECORATE, CLIPBOARD, TRANSLATE, IMAGES, VOICE, EDIT, INCOGNITO, SETTINGS, KEYS, TOOLS, WORDS,
    DELETE, ENTER, SHIFT, LANGUAGE, SWAP_SIDE, EXPAND, UP, DOWN, LEFT, RIGHT, WORD_BACK, LINE_START, UNDO, REDO, SEARCH,
    CUSTOMIZE, AUTOFIX, SHORTCUTS, SETUP, PRIVACY, BACKUP, NEXT, DONE, COPY, CUT, SELECT_ALL, ADD, CLEAR_ALL, SAVE,
    CHEVRON(mirrors = true), CHECK
}

internal sealed interface IconPart

internal class StrokePath(data: String) : IconPart {
    val path: Path by lazy { PathParser().parsePathString(data).toPath() }
}

internal class Dot(val x: Float, val y: Float, val r: Float) : IconPart

internal class Ring(val x: Float, val y: Float, val r: Float) : IconPart

internal class Box(val x: Float, val y: Float, val w: Float, val h: Float, val radius: Float, val dashed: Boolean) : IconPart

internal fun p(data: String): IconPart = StrokePath(data)

internal fun dot(x: Float, y: Float, r: Float): IconPart = Dot(x, y, r)

internal fun ring(x: Float, y: Float, r: Float): IconPart = Ring(x, y, r)

internal fun box(x: Float, y: Float, w: Float, h: Float, radius: Float, dashed: Boolean = false): IconPart =
    Box(x, y, w, h, radius, dashed)

private val allParts: Map<SpaceIcon, List<IconPart>> = ToolbarIconParts + KeyIconParts + ScreenIconParts

internal fun SpaceIcon.parts(): List<IconPart> = allParts.getValue(this)
