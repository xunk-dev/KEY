package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggesterPersonalTest {
    private val seed = listOf("hello", "help", "world")

    @Test
    fun defaultKeepsTheOldBehaviour() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "helmet") }
        val suggester = Suggester(seed, stats)
        assertEquals(suggester.suggest(TextContext("hel", null)), suggester.suggest(TextContext("hel", null), personal = Personal.DEFAULT))
    }

    @Test
    fun wordsUsedInThisScopeComeFirst() {
        val result = Suggester(seed, WordStats()).suggest(TextContext("he", null), personal = Personal(true, mapOf("hesham" to 2)))
        assertEquals("hesham", result.first())
    }

    @Test
    fun scopeUseOutweighsAFewExtraGlobalUses() {
        val stats = WordStats()
        repeat(4) { stats.learn(null, "helmet") }
        repeat(2) { stats.learn(null, "helium") }
        val result = Suggester(seed, stats).suggest(TextContext("hel", null), personal = Personal(true, mapOf("helium" to 1)))
        assertEquals("helium", result.first())
        assertTrue("helmet" in result)
    }

    @Test
    fun noneIgnoresLearnedWordsAndPairs() {
        val stats = WordStats()
        repeat(3) { stats.learn("good", "helmet") }
        val suggester = Suggester(seed, stats)
        assertFalse("helmet" in suggester.suggest(TextContext("hel", null), personal = Personal.NONE))
        assertTrue(suggester.suggest(TextContext("", "good"), personal = Personal.NONE).isEmpty())
        assertEquals(listOf("helmet"), suggester.suggest(TextContext("", "good")))
    }

    @Test
    fun separateScopeLeavesGlobalWordsOut() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "helmet") }
        val result = Suggester(seed, stats).suggest(TextContext("hel", null), personal = Personal(false, mapOf("helen" to 2)))
        assertEquals("helen", result.first())
        assertFalse("helmet" in result)
    }

    @Test
    fun blockedWordsStayOutOfScopedSuggestions() {
        val stats = WordStats()
        stats.block("hesham")
        val result = Suggester(seed, stats).suggest(TextContext("he", null), personal = Personal(true, mapOf("hesham" to 5)))
        assertFalse("hesham" in result)
    }

    @Test
    fun aScopedWordIsNotOfferedAsAnUnknownTypedWord() {
        val result = Suggester(seed, WordStats()).suggest(
            TextContext("hesham", null),
            includeTyped = true,
            personal = Personal(true, mapOf("hesham" to 2)),
        )
        assertFalse(result.contains("hesham"))
    }
}
