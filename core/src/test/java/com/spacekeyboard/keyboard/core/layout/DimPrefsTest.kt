package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DimPrefsTest {
    private val fake = FakePreferences()
    private val store = KeyboardPrefsStore(fake.prefs)

    @Test
    fun startsAtFullDayBrightness() {
        val read = store.read().dim
        assertEquals(DimLimits.LEVEL_DEFAULT, read.levelPercent)
        assertFalse(read.dimmed)
        assertEquals(1f, read.brightness, 0.001f)
    }

    @Test
    fun wildValuesAreClamped() {
        assertEquals(DimLimits.LEVEL_MIN, KeyboardPrefs(dim = DimPrefs(0)).sanitized().dim.levelPercent)
        assertEquals(DimLimits.LEVEL_MAX, KeyboardPrefs(dim = DimPrefs(900)).sanitized().dim.levelPercent)
    }

    @Test
    fun levelIsSavedAndClamped() {
        store.setDimLevel(40)
        val read = store.read().dim
        assertEquals(40, read.levelPercent)
        assertTrue(read.dimmed)
        assertEquals(0.4f, read.brightness, 0.001f)
        store.setDimLevel(1)
        assertEquals(DimLimits.LEVEL_MIN, store.read().dim.levelPercent)
    }

    @Test
    fun resetAllBringsFullBrightnessBack() {
        store.setDimLevel(40)
        store.resetAll()
        assertEquals(DimPrefs(), store.read().dim)
    }
}
