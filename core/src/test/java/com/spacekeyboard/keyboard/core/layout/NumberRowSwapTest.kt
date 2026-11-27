package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberRowSwapTest {
    @Test
    fun onlyLanguagesWithOwnDigitsCanSwap() {
        assertTrue(NumberRow.hasNative("ar"))
        assertTrue(NumberRow.hasNative("fa"))
        assertFalse(NumberRow.hasNative("en"))
        assertFalse(NumberRow.hasNative("az"))
        assertFalse(NumberRow.hasNative(LayoutCatalog.numbers.id))
    }

    @Test
    fun swapChangesTheShownDigitsOnArabicAndPersian() {
        assertEquals("١٢٣", NumberRow.digits("ar", true).take(3).joinToString(""))
        assertEquals("123", NumberRow.digits("ar", false).take(3).joinToString(""))
        assertEquals("۱۲۳", NumberRow.digits("fa", true).take(3).joinToString(""))
    }

    @Test
    fun longPressOffersTheOtherStyle() {
        assertEquals("١", NumberRow.alternates("ar", false).first())
        assertEquals("1", NumberRow.alternates("ar", true).first())
    }
}
