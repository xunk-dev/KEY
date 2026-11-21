package com.spacekeyboard.keyboard.autofix

object Zone {
    const val COUNT = 9
    const val NONE = -1
    private const val THIRD = 1f / 6f

    fun of(dx: Float, dy: Float): Int = band(dx) + 3 * band(dy)

    private fun band(offset: Float): Int = when {
        offset < -THIRD -> 0
        offset > THIRD -> 2
        else -> 1
    }
}
