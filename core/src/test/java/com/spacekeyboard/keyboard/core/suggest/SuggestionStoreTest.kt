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
}
