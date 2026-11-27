package com.spacekeyboard.keyboard.core.macro

class MacroCursor(private val steps: List<MacroStep>) {
    var index = 0
        private set

    var round = 1
        private set

    val size: Int get() = steps.size

    fun next(): MacroStep? {
        if (steps.isEmpty()) return null
        val step = steps[index]
        index += 1
        if (index == steps.size) {
            index = 0
            round += 1
        }
        return step
    }

    val atRoundStart: Boolean get() = index == 0

    fun finished(limit: Int): Boolean = limit > 0 && round > limit
}
