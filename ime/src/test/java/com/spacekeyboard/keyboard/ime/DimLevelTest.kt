package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.DimLimits
import org.junit.Assert.assertEquals
import org.junit.Test

class DimLevelTest {
    @Test
    fun leftToRightRunsFromNightToDay() {
        assertEquals(DimLimits.LEVEL_MIN, levelAt(0f, 200, rtl = false))
        assertEquals(DimLimits.LEVEL_MAX, levelAt(200f, 200, rtl = false))
    }

    @Test
    fun rightToLeftStartsAtNightOnTheRight() {
        assertEquals(DimLimits.LEVEL_MAX, levelAt(0f, 200, rtl = true))
        assertEquals(DimLimits.LEVEL_MIN, levelAt(200f, 200, rtl = true))
    }

    @Test
    fun outOfRangeTouchesAreClamped() {
        assertEquals(DimLimits.LEVEL_MIN, levelAt(-50f, 200, rtl = false))
        assertEquals(DimLimits.LEVEL_MAX, levelAt(900f, 200, rtl = false))
        assertEquals(DimLimits.LEVEL_MAX, levelAt(10f, 0, rtl = false))
    }
}
