package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LayoutSubtypeTest {
    @Test
    fun extraValueTagWinsOverTheLocale() {
        assertEquals("az", LayoutCatalog.forSubtype("ar", "layout=az"))
        assertEquals("fa", LayoutCatalog.forSubtype(null, "layout=fa"))
    }

    @Test
    fun tagIsFoundAmongOtherExtraValues() {
        assertEquals("fa", LayoutCatalog.forSubtype("en", "foo=1, layout=fa"))
    }

    @Test
    fun unknownTagFallsBackToTheLocale() {
        assertEquals("en", LayoutCatalog.forSubtype("en_US", "layout=xx"))
    }

    @Test
    fun localeAloneResolvesTheLanguage() {
        assertEquals("ar", LayoutCatalog.forSubtype("ar", null))
        assertEquals("en", LayoutCatalog.forSubtype("en-GB", ""))
        assertEquals("fa", LayoutCatalog.forSubtype("fa_IR", null))
    }

    @Test
    fun unsupportedSubtypesResolveToNothing() {
        assertNull(LayoutCatalog.forSubtype("de_DE", null))
        assertNull(LayoutCatalog.forSubtype("az", null))
        assertNull(LayoutCatalog.forSubtype(null, null))
    }
}
