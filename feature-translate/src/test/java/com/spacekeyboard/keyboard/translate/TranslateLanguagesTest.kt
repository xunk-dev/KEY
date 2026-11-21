package com.spacekeyboard.keyboard.translate

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateLanguagesTest {
    @Test
    fun listHasFiftyNineUniqueCodes() {
        assertEquals(59, TranslateLanguages.codes.size)
        assertEquals(59, TranslateLanguages.codes.toSet().size)
        assertTrue(TranslateLanguages.strong.all { it in TranslateLanguages.codes })
    }

    @Test
    fun supportAndQuality() {
        assertTrue(TranslateLanguages.isSupported("ar"))
        assertFalse(TranslateLanguages.isSupported("xx"))
        assertTrue(TranslateLanguages.isStrong("en"))
        assertFalse(TranslateLanguages.isStrong("mt"))
    }

    @Test
    fun displayNamesFollowTheLocale() {
        assertEquals("Arabic", TranslateLanguages.displayName("ar", Locale.ENGLISH))
        assertEquals("العربية", TranslateLanguages.displayName("ar", Locale.forLanguageTag("ar")))
        TranslateLanguages.codes.forEach { assertTrue(it, TranslateLanguages.displayName(it, Locale.ENGLISH).isNotBlank()) }
    }

    @Test
    fun orderedPutsStrongLanguagesFirstWithoutLosingAny() {
        val ordered = TranslateLanguages.ordered
        assertEquals(TranslateLanguages.codes.toSet(), ordered.toSet())
        assertEquals(59, ordered.size)
        assertTrue(ordered.take(TranslateLanguages.strong.size).all { it in TranslateLanguages.strong })
    }

    @Test
    fun previewFlattensAndTruncates() {
        assertEquals("a b c", TranslateLabels.preview("  a\n b\t c ", 10))
        assertEquals("abc…", TranslateLabels.preview("abcdef", 3))
        assertEquals("abc", TranslateLabels.preview("abc", 3))
    }
}
