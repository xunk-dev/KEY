package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyPriorTest {
    @Test
    fun seedWordsCountOnceEach() {
        val prior = KeyPrior(listOf("table", "tabs", "tree"), WordStats())
        assertEquals(2, prior.weight("ta", "b"))
        assertEquals(0, prior.weight("ta", "x"))
    }

    @Test
    fun matchingIgnoresCaseAndNeedsALongEnoughWord() {
        val prior = KeyPrior(listOf("Table"), WordStats())
        assertEquals(1, prior.weight("TA", "b"))
        assertEquals(0, prior.weight("table", "s"))
    }

    @Test
    fun learnedWordsWeighMoreAndAreCapped() {
        val stats = WordStats()
        repeat(9) { stats.learn(null, "table") }
        val prior = KeyPrior(emptyList(), stats)
        assertEquals(10, prior.weight("ta", "b"))
    }

    @Test
    fun learnedWordsBeatASingleSeedMatch() {
        val stats = WordStats()
        repeat(3) { stats.learn(null, "tango") }
        val prior = KeyPrior(listOf("table"), stats)
        assertTrue(prior.weight("ta", "n") > prior.weight("ta", "b"))
    }
}
