package com.spacekeyboard.keyboard.designsystem.icons

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser

enum class SpaceIcon(val mirrors: Boolean = false) {
    THEME, MODE, SYMBOLS, EMOJI, DECORATE, CLIPBOARD, TRANSLATE, IMAGES, VOICE, EDIT, RESIZE, INCOGNITO, SETTINGS, KEYS, NUMPAD, TOOLS, WORDS,
    MODE_NORMAL, MODE_ONE_RIGHT, MODE_ONE_LEFT, MODE_SPLIT,
    FLOAT, OPACITY, CENTER, DOCK, MOVE, MOON, SUN, CLOSE, CALCULATOR, AUTOMATION, PLAY, PAUSE, STOP,
    DELETE, ENTER, SEND, SHIFT, LANGUAGE, SWAP_SIDE, EXPAND, UP, DOWN, LEFT, RIGHT, WORD_BACK, LINE_START, UNDO, REDO, SEARCH,
    SHIFT_ONCE, SHIFT_LOCK,
    CUSTOMIZE, AUTOFIX, SHORTCUTS, SETUP, PRIVACY, BACKUP, NEXT, DONE, COPY, CUT, SELECT_ALL, ADD, CLEAR_ALL, SAVE,
    CHEVRON(mirrors = true), CHECK,
    PIN, BOOKMARK, TRASH, BROOM, FOLDER_ADD, LOCK, UNLOCK, MORE, CLIP_EMPTY, SHARE, PENCIL, FULL_VIEW, INSERT,
    SMART, KIND_ALL, KIND_LINK, KIND_EMAIL, KIND_PHONE, KIND_USERNAME, KIND_TOKEN, KIND_NUMBER, KIND_TEXT,
    RECENTS, GROUP_FACES, GROUP_BODY, GROUP_PEOPLE, GROUP_ANIMALS, GROUP_FOOD, GROUP_TRAVEL, GROUP_ACTIVITIES, GROUP_OBJECTS, GROUP_SYMBOLS, GROUP_FLAGS,
    SYM_FEATURED, SYM_CURRENCY, SYM_MATH, SYM_ARROWS, SYM_STARS, SYM_HEARTS, SYM_SHAPES, SYM_MARKS, SYM_SIGNS, SYM_PUNCTUATION,
    SYM_BRACKETS, SYM_FRAMES, SYM_GREEK, SYM_CIRCLED, SYM_NUMERALS, SYM_ARABIC, SYM_GAMES, SYM_MUSIC, SYM_MISC, SYM_ORNAMENTS,
    SYM_TECHNICAL,
    FACE_CLASSIC, FACE_HAPPY, FACE_LAUGH, FACE_LOVE, FACE_SHY, FACE_SAD, FACE_ANGRY, FACE_SURPRISED, FACE_SLEEPY, FACE_COOL,
    FACE_EVIL, FACE_CONFUSED, FACE_ANIMALS, FACE_ACTIONS,
    FILTER_ALL, FILTER_GIF, FILTER_STICKER,
    DECOR_FONTS, DECOR_MARKS, DECOR_STRETCH,
    ART,
    VIBRATE, SOUND
}

internal sealed interface IconPart

internal class StrokePath(data: String) : IconPart {
    val path: Path by lazy { PathParser().parsePathString(data).toPath() }
}

internal class Fill(data: String) : IconPart {
    val path: Path by lazy { PathParser().parsePathString(data).toPath() }
}

internal class Dot(val x: Float, val y: Float, val r: Float) : IconPart

internal class Ring(val x: Float, val y: Float, val r: Float) : IconPart

internal class Box(val x: Float, val y: Float, val w: Float, val h: Float, val radius: Float, val dashed: Boolean) : IconPart

internal enum class Tone { ACCENT, SOFT }

internal class Toned(val part: IconPart, val tone: Tone) : IconPart

internal fun p(data: String): IconPart = StrokePath(data)

internal fun fill(data: String): IconPart = Fill(data)

internal fun dot(x: Float, y: Float, r: Float): IconPart = Dot(x, y, r)

internal fun ring(x: Float, y: Float, r: Float): IconPart = Ring(x, y, r)

internal fun box(x: Float, y: Float, w: Float, h: Float, radius: Float, dashed: Boolean = false): IconPart =
    Box(x, y, w, h, radius, dashed)

internal fun acc(part: IconPart): IconPart = Toned(part, Tone.ACCENT)

internal fun soft(part: IconPart): IconPart = Toned(part, Tone.SOFT)

internal const val SOFT_ALPHA = 0.5f

private val allParts: Map<SpaceIcon, List<IconPart>> = ToolbarIconParts + KeyIconParts + ScreenIconParts + EmojiIconParts + ClipIconParts + SymbolIconParts + DecorIconParts

internal fun SpaceIcon.parts(): List<IconPart> = allParts.getValue(this)
