package com.spacekeyboard.keyboard.core.theme

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object ColorMath {
    const val OPAQUE = 0xFF000000.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()
    const val BLACK = 0xFF000000.toInt()
    private const val STEPS = 50

    fun red(argb: Int) = (argb shr 16) and 0xFF
    fun green(argb: Int) = (argb shr 8) and 0xFF
    fun blue(argb: Int) = argb and 0xFF

    fun rgb(r: Int, g: Int, b: Int) = OPAQUE or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)

    fun opaque(argb: Int) = argb or OPAQUE

    fun withAlpha(argb: Int, alpha: Float): Int = (argb and 0x00FFFFFF) or ((alpha.coerceIn(0f, 1f) * 255f).roundToInt() shl 24)

    private fun channel(value: Int): Double {
        val v = value / 255.0
        return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    fun luminance(argb: Int): Double =
        0.2126 * channel(red(argb)) + 0.7152 * channel(green(argb)) + 0.0722 * channel(blue(argb))

    fun contrast(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    fun mix(from: Int, to: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        fun lerp(x: Int, y: Int) = (x + (y - x) * f).roundToInt()
        return rgb(lerp(red(from), red(to)), lerp(green(from), green(to)), lerp(blue(from), blue(to)))
    }

    fun readableOn(background: Int, candidates: List<Int>): Int {
        candidates.firstOrNull { contrast(it, background) >= TEXT_MINIMUM }?.let { return it }
        return if (contrast(WHITE, background) >= contrast(BLACK, background)) WHITE else BLACK
    }

    fun ensureContrast(color: Int, background: Int, minimum: Double): Int {
        val base = opaque(color)
        if (contrast(base, background) >= minimum) return base
        val end = if (luminance(background) < 0.4) WHITE else BLACK
        for (step in 1..STEPS) {
            val candidate = mix(base, end, step / STEPS.toFloat())
            if (contrast(candidate, background) >= minimum) return candidate
        }
        return end
    }

    const val TEXT_MINIMUM = 4.5
}
