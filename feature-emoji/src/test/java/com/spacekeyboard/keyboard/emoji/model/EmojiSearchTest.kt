package com.spacekeyboard.keyboard.emoji.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiSearchTest {
    private val grin = Emoji("😀")
    private val joy = Emoji("😂")
    private val cat = Emoji("🐱")
    private val heart = Emoji("❤️")

    private val english = mapOf(
        grin.text to EmojiName("grinning face", listOf("face", "grin", "smile")),
        joy.text to EmojiName("face with tears of joy", listOf("laugh", "tears", "funny")),
        cat.text to EmojiName("cat face", listOf("pet", "kitten")),
        heart.text to EmojiName("red heart", listOf("love")),
    )
    private val arabic = mapOf(
        grin.text to EmojiName("وجه مبتسم", listOf("ابتسامة", "سعيد")),
        cat.text to EmojiName("وجه قطة", listOf("قطة", "حيوان أليف")),
        heart.text to EmojiName("قلب أحمر", listOf("حب", "قلب")),
    )
    private val search = EmojiSearch(listOf(grin, joy, cat, heart), listOf(english, arabic))

    @Test
    fun emptyQueryFindsNothing() {
        assertTrue(search.find("").isEmpty())
        assertTrue(search.find("   ").isEmpty())
        assertTrue(search.find("!!").isEmpty())
    }

    @Test
    fun findsByNameWordAndPrefix() {
        assertEquals(listOf(grin, joy, cat), search.find("face"))
        assertEquals(listOf(grin), search.find("grinn"))
        assertEquals(listOf(heart), search.find("RED"))
    }

    @Test
    fun nameMatchesRankAboveKeywordMatches() {
        val result = search.find("smile")
        assertEquals(grin, result.first())
        assertEquals(listOf(joy), search.find("laugh"))
    }

    @Test
    fun everyTokenMustMatch() {
        assertEquals(listOf(joy), search.find("face tears"))
        assertTrue(search.find("face tears cat").isEmpty())
    }

    @Test
    fun findsInArabicIgnoringDiacriticsAndAlefForms() {
        assertEquals(listOf(cat), search.find("قطّة"))
        assertEquals(listOf(grin), search.find("ابتسامه"))
        assertEquals(listOf(heart), search.find("حُب"))
        assertEquals(listOf(grin, cat), search.find("وجه"))
        assertEquals(listOf(cat), search.find("أليف"))
        assertEquals(listOf(cat), search.find("اليف"))
    }

    @Test
    fun unknownWordsFindNothing() {
        assertTrue(search.find("spaceship").isEmpty())
    }

    @Test
    fun limitCapsTheResults() {
        assertEquals(2, search.find("face", limit = 2).size)
    }

    @Test
    fun emojiWithoutNamesAreNeverReturned() {
        val lonely = EmojiSearch(listOf(Emoji("🚀")), listOf(english))
        assertTrue(lonely.find("rocket").isEmpty())
    }

    @Test
    fun normalizeFoldsArabicVariants() {
        assertEquals("ابتسامه", SearchText.normalize("إِبْتِسَامَة"))
        assertEquals("علي", SearchText.normalize("على"))
        assertEquals(listOf("face", "with"), SearchText.words("Face, with!"))
        assertEquals("مرحبا", SearchText.normalize("مرحبـــا"))
    }
}
