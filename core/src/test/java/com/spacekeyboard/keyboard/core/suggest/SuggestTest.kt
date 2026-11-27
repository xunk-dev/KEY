package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestTest {
    private val seed = listOf("hello", "help", "world", "مرحبا", "مرحلة")

    @Test
    fun contextSplitsPrefixAndPreviousWord() {
        assertEquals(TextContext("he", "say"), TextContext.parse("say he"))
        assertEquals(TextContext("", "hello"), TextContext.parse("hello "))
        assertEquals(TextContext("wor", null), TextContext.parse("wor"))
        assertNull(TextContext.parse("end. x").previous)
        assertNull(TextContext.parse("a\nb").previous)
    }

    @Test
    fun seedCompletesByPrefixInRankOrder() {
        val suggester = Suggester(seed, WordStats())
        assertEquals(listOf("hello", "help"), suggester.suggest(TextContext("hel", null)))
        assertEquals(listOf("مرحبا", "مرحلة"), suggester.suggest(TextContext("مرح", null)))
        assertTrue(suggester.suggest(TextContext("zzz", null)).isEmpty())
    }

    @Test
    fun learnedWordsOutrankTheSeed() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "helmet") }
        val result = Suggester(seed, stats).suggest(TextContext("hel", null))
        assertEquals("helmet", result.first())
    }

    @Test
    fun nextWordComesFromLearnedPairs() {
        val stats = WordStats()
        stats.learn("good", "morning")
        stats.learn("good", "morning")
        stats.learn("good", "night")
        val result = Suggester(seed, stats).suggest(TextContext("", "good"))
        assertEquals(listOf("morning", "night"), result)
        assertTrue(Suggester(seed, stats).suggest(TextContext("", null)).isEmpty())
    }

    @Test
    fun capitalisationFollowsWhatWasTyped() {
        val suggester = Suggester(seed, WordStats())
        assertEquals("Hello", suggester.suggest(TextContext("He", null)).first())
        assertEquals("HELLO", suggester.suggest(TextContext("HE", null)).first())
    }

    @Test
    fun statsRoundTripAndForget() {
        val stats = WordStats()
        stats.learn("a", "word")
        stats.learn("word", "next")
        val copy = WordStats.decode(stats.encode())
        assertEquals(stats.words, copy.words)
        assertEquals(stats.pairs, copy.pairs)
        copy.forget()
        assertTrue(copy.words.isEmpty() && copy.pairs.isEmpty())
    }

    @Test
    fun tooShortOrTooLongWordsAreIgnored() {
        val stats = WordStats()
        stats.learn(null, "a")
        stats.learn(null, "x".repeat(WordStats.MAX_WORD + 1))
        assertTrue(stats.words.isEmpty())
    }

    @Test
    fun typedWordComesFirstWhenUnknown() {
        val suggester = Suggester(seed, WordStats())
        assertEquals(listOf("helx"), suggester.suggest(TextContext("helx", null), includeTyped = true))
        assertEquals("hel", suggester.suggest(TextContext("hel", null), includeTyped = true).first())
        assertEquals(listOf("hello", "help"), suggester.suggest(TextContext("hel", null)))
    }

    @Test
    fun knownWordIsNotRepeatedAsTyped() {
        val result = Suggester(seed, WordStats()).suggest(TextContext("hello", null), includeTyped = true)
        assertTrue(result.none { it == "hello" })
        val oneLetter = Suggester(seed, WordStats()).suggest(TextContext("h", null), includeTyped = true)
        assertEquals("hello", oneLetter.first())
    }

    @Test
    fun arabicSpellingVariantsMatchAndCorrect() {
        val suggester = Suggester(listOf("أحب", "مدرسة", "الإنترنت", "على"), WordStats())
        assertEquals(listOf("أحب"), suggester.suggest(TextContext("احب", null)))
        assertEquals(listOf("مدرسة"), suggester.suggest(TextContext("مدرسه", null)))
        assertEquals(listOf("الإنترنت"), suggester.suggest(TextContext("الان", null)))
        assertEquals(listOf("على"), suggester.suggest(TextContext("علي", null)))
    }

    @Test
    fun blockedWordsDisappearAndAreNotRelearned() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "helmet") }
        stats.block("hello")
        stats.block("helmet")
        val result = Suggester(seed, stats).suggest(TextContext("hel", null))
        assertEquals(listOf("help"), result)
        stats.learn(null, "helmet")
        assertTrue(stats.words.isEmpty())
        stats.restoreBlocked()
        assertTrue(Suggester(seed, stats).suggest(TextContext("hel", null)).contains("hello"))
    }

    @Test
    fun blockedListSurvivesEncodeAndForgetClearsIt() {
        val stats = WordStats()
        stats.learn("a", "word")
        stats.block("hello")
        val copy = WordStats.decode(stats.encode())
        assertEquals(setOf("hello"), copy.blocked)
        assertEquals(stats.words, copy.words)
        copy.forget()
        assertTrue(copy.blocked.isEmpty())
    }

    @Test
    fun blockingRemovesLearnedPairs() {
        val stats = WordStats()
        stats.learn("good", "morning")
        stats.block("morning")
        assertTrue(stats.pairs.isEmpty())
    }

    @Test
    fun seededNextWordsAppearAfterAWordAndLearnedOnesWin() {
        val pairs = mapOf("السلام" to listOf("عليكم"), "thank" to listOf("you", "god"))
        val stats = WordStats()
        val suggester = Suggester(seed, stats, pairs)
        assertEquals(listOf("عليكم"), suggester.suggest(TextContext("", "السلام")))
        assertEquals(listOf("you", "god"), suggester.suggest(TextContext("", "Thank")))
        repeat(2) { stats.learn("thank", "goodness") }
        assertEquals("goodness", suggester.suggest(TextContext("", "thank")).first())
    }
}
