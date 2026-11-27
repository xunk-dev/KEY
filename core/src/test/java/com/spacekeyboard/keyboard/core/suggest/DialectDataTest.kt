package com.spacekeyboard.keyboard.core.suggest

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialectDataTest {
    private fun lines(name: String): List<String> =
        File("src/main/res/raw/$name.txt").readLines(Charsets.UTF_8).map { it.trim() }.filter { it.isNotEmpty() }

    private val words = mapOf(
        Dialect.SHAMI to lines("dialect_shami"),
        Dialect.MASRI to lines("dialect_masri"),
        Dialect.KHALEEJI to lines("dialect_khaleeji"),
    )
    private val book = DialectBook(words)

    @Test
    fun everyDialectHasEnoughDistinctiveWords() {
        Dialect.values().forEach { dialect ->
            val own = book.entries(dialect).count { book.markerOf(it.folded) == dialect }
            assertTrue("$dialect has only $own marker words", own >= 60)
        }
    }

    @Test
    fun noListRepeatsAWord() {
        words.forEach { (dialect, list) ->
            val real = list.filter { it != DialectBook.BOOST_ONLY }
            assertEquals(dialect.name, real.map { ArabicFold.fold(it) }.distinct().size, real.size)
        }
    }

    @Test
    fun entriesAreSingleWords() {
        words.forEach { (dialect, list) ->
            assertTrue(dialect.name, list.none { it.any(Char::isWhitespace) })
        }
    }

    @Test
    fun pairFilesAreWellFormed() {
        listOf("pairs_shami", "pairs_masri", "pairs_khaleeji").forEach { name ->
            val rows = lines(name).map { it.split(Regex("\\s+")) }
            assertTrue(name, rows.isNotEmpty() && rows.all { it.size >= 2 })
            assertEquals(name, rows.map { it[0] }.distinct().size, rows.size)
        }
    }

    @Test
    fun sampleSentencesAreRecognised() {
        val tracker = DialectTracker(book)
        assertEquals(Dialect.SHAMI, tracker.detect("انا بدي اروح هلق"))
        assertEquals(Dialect.SHAMI, tracker.detect("شو بدك تاكل كتير جوعان"))
        assertEquals(Dialect.MASRI, tracker.detect("ازيك يا باشا عامل ايه"))
        assertEquals(Dialect.MASRI, tracker.detect("عايز اروح دلوقتي"))
        assertEquals(Dialect.KHALEEJI, tracker.detect("شلونك شخبارك وايد اشتقت"))
        assertEquals(Dialect.KHALEEJI, tracker.detect("وش تبي الحين"))
    }

    @Test
    fun formalArabicIsLeftAlone() {
        val tracker = DialectTracker(book)
        assertEquals(null, tracker.detect("صباح الخير كيف حالك اليوم"))
        assertEquals(null, tracker.detect("ذهب أبي إلى العمل"))
        assertEquals(null, tracker.detect("هذه الدول العربية"))
    }
}
