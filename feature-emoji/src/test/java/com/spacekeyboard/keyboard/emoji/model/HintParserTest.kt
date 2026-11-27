package com.spacekeyboard.keyboard.emoji.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HintParserTest {
    @Test
    fun parsesWordsAndSeveralEmoji() {
        val parsed = HintParser.parse(sequenceOf("happy\t😊 😄", "pizza\t🍕"))
        assertEquals(listOf("😊", "😄"), parsed["happy"])
        assertEquals(listOf("🍕"), parsed["pizza"])
    }

    @Test
    fun wordsAreNormalisedLikeSearchText() {
        val parsed = HintParser.parse(sequenceOf("أسد\t🦁", "مبسوطة\t😊"))
        assertEquals(listOf("🦁"), parsed["اسد"])
        assertEquals(listOf("😊"), parsed["مبسوطه"])
    }

    @Test
    fun blankMalformedAndDuplicateLinesAreSkipped() {
        val parsed = HintParser.parse(sequenceOf("", "nomoji", "word\t", "\t😊", "cat\t🐱", "CAT\t🐶"))
        assertEquals(setOf("cat"), parsed.keys)
        assertEquals(listOf("🐱"), parsed["cat"])
        assertTrue(HintParser.parse(emptySequence()).isEmpty())
    }
}
