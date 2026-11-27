package com.spacekeyboard.keyboard.core.macro

enum class MacroRepeat(val id: String, val rounds: Int) {
    ONCE("once", 1),
    THREE("three", 3),
    FIVE("five", 5),
    TEN("ten", 10),
    FOREVER("forever", 0);

    val limited: Boolean get() = rounds > 0

    companion object {
        fun byId(id: String?): MacroRepeat = entries.firstOrNull { it.id == id } ?: FOREVER
    }
}
