package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatPrefsTest {
    private val fake = FakePreferences()
    private val store = KeyboardPrefsStore(fake.prefs)

    @Test
    fun heightOpacityAndSizeAreClamped() {
        val wild = KeyboardPrefs(floatHeightPercent = 5, floatOpacityPercent = 0).sanitized()
        assertEquals(Limits.FLOAT_HEIGHT_MIN, wild.floatHeightPercent)
        assertEquals(Limits.FLOAT_OPACITY_MIN, wild.floatOpacityPercent)
        val huge = KeyboardPrefs(floatHeightPercent = 900, floatOpacityPercent = 900, floatWidthPercent = 900).sanitized()
        assertEquals(Limits.FLOAT_HEIGHT_MAX, huge.floatHeightPercent)
        assertEquals(Limits.FLOAT_OPACITY_MAX, huge.floatOpacityPercent)
        assertEquals(Limits.FLOAT_WIDTH_MAX, huge.floatWidthPercent)
    }

    @Test
    fun floatingRowsUseTheFloatingHeight() {
        val normal = KeyboardPrefs(heightPercent = 100, floatHeightPercent = 60)
        assertEquals(60f, normal.rowDp(), 0.001f)
        assertEquals(60f * 0.6f, normal.copy(mode = PanelMode.FLOAT).rowDp(), 0.001f)
        assertEquals(60f, normal.copy(mode = PanelMode.SPLIT).rowDp(), 0.001f)
    }

    @Test
    fun everyFloatValueRoundTrips() {
        store.setFloatFrame(width = 70, height = 90, left = 120, top = 340)
        store.setFloatOpacity(55)
        val read = store.read()
        assertEquals(70, read.floatWidthPercent)
        assertEquals(90, read.floatHeightPercent)
        assertEquals(55, read.floatOpacityPercent)
        assertEquals(120, read.floatLeft)
        assertEquals(340, read.floatTop)
    }

    @Test
    fun enteringFloatSwitchesModeAndCentresTheKeyboard() {
        store.setFloatWidth(70)
        store.enterFloat()
        val read = store.read()
        assertEquals(PanelMode.FLOAT, read.mode)
        assertEquals(Limits.centeredLeft(70), read.floatLeft)
        assertEquals(Limits.FLOAT_START_TOP, read.floatTop)
    }

    @Test
    fun centeredLeftLeavesEqualSpaceOnBothSides() {
        assertEquals(0, Limits.centeredLeft(100))
        assertEquals(150, Limits.centeredLeft(70))
        assertEquals(Limits.FLOAT_POS_MAX, Limits.centeredLeft(60) * 2 + 60 * Limits.FLOAT_POS_MAX / 100)
    }

    @Test
    fun resetSizeClearsTheFloatingSizeButKeepsOpacity() {
        store.setFloatFrame(width = 70, height = 90, left = 120, top = 340)
        store.setFloatOpacity(55)
        store.resetSize()
        val read = store.read()
        assertEquals(KeyboardPrefs().floatWidthPercent, read.floatWidthPercent)
        assertEquals(KeyboardPrefs().floatHeightPercent, read.floatHeightPercent)
        assertEquals(55, read.floatOpacityPercent)
    }

    @Test
    fun resetAllClearsTheFloatOpacityToo() {
        store.setFloatOpacity(40)
        store.resetAll()
        assertEquals(Limits.FLOAT_OPACITY_MAX, store.read().floatOpacityPercent)
    }
}
