package com.spacekeyboard.keyboard.core.text

import com.spacekeyboard.keyboard.core.text.ScriptGuess.Script
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScriptGuessTest {
    private val both = listOf("ar", "en")

    @Test
    fun arabicTextIsArabic() {
        assertEquals(Script.ARABIC, ScriptGuess.of("\u0643\u064A\u0641 \u062D\u0627\u0644\u0643 \u0627\u0644\u064A\u0648\u0645"))
    }

    @Test
    fun englishTextIsLatin() {
        assertEquals(Script.LATIN, ScriptGuess.of("see you tomorrow at the office"))
    }

    @Test
    fun tooFewLettersGivesNoAnswer() {
        assertNull(ScriptGuess.of(""))
        assertNull(ScriptGuess.of("ok"))
        assertNull(ScriptGuess.of("12 34 !!"))
    }

    @Test
    fun mixedTextGivesNoAnswer() {
        assertNull(ScriptGuess.of("\u0630\u0647\u0628\u062A \u0625\u0644\u0649 Starbucks mall"))
    }

    @Test
    fun onlyTheLastWindowCounts() {
        val old = "\u0645\u0631\u062D\u0628\u0627 ".repeat(30)
        assertEquals(Script.LATIN, ScriptGuess.of(old + "this is the new english sentence here"))
    }

    @Test
    fun arabicTextOnEnglishKeyboardSwitchesToArabic() {
        assertEquals("ar", ScriptGuess.layoutFor(Script.ARABIC, "en", both))
    }

    @Test
    fun englishTextOnArabicKeyboardSwitchesToEnglish() {
        assertEquals("en", ScriptGuess.layoutFor(Script.LATIN, "ar", both))
        assertEquals("en", ScriptGuess.layoutFor(Script.LATIN, "fa", listOf("ar", "en", "fa")))
    }

    @Test
    fun matchingKeyboardStaysAndArabiziIsLeftAlone() {
        assertNull(ScriptGuess.layoutFor(Script.ARABIC, "ar", both))
        assertNull(ScriptGuess.layoutFor(Script.ARABIC, "fa", listOf("ar", "en", "fa")))
        assertNull(ScriptGuess.layoutFor(Script.ARABIC, "az", listOf("ar", "en", "az")))
        assertNull(ScriptGuess.layoutFor(Script.LATIN, "az", listOf("ar", "en", "az")))
        assertNull(ScriptGuess.layoutFor(Script.LATIN, "en", both))
        assertNull(ScriptGuess.layoutFor(null, "ar", both))
    }

    @Test
    fun neverSwitchesToALanguageThatIsNotEnabled() {
        assertNull(ScriptGuess.layoutFor(Script.LATIN, "ar", listOf("ar")))
        assertEquals("fa", ScriptGuess.layoutFor(Script.ARABIC, "en", listOf("fa", "en")))
    }
}
