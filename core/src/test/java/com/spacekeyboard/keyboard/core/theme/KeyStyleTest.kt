package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyStyleTest {
    @Test
    fun everyThemeUsesTheGlassKeyDesign() {
        ThemeCatalog.all.forEach { assertEquals(it.id, KeyStyle.GLASS, KeyStyle.of(it)) }
    }

    @Test
    fun designColorsSurviveTheResolver() {
        listOf("glass", "daylight", "eclipse").forEach { id ->
            val colors = ThemeCatalog.byId(id).colors
            val palette = ThemeResolver.resolve(colors)
            assertEquals(ColorMath.opaque(colors.keys), palette.keyFill)
            assertTrue(palette.keyFill != palette.background)
        }
    }
}
