package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemeCatalog
import com.spacekeyboard.keyboard.core.theme.ThemeResolver
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.ime.layout.Shift
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeySkinTest {
    @Test
    fun inkStaysReadableOnEveryKeyKindInEveryTheme() {
        ThemeCatalog.all.forEach { theme ->
            val palette = ThemeResolver.resolve(theme.colors)
            val style = KeyStyle.of(theme)
            KeyRole.values().forEach { role ->
                listOf(false, true).forEach { lit ->
                    val skin = KeySkins.of(style, role, palette, lit)
                    val ratio = ColorMath.contrast(skin.ink, ColorMath.opaque(skin.fill))
                    assertTrue("${theme.id} $role lit=$lit $ratio", ratio >= ColorMath.TEXT_MINIMUM)
                }
            }
        }
    }

    @Test
    fun onlyDayDrawsAShadowAndNoSkinHalos() {
        val palette = ThemeResolver.resolve(ThemeCatalog.byId("glass").colors)
        assertEquals(0, KeySkins.of(KeyStyle.GALAXY, KeyRole.LETTER, palette, false).shadow)
        assertNotNull(KeySkins.of(KeyStyle.DAY, KeyRole.LETTER, palette, false).shadow.takeIf { it != 0 })
        assertFalse(KeySkins.of(KeyStyle.GALAXY, KeyRole.PRIMARY, palette, false).halo)
        assertFalse(KeySkins.of(KeyStyle.ECLIPSE, KeyRole.PRIMARY, palette, false).halo)
    }

    @Test
    fun galaxyKeysDrawNoOutlineExceptSpaceAndChips() {
        val palette = ThemeResolver.resolve(ThemeCatalog.byId("glass").colors)
        KeyRole.values().filter { it != KeyRole.QUIET && it != KeyRole.CHIP }.forEach { role ->
            assertEquals("$role", 0, KeySkins.of(KeyStyle.GALAXY, role, palette, false).border)
        }
        assertTrue(KeySkins.of(KeyStyle.GALAXY, KeyRole.QUIET, palette, false).border != 0)
    }

    @Test
    fun dayRoundsSpaceAndEnterIntoPills() {
        val palette = ThemeResolver.resolve(ThemeCatalog.byId("daylight").colors)
        assertTrue(KeySkins.of(KeyStyle.DAY, KeyRole.PRIMARY, palette, false).pill)
        assertTrue(KeySkins.of(KeyStyle.DAY, KeyRole.QUIET, palette, false).pill)
        assertFalse(KeySkins.of(KeyStyle.DAY, KeyRole.LETTER, palette, false).pill)
        assertTrue(KeySkins.of(KeyStyle.GALAXY, KeyRole.QUIET, palette, false).pill)
        assertTrue(KeySkins.of(KeyStyle.GALAXY, KeyRole.PRIMARY, palette, false).pill)
        assertFalse(KeySkins.of(KeyStyle.GALAXY, KeyRole.FUNCTION, palette, false).pill)
    }

    @Test
    fun rolesFollowTheKeyAction() {
        assertEquals(KeyRole.PRIMARY, keyRole(KeyAction.ENTER))
        assertEquals(KeyRole.QUIET, keyRole(KeyAction.SPACE))
        assertEquals(KeyRole.LETTER, keyRole(KeyAction.COMMIT))
        assertEquals(KeyRole.FUNCTION, keyRole(KeyAction.DELETE))
        assertEquals(KeyRole.CHIP, keyRole(KeyAction.NUMBERS))
        assertEquals(KeyRole.CHIP, keyRole(KeyAction.LANGUAGE))
        assertEquals(KeyRole.CHIP, keyRole(KeyAction.RAIL))
    }

    @Test
    fun fadeScalesExistingAlpha() {
        assertEquals(0x7FFFFFFF, fade(0xFFFFFFFF.toInt(), 0.5f))
        assertEquals(0x40102030, fade(0x80102030.toInt(), 0.5f))
        assertEquals(0xFF102030.toInt(), fade(0xFF102030.toInt(), 1f))
    }

    @Test
    fun keyGlyphsMatchTheDesignIcons() {
        assertEquals(SpaceIcon.DELETE, glyphFor(KeyAction.DELETE, true)?.icon)
        assertEquals(SpaceIcon.SWAP_SIDE, glyphFor(KeyAction.FLIP, true)?.icon)
        assertEquals(SpaceIcon.WORD_BACK, glyphFor(KeyAction.WORD_RIGHT, true)?.icon)
        assertTrue(glyphFor(KeyAction.WORD_RIGHT, true)?.mirror == true)
        assertTrue(glyphFor(KeyAction.LINE_END, true)?.mirror == true)
        assertFalse(glyphFor(KeyAction.LEFT, true)?.mirror == true)
    }

    @Test
    fun expandChevronMirrorsOnlyWhenItPointsRight() {
        assertFalse(glyphFor(KeyAction.EXPAND, true)?.mirror == true)
        assertTrue(glyphFor(KeyAction.EXPAND, false)?.mirror == true)
    }

    @Test
    fun letterAndTextKeysHaveNoGlyph() {
        assertNull(glyphFor(KeyAction.COMMIT, true))
        assertNull(glyphFor(KeyAction.SPACE, true))
        assertNull(glyphFor(KeyAction.SHIFT, true))
        assertEquals(SpaceIcon.SHIFT, shiftIcon(Shift.OFF))
        assertEquals(SpaceIcon.SHIFT_ONCE, shiftIcon(Shift.ONCE))
        assertEquals(SpaceIcon.SHIFT_LOCK, shiftIcon(Shift.LOCKED))
        assertNull(glyphFor(KeyAction.LANGUAGE, true))
        assertEquals(SpaceIcon.SYMBOLS, glyphFor(KeyAction.OPEN_SYMBOLS, true)?.icon)
    }
}
