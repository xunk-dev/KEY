package com.spacekeyboard.keyboard.autofix

data class Rule(
    val fixed: Int = 0,
    val applied: Int = 0,
    val undone: Int = 0,
    val kept: Int = 0,
    val day: Int = 0,
) {
    val score: Int get() = fixed + applied - UNDO_WEIGHT * undone
    val total: Int get() = fixed + applied + undone + kept

    fun withFixed(today: Int) = copy(fixed = (fixed + 1).coerceAtMost(CAP), day = today)

    fun withApplied(today: Int) = copy(applied = (applied + 1).coerceAtMost(CAP), day = today)

    fun withUndone(today: Int) = copy(undone = (undone + 1).coerceAtMost(CAP), day = today)

    fun withKept() = copy(kept = (kept + 1).coerceAtMost(CAP))

    fun halved() = Rule(fixed / 2, applied / 2, undone / 2, kept / 2, day)

    companion object {
        const val UNDO_WEIGHT = 2
        const val CAP = 60
    }
}
