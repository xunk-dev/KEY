package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeChoiceTest {
    @Test
    fun catalogHasTwelveUniqueThemes() {
        assertEquals(4, ThemeCatalog.all.size)
        assertEquals(4, ThemeCatalog.all.map { it.id }.toSet().size)
        assertEquals(1, ThemeCatalog.all.map { it.backdrop }.toSet().size)
    }

    @Test
    fun unknownIdFallsBackToDefault() {
        assertEquals(ThemeCatalog.default.id, ThemeCatalog.byId("missing").id)
        assertEquals(ThemeCatalog.default.id, ThemeCatalog.byId(null).id)
    }

    @Test
    fun nextWrapsAround() {
        val last = ThemeCatalog.all.last().id
        assertEquals(ThemeCatalog.all.first().id, ThemeCatalog.next(last).id)
        assertEquals(ThemeCatalog.all[1].id, ThemeCatalog.next(ThemeCatalog.all[0].id).id)
    }

    @Test
    fun overrideChangesOnlyCurrentTheme() {
        val choice = ThemeChoice("glass").withOverride(ThemeSlot.GLOW, 0xFF00FF00.toInt())
        assertEquals(0xFF00FF00.toInt(), choice.requested().glow)
        assertEquals(ThemeCatalog.byId("eclipse").colors, ThemeChoice("eclipse", choice.overrides).requested())
        assertTrue(choice.hasOverrides())
    }

    @Test
    fun resetRemovesOverridesOfCurrentThemeOnly() {
        val both = ThemeChoice("glass")
            .withOverride(ThemeSlot.KEYS, 0xFF123456.toInt())
            .copy(themeId = "eclipse")
            .withOverride(ThemeSlot.KEYS, 0xFF654321.toInt())
        val reset = both.withoutOverrides()
        assertFalse(reset.hasOverrides())
        assertTrue(ThemeChoice("glass", reset.overrides).hasOverrides())
    }

    @Test
    fun paletteReflectsOverrides() {
        val choice = ThemeChoice("fog").withOverride(ThemeSlot.BACKGROUND, 0xFF000000.toInt())
        assertEquals(0xFF000000.toInt(), choice.palette().background)
    }
}
