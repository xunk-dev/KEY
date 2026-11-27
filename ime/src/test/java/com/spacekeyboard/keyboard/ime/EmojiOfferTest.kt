package com.spacekeyboard.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiOfferTest {
    private val offer = EmojiOffer { word -> if (word == "happy") "😊" else null }

    @Test
    fun theEmojiComesLastAndKeepsTheStripAtThreeItems() {
        assertEquals(listOf("happy", "happen", "😊"), offer.merge(listOf("happy", "happen", "happier"), "happy", true))
        assertEquals("😊", offer.shown)
        assertTrue(offer.owns("😊"))
        assertFalse(offer.owns("happy"))
    }

    @Test
    fun shortListsJustGetTheEmoji() {
        assertEquals(listOf("happy", "😊"), offer.merge(listOf("happy"), "happy", true))
        assertEquals(listOf("😊"), offer.merge(emptyList(), "happy", true))
    }

    @Test
    fun nothingIsAddedWhenDisabledOrEmptyOrUnknown() {
        val words = listOf("a", "b")
        assertEquals(words, offer.merge(words, "happy", false))
        assertEquals(words, offer.merge(words, "", true))
        assertEquals(words, offer.merge(words, "sad", true))
        assertNull(offer.shown)
    }

    @Test
    fun aStaleOfferIsForgottenOnTheNextMerge() {
        offer.merge(listOf("happy"), "happy", true)
        offer.merge(listOf("sad"), "sad", true)
        assertFalse(offer.owns("😊"))
    }

    @Test
    fun clearForgetsTheOffer() {
        offer.merge(listOf("happy"), "happy", true)
        offer.clear()
        assertFalse(offer.owns("😊"))
    }
}
