package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeResolverTest {
    private fun assertLegible(palette: ThemePalette) {
        assertTrue(ColorMath.contrast(palette.keyText, palette.keyFill) >= ColorMath.TEXT_MINIMUM)
        assertTrue(ColorMath.contrast(palette.keyTextPressed, palette.keyPressed) >= ColorMath.TEXT_MINIMUM)
        assertTrue(ColorMath.contrast(palette.label, palette.background) >= ColorMath.TEXT_MINIMUM)
        assertTrue(ColorMath.contrast(palette.keyFill, palette.background) >= ThemeResolver.KEY_SEPARATION - 0.005)
        assertTrue(ColorMath.contrast(palette.glow, palette.background) >= ThemeResolver.DECOR - 0.005)
        assertTrue(ColorMath.contrast(palette.border, palette.background) >= ThemeResolver.DECOR - 0.005)
    }

    @Test
    fun everyCatalogThemeIsLegibleAndUnadjusted() {
        ThemeCatalog.all.forEach {
            val palette = ThemeResolver.resolve(it.colors)
            assertLegible(palette)
            assertFalse(it.id, palette.adjusted)
        }
    }

    @Test
    fun extremeUserColorsAreStillLegible() {
        val samples = listOf(ColorMath.BLACK, ColorMath.WHITE, 0xFF808080.toInt(), 0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt())
        for (bg in samples) for (keys in samples) for (glow in samples) {
            assertLegible(ThemeResolver.resolve(ThemeColors(bg, keys, glow, keys)))
        }
    }

    @Test
    fun identicalKeysAndBackgroundGetSeparated() {
        val palette = ThemeResolver.resolve(ThemeColors(0xFF202020.toInt(), 0xFF202020.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()))
        assertTrue(palette.adjusted)
        assertTrue(palette.keyFill != palette.background)
    }

    @Test
    fun lightBackgroundGetsDarkText() {
        val palette = ThemeResolver.resolve(ThemeColors(0xFFFFFFFF.toInt(), 0xFFE0E0E0.toInt(), 0xFF3366FF.toInt(), 0xFF000000.toInt()))
        assertLegible(palette)
        assertTrue(ColorMath.luminance(palette.label) < 0.2)
    }

    @Test
    fun resolvedColorsAreOpaque() {
        val palette = ThemeResolver.resolve(ThemeColors(0x00112233, 0x00445566, 0x00778899, 0x00AABBCC))
        assertEquals(0xFF, palette.background ushr 24)
        assertEquals(0xFF, palette.keyFill ushr 24)
    }
}
