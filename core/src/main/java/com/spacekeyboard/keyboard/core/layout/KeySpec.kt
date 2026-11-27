package com.spacekeyboard.keyboard.core.layout

enum class KeyAction {
    COMMIT, SPACE, DELETE, ENTER, LANGUAGE, EXPAND, FLIP, SPACER, SHIFT, NUMBERS, SYMBOLS, OPEN_SYMBOLS, RAIL,
    EDIT, SELECT_ALL, COPY, CUT, PASTE, SELECT_MODE, LEFT, RIGHT, UP, DOWN, WORD_LEFT, WORD_RIGHT, LINE_START, LINE_END,
    UNDO, REDO, DELETE_WORD, EMOJI, CLIPBOARD, TRANSLATE;

    val isEditCommand: Boolean get() = this in COMMANDS

    val isTool: Boolean get() = this == EMOJI || this == CLIPBOARD || this == TRANSLATE

    private companion object {
        val COMMANDS = setOf(
            SELECT_ALL, COPY, CUT, PASTE, SELECT_MODE, LEFT, RIGHT, UP, DOWN, WORD_LEFT, WORD_RIGHT, LINE_START, LINE_END,
            UNDO, REDO,
        )
    }
}

data class KeySpec(
    val id: String,
    val label: String,
    val action: KeyAction,
    val weight: Float,
    val movable: Boolean,
    val alts: List<String> = emptyList(),
    val pair: Boolean = false,
) {
    val letters: List<String> get() = if (pair) label.map { it.toString() } else listOf(label)

    val splittable: Boolean get() = action == KeyAction.SPACE
    val visible: Boolean get() = action != KeyAction.SPACER
}

data class SlotRef(val row: Int, val col: Int)
