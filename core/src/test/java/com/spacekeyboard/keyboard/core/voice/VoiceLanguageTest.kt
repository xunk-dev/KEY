package com.spacekeyboard.keyboard.core.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceLanguageTest {
    @Test
    fun arabicUsesTheChosenDialect() {
        assertEquals("ar-EG", VoiceLanguage.tagFor("ar", "ar-EG"))
        assertEquals("ar-KW", VoiceLanguage.tagFor("az", "ar-KW"))
    }

    @Test
    fun unknownOrMissingDialectFallsBackToDefault() {
        assertEquals(VoiceLanguage.DEFAULT_ARABIC, VoiceLanguage.tagFor("ar", null))
        assertEquals(VoiceLanguage.DEFAULT_ARABIC, VoiceLanguage.tagFor("ar", "xx-YY"))
        assertEquals(VoiceLanguage.DEFAULT_ARABIC, VoiceLanguage.arabicOrDefault(""))
    }

    @Test
    fun otherLanguagesIgnoreTheDialect() {
        assertEquals("fa-IR", VoiceLanguage.tagFor("fa", "ar-EG"))
        assertEquals("en-US", VoiceLanguage.tagFor("en", "ar-EG"))
        assertEquals("en-US", VoiceLanguage.tagFor("tr", null))
    }

    @Test
    fun mixedSpeechHintsEnglishOnlyForNonEnglish() {
        assertEquals(listOf("en-US"), VoiceLanguage.extraLanguages("ar-SA"))
        assertEquals(listOf("en-US"), VoiceLanguage.extraLanguages("fa-IR"))
        assertTrue(VoiceLanguage.extraLanguages("en-US").isEmpty())
    }

    @Test
    fun defaultDialectIsInTheList() {
        assertTrue(VoiceLanguage.DEFAULT_ARABIC in VoiceLanguage.arabicTags)
        assertEquals(VoiceLanguage.arabicTags.size, VoiceLanguage.arabicTags.toSet().size)
    }
}
