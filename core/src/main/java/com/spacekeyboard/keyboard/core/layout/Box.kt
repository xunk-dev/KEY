package com.spacekeyboard.keyboard.core.layout

data class Box(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean = x >= left && x < right && y >= top && y < bottom

    fun inset(by: Float): Box = Box(left + by, top + by, right - by, bottom - by)

    fun distanceTo(x: Float, y: Float): Float {
        val dx = maxOf(left - x, 0f, x - right)
        val dy = maxOf(top - y, 0f, y - bottom)
        return maxOf(dx, dy)
    }
}
