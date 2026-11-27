package com.spacekeyboard.keyboard.core.suggest

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionStoreTest {
    private val store = SuggestionStore(FakePreferences().prefs)

    @Test
    fun emptyStoreLoadsNothing() {
        assertFalse(store.has())
        assertTrue(store.load().words.isEmpty())
        assertTrue(store.seed().isEmpty())
    }

    @Test
    fun savedStatsLoadBack() {
        val stats = WordStats()
        stats.learn(null, "hello")
        stats.learn("hello", "world")
        store.save(stats)
        val loaded = store.load()
        assertTrue(store.has())
        assertEquals(1, loaded.words["hello"])
        assertEquals(1, loaded.pairs["hello"]?.get("world"))
    }

    @Test
    fun clearForgetsEverything() {
        val stats = WordStats()
        stats.learn(null, "hello")
        store.save(stats)
        store.clear()
        assertFalse(store.has())
        assertTrue(store.load().words.isEmpty())
    }

    @Test
    fun observersHearAboutChanges() {
        var calls = 0
        val subscription = store.observe { calls++ }
        store.save(WordStats().also { it.learn(null, "hello") })
        assertEquals(1, calls)
        subscription.close()
        store.clear()
        assertEquals(1, calls)
    }

    @Test
    fun addedWordIsListedAndSuggested() {
        assertTrue(store.addWord("  Zebrafish "))
        assertEquals(listOf("zebrafish"), store.words())
        val result = Suggester(listOf("zebra"), store.load()).suggest(TextContext("zeb", null))
        assertEquals("zebrafish", result.first())
    }

    @Test
    fun badWordsAreRefused() {
        assertFalse(store.addWord(""))
        assertFalse(store.addWord("a"))
        assertFalse(store.addWord("two words"))
        assertFalse(store.addWord("x".repeat(WordStats.MAX_WORD + 1)))
        assertTrue(store.words().isEmpty())
    }

    @Test
    fun removedWordStaysGoneUntilAddedAgain() {
        val stats = WordStats()
        repeat(3) { stats.learn("a", "helmet") }
        store.save(stats)
        store.removeWord("Helmet")
        assertTrue(store.words().isEmpty())
        val reloaded = store.load()
        reloaded.learn(null, "helmet")
        assertTrue(reloaded.words.isEmpty())
        assertTrue(store.addWord("helmet"))
        assertEquals(listOf("helmet"), store.words())
        assertTrue(store.load().blocked.isEmpty())
    }

    @Test
    fun listOrdersByUseThenAlphabet() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "beta") }
        stats.learn(null, "alpha")
        stats.learn(null, "gamma")
        store.save(stats)
        assertEquals(listOf("beta", "alpha", "gamma"), store.words())
    }

    @Test
    fun addKeepsHigherCount() {
        val stats = WordStats()
        repeat(50) { stats.learn(null, "common") }
        store.save(stats)
        store.addWord("common")
        assertEquals(50, store.load().words["common"])
    }

    @Test
    fun exportListsOneWordPerLine() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "beta") }
        stats.learn(null, "alpha")
        store.save(stats)
        assertEquals("beta\nalpha\n", store.exportText())
        assertEquals("", SuggestionStore(FakePreferences().prefs).exportText())
    }

    @Test
    fun importAddsValidLinesAndSkipsTheRest() {
        val text = "\uFEFFهلا\r\nworld\n\n  Padded  \nx\ntwo words\nworld\n" + "y".repeat(25) + "\nbad\u0000word\nbro\uFFFDken\n"
        assertEquals(3, store.importText(text))
        assertEquals(setOf("هلا", "world", "padded"), store.words().toSet())
        assertEquals(WordStats.ADDED_COUNT, store.load().words["world"])
    }

    @Test
    fun importCountsOnlyNewWordsAndKeepsCounts() {
        val stats = WordStats()
        repeat(50) { stats.learn(null, "common") }
        store.save(stats)
        assertEquals(1, store.importText("common\nfresh\ncommon\n"))
        assertEquals(50, store.load().words["common"])
        assertEquals(0, store.importText("common\nfresh"))
    }

    @Test
    fun importTakesAWordOffTheBlockList() {
        store.save(WordStats().also { it.block("helmet") })
        assertEquals(1, store.importText("helmet"))
        assertTrue(store.load().blocked.isEmpty())
        assertEquals(listOf("helmet"), store.words())
    }

    @Test
    fun importStopsAtTheLimitsAndNeverEvictsLearnedWords() {
        val many = (1..WordStats.MAX_IMPORT + 50).joinToString("\n") { "word$it" }
        assertEquals(WordStats.MAX_IMPORT, store.importText(many))
        val stats = WordStats()
        repeat(WordStats.MAX_WORDS) { stats.words["learned$it"] = 1 }
        store.save(stats)
        assertEquals(0, store.importText("newcomer"))
        assertEquals(WordStats.MAX_WORDS, store.load().words.size)
    }

    @Test
    fun exportThenImportRoundTrips() {
        listOf("one1", "two2", "هلا").forEach { store.addWord(it) }
        val other = SuggestionStore(FakePreferences().prefs)
        assertEquals(3, other.importText(store.exportText()))
        assertEquals(store.words().toSet(), other.words().toSet())
    }

    @Test
    fun emptyOrBlankTextImportsNothing() {
        assertEquals(0, store.importText(""))
        assertEquals(0, store.importText("\n \n\t\n"))
        assertFalse(store.has())
    }
}
