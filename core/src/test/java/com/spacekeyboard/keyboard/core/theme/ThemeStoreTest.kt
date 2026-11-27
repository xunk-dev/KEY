package com.spacekeyboard.keyboard.core.theme

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeStoreTest {
    private val fake = FakePreferences()
    private val store = ThemeStore(fake.prefs)

    @Test
    fun emptyStoreReadsTheDefaults() {
        val choice = store.read()
        assertEquals(ThemeCatalog.default.id, choice.themeId)
        assertFalse(choice.animated)
        assertFalse(choice.followSystem)
        assertTrue(choice.overrides.isEmpty())
    }

    @Test
    fun selectedThemeIsKeptAndUnknownIdFallsBack() {
        val other = ThemeCatalog.all.last().id
        store.selectTheme(other)
        assertEquals(other, store.read().themeId)
        store.selectTheme("nope")
        assertEquals(ThemeCatalog.default.id, store.read().themeId)
    }

    @Test
    fun colorOverridesAreOpaqueAndPerTheme() {
        val id = ThemeCatalog.default.id
        store.setOverride(id, ThemeSlot.GLOW, 0x00123456)
        assertEquals(0xFF123456.toInt(), store.read().overrides.getValue(id).getValue(ThemeSlot.GLOW))
    }

    @Test
    fun resetRemovesOnlyThatThemesOverrides() {
        val first = ThemeCatalog.all.first().id
        val second = ThemeCatalog.all.last().id
        store.setOverride(first, ThemeSlot.KEYS, 0x010203)
        store.setOverride(second, ThemeSlot.KEYS, 0x040506)
        store.resetTheme(first)
        val overrides = store.read().overrides
        assertFalse(first in overrides)
        assertTrue(second in overrides)
    }

    @Test
    fun switchesRoundTrip() {
        store.setAnimated(false)
        store.setFollowSystem(true)
        val choice = store.read()
        assertFalse(choice.animated)
        assertTrue(choice.followSystem)
    }

    @Test
    fun observersAreNotifiedUntilClosed() {
        var calls = 0
        val subscription = store.observe { calls++ }
        store.setAnimated(false)
        assertEquals(1, calls)
        subscription.close()
        store.setAnimated(true)
        assertEquals(1, calls)
    }
}
