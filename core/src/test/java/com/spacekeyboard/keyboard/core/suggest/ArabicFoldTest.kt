package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicFoldTest {
    @Test
    fun unifiesAlefYaaAndTaaMarbuta() {
        assertEquals("احب", ArabicFold.fold("أحب"))
        assertEquals("الانترنت", ArabicFold.fold("الإنترنت"))
        assertEquals("علي", ArabicFold.fold("على"))
        assertEquals("مدرسه", ArabicFold.fold("مدرسة"))
    }

    @Test
    fun dropsDiacriticsAndTatweel() {
        assertEquals("مرحبا", ArabicFold.fold("مَرْحَبـًا"))
    }

    @Test
    fun lowercasesLatinAndMapsPersianLetters() {
        assertEquals("hello", ArabicFold.fold("HeLLo"))
        assertEquals("كتاب", ArabicFold.fold("کتاب"))
        assertEquals("علي", ArabicFold.fold("علی"))
    }
}
