package com.spacekeyboard.keyboard.symbols

import com.spacekeyboard.keyboard.symbols.data.SymbolData
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class GeneratedDataTest {
    private val directory = File("src/main/assets/symbols")

    private fun lines(name: String): List<String> = File(directory, name).readLines(Charsets.UTF_8)

    private fun data(): SymbolData {
        assumeTrue(File(directory, "symbols.tsv").exists())
        return SymbolData.parse(lines("symbols.tsv"), lines("featured.tsv"), lines("faces.tsv"), lines("face_categories.tsv"))
    }

    @Test
    fun everyGeneratedRowIsParsed() {
        val data = data()
        val rows = lines("symbols.tsv").count { !it.startsWith("#") && it.isNotEmpty() }
        assertEquals(rows, data.symbols.symbols.size)
        val faceRows = lines("faces.tsv").count { !it.startsWith("#") && it.isNotEmpty() }
        assertEquals(faceRows, data.faces.faces.size)
    }

    @Test
    fun theRequestedSymbolsAreFeaturedInOrder() {
        val featured = data().symbols.featured.map { it.text }
        assertEquals(listOf("€", "{", "}", "✓", "¶", "π", "®", "©", "∆", "★", "✪", "❥", "♧", "°", "●", "×", "√"), featured.take(17))
    }

    @Test
    fun everyCategoryHasSymbolsAndSearchWorks() {
        val data = data()
        SymbolCategory.entries.forEach { assertTrue(it.id, data.symbols.category(it).isNotEmpty()) }
        assertNotNull(data.symbols.find(0x2605))
        assertTrue(data.symbols.search("star").isNotEmpty())
        assertTrue(data.faces.search("^_^").isNotEmpty())
        assertTrue(data.faces.search("(◔‿◔)").isNotEmpty())
    }
}
