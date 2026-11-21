package com.spacekeyboard.keyboard.emoji.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiParserTest {
    @Test
    fun decodesHexSequences() {
        assertEquals("😀", EmojiParser.decode("1F600"))
        assertEquals("❤️", EmojiParser.decode("2764 FE0F"))
        assertNull(EmojiParser.decode(""))
        assertNull(EmojiParser.decode("ZZZZ"))
        assertNull(EmojiParser.decode("110000"))
    }

    @Test
    fun groupsFollowTheFixedOrderNotTheFileOrder() {
        val lines = sequenceOf("flags\t1F3C1", "smileys\t1F600", "people\t1F44B")
        assertEquals(listOf("smileys", "people", "flags"), EmojiParser.parse(lines).map { it.id })
    }

    @Test
    fun skipsUnknownGroupsAndBrokenLines() {
        val lines = sequenceOf("mystery\t1F600", "smileys\tnothex", "smileys", "", "smileys\t1F600")
        val groups = EmojiParser.parse(lines)
        assertEquals(1, groups.size)
        assertEquals(1, groups[0].emojis.size)
    }

    @Test
    fun readsFiveToneVariants() {
        val tones = listOf("1F44B 1F3FB", "1F44B 1F3FC", "1F44B 1F3FD", "1F44B 1F3FE", "1F44B 1F3FF").joinToString("|")
        val emoji = EmojiParser.parse(sequenceOf("people\t1F44B\t$tones")).single().emojis.single()
        assertTrue(emoji.hasTones)
        assertEquals("👋🏽", emoji.forTone(SkinTone.MEDIUM))
        assertEquals("👋", emoji.forTone(SkinTone.NONE))
        assertEquals("👋🏿", emoji.forTone(SkinTone.DARK))
    }

    @Test
    fun incompleteToneListIsIgnored() {
        val emoji = EmojiParser.parse(sequenceOf("people\t1F44B\t1F44B 1F3FB|1F44B 1F3FC")).single().emojis.single()
        assertFalse(emoji.hasTones)
        assertEquals(emoji.text, emoji.forTone(SkinTone.LIGHT))
    }

    @Test
    fun emptyToneSlotFallsBackToTheBase() {
        val tones = listOf("1F44B 1F3FB", "", "", "", "").joinToString("|")
        val emoji = EmojiParser.parse(sequenceOf("people\t1F44B\t$tones")).single().emojis.single()
        assertEquals(emoji.text, emoji.forTone(SkinTone.MEDIUM))
        assertEquals("👋🏻", emoji.forTone(SkinTone.LIGHT))
    }

    @Test
    fun toneModifiersMapBack() {
        assertEquals(SkinTone.DARK, SkinTone.ofModifier(0x1F3FF))
        assertNull(SkinTone.ofModifier(0x1F600))
        assertEquals(SkinTone.NONE, SkinTone.byId("bogus"))
        assertEquals(SkinTone.MEDIUM_DARK, SkinTone.byId("medium_dark"))
        assertEquals(0, SkinTone.LIGHT.slot)
        assertEquals(4, SkinTone.DARK.slot)
    }
}
