package com.spacekeyboard.keyboard.symbols

import com.spacekeyboard.keyboard.symbols.data.ArtParser
import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.search.ArtBook
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtTest {
    private val sample = listOf(
        "%# space-keyboard-text-art 1",
        "%cat hearts",
        "",
        "❤️❤️",
        " ❤️",
        "",
        "%--",
        "%cat unknown",
        "ignored",
        "%cat dividers",
        "#not a comment",
        "%--",
        "@}->--",
    )

    @Test
    fun parserKeepsLayoutAndDropsBlankEdges() {
        val parsed = ArtParser.parse(sample)
        assertEquals(3, parsed.size)
        assertEquals(ArtCategory.HEARTS, parsed[0].category)
        assertEquals("❤️❤️\n ❤️", parsed[0].text)
    }

    @Test
    fun unknownCategoriesAndLeadingSymbolsAreHandled() {
        val parsed = ArtParser.parse(sample)
        assertFalse(parsed.any { it.text == "ignored" })
        assertEquals(listOf("#not a comment", "@}->--"), parsed.filter { it.category == ArtCategory.DIVIDERS }.map { it.text })
    }

    @Test
    fun bookGroupsByCategory() {
        val book = ArtBook(ArtParser.parse(sample))
        assertEquals(1, book.category(ArtCategory.HEARTS).size)
        assertTrue(book.category(ArtCategory.FACES).isEmpty())
    }

    @Test
    fun widthCountsEmojiWiderThanLetters() {
        val emoji = ArtParser.parse(listOf("%cat faces", "🟥🟥🟥")).first()
        val letters = ArtParser.parse(listOf("%cat lines", "abc")).first()
        assertTrue(emoji.widest() > letters.widest())
        assertEquals(1, letters.rowCount)
    }

    @Test
    fun shippedArtCoversEveryCategoryAndEmojiGridsAreRectangular() {
        val file = File("src/main/res/raw/text_art.txt")
        val art = ArtBook(ArtParser.parse(file.readLines(Charsets.UTF_8)))
        ArtCategory.entries.forEach { assertTrue(it.id, art.category(it).isNotEmpty()) }
        art.category(ArtCategory.ANIMALS).forEach { piece ->
            val widths = piece.lines.map { it.codePointCount(0, it.length) }.toSet()
            assertEquals(piece.text, 1, widths.size)
        }
    }
}
