package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScopedStatsTest {
    @Test
    fun countsAreKeptPerScopeAndLowercased() {
        val stats = ScopedStats()
        stats.learn("app:a", "Hello")
        stats.learn("app:a", "hello")
        stats.learn("app:b", "hello")
        assertEquals(2, stats.counts("app:a")["hello"])
        assertEquals(1, stats.counts("app:b")["hello"])
        assertTrue(stats.counts("app:c").isEmpty())
    }

    @Test
    fun tooShortOrBadWordsAreIgnored() {
        val stats = ScopedStats()
        stats.learn("app:a", "a")
        stats.learn("app:a", "two words")
        stats.learn("", "hello")
        assertTrue(stats.scopes.isEmpty())
    }

    @Test
    fun oldestScopeIsDroppedAtTheLimit() {
        val stats = ScopedStats()
        repeat(ScopedStats.MAX_SCOPES + 1) { stats.learn("app:$it", "word") }
        assertEquals(ScopedStats.MAX_SCOPES, stats.scopes.size)
        assertFalse(stats.scopes.containsKey("app:0"))
        assertTrue(stats.scopes.containsKey("app:${ScopedStats.MAX_SCOPES}"))
    }

    @Test
    fun usingAScopeKeepsItFromBeingDropped() {
        val stats = ScopedStats()
        repeat(ScopedStats.MAX_SCOPES) { stats.learn("app:$it", "word") }
        stats.learn("app:0", "again")
        stats.learn("app:new", "word")
        assertTrue(stats.scopes.containsKey("app:0"))
        assertFalse(stats.scopes.containsKey("app:1"))
    }

    @Test
    fun overflowingAScopeKeepsTheFrequentWords() {
        val stats = ScopedStats()
        repeat(5) { stats.learn("app:a", "frequent") }
        repeat(ScopedStats.MAX_WORDS + 5) { stats.learn("app:a", "word$it") }
        val kept = stats.counts("app:a")
        assertTrue(kept.size <= ScopedStats.MAX_WORDS)
        assertEquals(5, kept["frequent"])
    }

    @Test
    fun removeDeletesFromEveryScopeAndDropsEmptyOnes() {
        val stats = ScopedStats()
        stats.learn("app:a", "secret")
        stats.learn("app:b", "secret")
        stats.learn("app:b", "other")
        stats.remove("Secret")
        assertFalse(stats.scopes.containsKey("app:a"))
        assertEquals(setOf("other"), stats.counts("app:b").keys)
    }

    @Test
    fun encodeAndDecodeRoundTripKeepingOrder() {
        val stats = ScopedStats()
        stats.learn("app:a", "alpha")
        stats.learn("kind:name", "layla")
        stats.learn("kind:name", "layla")
        stats.learn("app:a", "beta")
        val loaded = ScopedStats.decode(stats.encode())
        assertEquals(stats.scopes.keys.toList(), loaded.scopes.keys.toList())
        assertEquals(2, loaded.counts("kind:name")["layla"])
        assertEquals(1, loaded.counts("app:a")["beta"])
    }

    @Test
    fun decodeSkipsDamagedLines() {
        val raw = "app:a\talpha\t3\nbroken\napp:a\tbeta\tx\napp:a\t\t2\napp:a\tUPPER\t2\n\tgamma\t1"
        val loaded = ScopedStats.decode(raw)
        assertEquals(mapOf("alpha" to 3), loaded.counts("app:a"))
        assertEquals(1, loaded.scopes.size)
    }

    @Test
    fun forgetClearsEverything() {
        val stats = ScopedStats()
        stats.learn("app:a", "alpha")
        stats.forget()
        assertTrue(stats.scopes.isEmpty())
        assertEquals("", stats.encode())
    }
}
