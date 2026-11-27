package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DialectTest {
    private val book = DialectBook(
        words = mapOf(
            Dialect.SHAMI to listOf("بدي", "هلق", "كتير", "وين"),
            Dialect.MASRI to listOf("عايز", "عاوز", "دلوقتي", "ازاي", "==", "طب"),
            Dialect.KHALEEJI to listOf("ابغى", "وايد", "شنو", "وين"),
        ),
        pairs = mapOf(Dialect.MASRI to mapOf("عايز" to listOf("اروح", "اكل"))),
    )

    @Test
    fun sharedAndBoostOnlyWordsAreNotMarkers() {
        assertNull(book.markerOf(ArabicFold.fold("وين")))
        assertNull(book.markerOf(ArabicFold.fold("طب")))
        assertEquals(Dialect.SHAMI, book.markerOf(ArabicFold.fold("بدي")))
        assertEquals(Dialect.KHALEEJI, book.markerOf(ArabicFold.fold("ابغي")))
    }

    @Test
    fun trackerPicksTheDialectFromText() {
        val tracker = DialectTracker(book)
        assertEquals(Dialect.SHAMI, tracker.detect("انا بدي اروح هلق"))
        assertEquals(Dialect.MASRI, tracker.detect("عايز اروح دلوقتي"))
        assertEquals(Dialect.KHALEEJI, tracker.detect("وايد حلو"))
        assertNull(tracker.detect("صباح الخير كيف حالك"))
        assertNull(tracker.detect("بدي عايز"))
        assertNull(tracker.detect("وين"))
    }

    @Test
    fun trackerRemembersThenFades() {
        val tracker = DialectTracker(book)
        tracker.observe("بدي")
        repeat(5) { tracker.observe("كتاب") }
        assertEquals(Dialect.SHAMI, tracker.detect())
        repeat(20) { tracker.observe("كتاب") }
        assertNull(tracker.detect())
        tracker.observe("عايز")
        tracker.reset()
        assertNull(tracker.detect())
    }

    @Test
    fun emptyBookNeverDetects() {
        assertNull(DialectTracker(DialectBook.EMPTY).detect("عايز بدي وايد"))
    }

    @Test
    fun dialectWordsRankAboveTheSeedOnlyWhenDetected() {
        val suggester = Suggester(listOf("عاصمة", "عادة", "عامل"), WordStats(), dialects = book)
        assertEquals(listOf("عاصمة", "عادة", "عامل"), suggester.suggest(TextContext("عا", null)))
        val boosted = suggester.suggest(TextContext("عا", null), dialect = Dialect.MASRI)
        assertEquals(listOf("عايز", "عاوز", "عاصمة"), boosted)
    }

    @Test
    fun otherDialectSeedWordsSinkBelowPlainWords() {
        val suggester = Suggester(listOf("هلا", "هلق", "هلاك"), WordStats(), dialects = book)
        assertEquals(listOf("هلا", "هلق", "هلاك"), suggester.suggest(TextContext("هل", null)))
        assertEquals(listOf("هلا", "هلاك", "هلق"), suggester.suggest(TextContext("هل", null), dialect = Dialect.MASRI))
        assertEquals(listOf("هلق", "هلا", "هلاك"), suggester.suggest(TextContext("هل", null), dialect = Dialect.SHAMI))
    }

    @Test
    fun learnedWordsStillBeatTheDialectBoost() {
        val stats = WordStats()
        repeat(2) { stats.learn(null, "عاملين") }
        val result = Suggester(listOf("عاصمة"), stats, dialects = book).suggest(TextContext("عا", null), dialect = Dialect.MASRI)
        assertEquals("عاملين", result.first())
    }

    @Test
    fun nextWordUsesTheDialectPairsOnlyWhenDetected() {
        val suggester = Suggester(emptyList(), WordStats(), dialects = book)
        assertEquals(listOf("اروح", "اكل"), suggester.suggest(TextContext("", "عايز"), dialect = Dialect.MASRI))
        assertTrue(suggester.suggest(TextContext("", "عايز")).isEmpty())
        assertTrue(suggester.suggest(TextContext("", "عايز"), dialect = Dialect.SHAMI).isEmpty())
    }

    @Test
    fun aTypedDialectWordIsNotTreatedAsUnknown() {
        val suggester = Suggester(emptyList(), WordStats(), dialects = book)
        assertTrue(suggester.suggest(TextContext("دلوقتي", null), includeTyped = true, dialect = Dialect.MASRI).isEmpty())
        assertEquals(listOf("xyz"), suggester.suggest(TextContext("xyz", null), includeTyped = true, dialect = Dialect.MASRI))
    }
}
