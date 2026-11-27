package com.spacekeyboard.keyboard.core.arabizi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabiziTest {
    @Test
    fun commonWordsConvert() {
        assertEquals("سلام", Arabizi.convert("salam"))
        assertEquals("كتاب", Arabizi.convert("kitab"))
        assertEquals("شكران", Arabizi.convert("shukran"))
        assertEquals("مرحبا", Arabizi.convert("mar7aba"))
        assertEquals("عربي", Arabizi.convert("3arabi"))
        assertEquals("الله", Arabizi.convert("allah"))
    }

    @Test
    fun plainNumbersStayNumbers() {
        assertEquals("2024", Arabizi.convert("2024"))
        assertEquals("7", Arabizi.convert("7"))
    }

    @Test
    fun uppercaseIsTreatedAsLowercase() {
        assertEquals(Arabizi.convert("salam"), Arabizi.convert("SALAM"))
    }

    @Test
    fun emptyInputIsEmpty() {
        assertEquals("", Arabizi.convert(""))
    }

    @Test
    fun acceptsOnlyLatinLettersAndDigits() {
        assertTrue(Arabizi.accepts('a'))
        assertTrue(Arabizi.accepts('Z'))
        assertTrue(Arabizi.accepts('7'))
        assertFalse(Arabizi.accepts(' '))
        assertFalse(Arabizi.accepts('س'))
        assertFalse(Arabizi.accepts('.'))
    }
}
