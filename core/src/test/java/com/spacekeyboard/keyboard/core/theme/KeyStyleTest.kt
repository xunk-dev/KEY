package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyStyleTest {
    @Test
    fun designThemesMapToTheirStyle() {
        assertEquals(KeyStyle.GALAXY, KeyStyle.of(ThemeCatalog.byId("nebula")))
        assertEquals(KeyStyle.DAY, KeyStyle.of(ThemeCatalog.byId("daylight")))
        assertEquals(KeyStyle.ECLIPSE, KeyStyle.of(ThemeCatalog.byId("eclipse")))
    }

    @Test
    fun everyLightThemeUsesDayStyle() {
        ThemeCatalog.all.filter { it.light }.forEach { assertEquals(KeyStyle.DAY, KeyStyle.of(it)) }
    }

    @Test
    fun designColorsSurviveTheResolver() {
        listOf("nebula", "daylight", "eclipse").forEach { id ->
            val colors = ThemeCatalog.byId(id).colors
            val palette = ThemeResolver.resolve(colors)
            assertEquals(ColorMath.opaque(colors.keys), palette.keyFill)
            assertTrue(palette.keyFill != palette.background)
        }
    }
}
