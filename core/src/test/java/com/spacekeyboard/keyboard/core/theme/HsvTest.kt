package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class HsvTest {
    @Test
    fun primariesConvertExactly() {
        assertEquals(0xFFFF0000.toInt(), Hsv(0f, 1f, 1f).toArgb())
        assertEquals(0xFF00FF00.toInt(), Hsv(120f, 1f, 1f).toArgb())
        assertEquals(0xFF0000FF.toInt(), Hsv(240f, 1f, 1f).toArgb())
    }

    @Test
    fun roundTripStaysWithinOneStep() {
        for (r in 0..255 step 17) for (g in 0..255 step 17) for (b in 0..255 step 17) {
            val original = ColorMath.rgb(r, g, b)
            val back = Hsv.fromArgb(original).toArgb()
            assertTrue(abs(r - ColorMath.red(back)) <= 1)
            assertTrue(abs(g - ColorMath.green(back)) <= 1)
            assertTrue(abs(b - ColorMath.blue(back)) <= 1)
        }
    }

    @Test
    fun blackAndWhiteHaveNoSaturation() {
        assertEquals(0f, Hsv.fromArgb(ColorMath.BLACK).value, 0f)
        assertEquals(0f, Hsv.fromArgb(ColorMath.WHITE).saturation, 0f)
    }

    @Test
    fun withClampsRanges() {
        val clamped = Hsv(10f, 0.5f, 0.5f).with(hue = 999f, saturation = 2f, value = -1f)
        assertEquals(360f, clamped.hue, 0f)
        assertEquals(1f, clamped.saturation, 0f)
        assertEquals(0f, clamped.value, 0f)
    }
}
