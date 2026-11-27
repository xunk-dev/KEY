package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.Limits
import org.junit.Assert.assertEquals
import org.junit.Test

class FloatOpacityTest {
    @Test
    fun leftToRightFillsFromTheLeft() {
        assertEquals(Limits.FLOAT_OPACITY_MIN, opacityAt(0f, 200, rtl = false))
        assertEquals(Limits.FLOAT_OPACITY_MAX, opacityAt(200f, 200, rtl = false))
    }

    @Test
    fun rightToLeftFillsFromTheRight() {
        assertEquals(Limits.FLOAT_OPACITY_MAX, opacityAt(0f, 200, rtl = true))
        assertEquals(Limits.FLOAT_OPACITY_MIN, opacityAt(200f, 200, rtl = true))
    }

    @Test
    fun middleIsHalfwayBetweenTheLimits() {
        val half = (Limits.FLOAT_OPACITY_MIN + Limits.FLOAT_OPACITY_MAX) / 2
        assertEquals(half, opacityAt(100f, 200, rtl = false))
        assertEquals(half, opacityAt(100f, 200, rtl = true))
    }

    @Test
    fun fingerOutsideTheTrackIsClamped() {
        assertEquals(Limits.FLOAT_OPACITY_MIN, opacityAt(-50f, 200, rtl = false))
        assertEquals(Limits.FLOAT_OPACITY_MAX, opacityAt(900f, 200, rtl = false))
    }

    @Test
    fun unmeasuredTrackKeepsFullOpacity() {
        assertEquals(Limits.FLOAT_OPACITY_MAX, opacityAt(10f, 0, rtl = false))
    }
}
