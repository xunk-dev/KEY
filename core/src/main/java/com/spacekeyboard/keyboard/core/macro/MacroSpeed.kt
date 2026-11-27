package com.spacekeyboard.keyboard.core.macro

enum class MacroSpeed(val id: String, val stepMs: Long) {
    SLOW("slow", 1_200L),
    NORMAL("normal", 600L),
    FAST("fast", 250L),
    TURBO("turbo", 100L);

    val roundMs: Long get() = maxOf(stepMs * 2, MIN_ROUND_MS)

    fun next(): MacroSpeed = entries[(ordinal + 1) % entries.size]

    companion object {
        const val MIN_ROUND_MS = 400L

        fun byId(id: String?): MacroSpeed = entries.firstOrNull { it.id == id } ?: NORMAL
    }
}
