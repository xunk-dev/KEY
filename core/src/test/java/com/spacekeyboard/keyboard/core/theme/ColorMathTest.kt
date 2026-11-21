package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorMathTest {
    @Test
    fun blackAgainstWhiteIsTwentyOne() {
        assertEquals(21.0, ColorMath.contrast(ColorMath.BLACK, ColorMath.WHITE), 0.01)
    }

    @Test
    fun mixHalfwayGivesMidGrey() {
        val mid = ColorMath.mix(ColorMath.BLACK, ColorMath.WHITE, 0.5f)
        assertEquals(128, ColorMath.red(mid))
        assertEquals(128, ColorMath.blue(mid))
    }

    @Test
    fun ensureContrastReachesMinimumOnDarkBackground() {
        val background = 0xFF101010.toInt()
        val fixed = ColorMath.ensureContrast(0xFF181818.toInt(), background, 4.5)
        assertTrue(ColorMath.contrast(fixed, background) >= 4.5)
    }

    @Test
    fun ensureContrastReachesMinimumOnLightBackground() {
        val background = 0xFFF0F0F0.toInt()
        val fixed = ColorMath.ensureContrast(0xFFE8E8E8.toInt(), background, 4.5)
        assertTrue(ColorMath.contrast(fixed, background) >= 4.5)
    }

    @Test
    fun readableOnAlwaysMeetsTextMinimum() {
        for (r in 0..255 step 51) for (g in 0..255 step 51) for (b in 0..255 step 51) {
            val background = ColorMath.rgb(r, g, b)
            val text = ColorMath.readableOn(background, listOf(0xFF808080.toInt()))
            assertTrue(ColorMath.contrast(text, background) >= ColorMath.TEXT_MINIMUM)
        }
    }

    @Test
    fun withAlphaKeepsColorChannels() {
        val faded = ColorMath.withAlpha(0xFF336699.toInt(), 0.5f)
        assertEquals(0x80, faded ushr 24)
        assertEquals(0x33, ColorMath.red(faded))
    }
}
