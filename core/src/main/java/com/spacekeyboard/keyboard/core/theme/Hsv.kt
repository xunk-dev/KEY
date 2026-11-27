package com.spacekeyboard.keyboard.core.theme

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class Hsv(val hue: Float, val saturation: Float, val value: Float) {
    fun toArgb(): Int {
        val h = ((hue % 360f) + 360f) % 360f
        val c = value * saturation
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = value - c
        val (r, g, b) = when (floor(h / 60f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return ColorMath.rgb(((r + m) * 255f).roundToInt(), ((g + m) * 255f).roundToInt(), ((b + m) * 255f).roundToInt())
    }

    fun with(hue: Float = this.hue, saturation: Float = this.saturation, value: Float = this.value) =
        Hsv(hue.coerceIn(0f, 360f), saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f))

    companion object {
        fun fromArgb(argb: Int): Hsv {
            val r = ColorMath.red(argb) / 255f
            val g = ColorMath.green(argb) / 255f
            val b = ColorMath.blue(argb) / 255f
            val top = max(r, max(g, b))
            val delta = top - min(r, min(g, b))
            val hue = when {
                delta == 0f -> 0f
                top == r -> 60f * (((g - b) / delta) % 6f)
                top == g -> 60f * ((b - r) / delta + 2f)
                else -> 60f * ((r - g) / delta + 4f)
            }
            return Hsv((hue + 360f) % 360f, if (top == 0f) 0f else delta / top, top)
        }
    }
}
