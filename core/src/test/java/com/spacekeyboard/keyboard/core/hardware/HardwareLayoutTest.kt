package com.spacekeyboard.keyboard.core.hardware

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareLayoutTest {
    @Test
    fun arabicLettersFollowThePhysicalKeys() {
        assertEquals("ض", HardwareLayout.translate("ar", 'q'))
        assertEquals("ش", HardwareLayout.translate("ar", 'a'))
        assertEquals("ا", HardwareLayout.translate("ar", 'h'))
        assertEquals("لا", HardwareLayout.translate("ar", 'b'))
        assertEquals("ذ", HardwareLayout.translate("ar", '`'))
    }

    @Test
    fun arabicShiftGivesMarksAndVariants() {
        assertEquals("َ", HardwareLayout.translate("ar", 'Q'))
        assertEquals("ْ", HardwareLayout.translate("ar", 'X'))
        assertEquals("أ", HardwareLayout.translate("ar", 'H'))
        assertEquals("؟", HardwareLayout.translate("ar", '?'))
    }

    @Test
    fun persianUsesItsOwnLetters() {
        assertEquals("ی", HardwareLayout.translate("fa", 'd'))
        assertEquals("پ", HardwareLayout.translate("fa", 'm'))
        assertEquals("گ", HardwareLayout.translate("fa", '\''))
        assertEquals("ژ", HardwareLayout.translate("fa", 'C'))
        assertEquals("‌", HardwareLayout.translate("fa", '`'))
    }

    @Test
    fun persianShiftKeepsUnmappedKeysAsTyped() {
        assertEquals("ك", HardwareLayout.translate("fa", 'Z'))
        assertEquals("A", HardwareLayout.translate("fa", 'A'))
    }

    @Test
    fun otherLanguagesAndUnmappedCharactersPassThrough() {
        assertEquals("q", HardwareLayout.translate("en", 'q'))
        assertEquals("q", HardwareLayout.translate("az", 'q'))
        assertEquals("1", HardwareLayout.translate("ar", '1'))
        assertEquals(" ", HardwareLayout.translate("fa", ' '))
    }

    @Test
    fun textForMapsLettersAndPassesOtherCodePointsThrough() {
        assertEquals("\u0636", HardwareLayout.textFor("ar", 'q'.code))
        assertEquals("q", HardwareLayout.textFor("en", 'q'.code))
        assertEquals("\uD83D\uDE00", HardwareLayout.textFor("en", 0x1F600))
        assertEquals("\uD83D\uDE00", HardwareLayout.textFor("ar", 0x1F600))
    }

    @Test
    fun textForRejectsControlsAndEmptyCodes() {
        assertNull(HardwareLayout.textFor("ar", 0))
        assertNull(HardwareLayout.textFor("en", -1))
        assertNull(HardwareLayout.textFor("fa", 0x0A))
        assertNull(HardwareLayout.textFor("en", 0x7F))
    }

    @Test
    fun onlyArabicAndPersianAreMapped() {
        assertTrue(HardwareLayout.maps("ar"))
        assertTrue(HardwareLayout.maps("fa"))
        assertFalse(HardwareLayout.maps("en"))
        assertFalse(HardwareLayout.maps("az"))
    }
}
