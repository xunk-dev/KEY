package com.spacekeyboard.keyboard.emoji.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GraphemeTailTest {
    private fun len(text: String) = GraphemeTail.lastLength(text)

    @Test
    fun emptyAndPlainText() {
        assertEquals(0, len(""))
        assertEquals(1, len("a"))
        assertEquals(1, len("ab"))
        assertEquals(1, len("ا"))
    }

    @Test
    fun surrogatePairIsOneStep() {
        assertEquals(2, len("x😀"))
    }

    @Test
    fun variationSelectorStaysWithItsBase() {
        assertEquals(2, len("x❤️"))
    }

    @Test
    fun skinToneStaysWithItsBase() {
        assertEquals(4, len("x👋🏽"))
    }

    @Test
    fun zwjSequenceIsOneStep() {
        val family = "👨‍👩‍👧"
        assertEquals(family.length, len("hi$family"))
        val heartFire = "❤️‍🔥"
        assertEquals(heartFire.length, len("a$heartFire"))
    }

    @Test
    fun zwjWithTonesIsOneStep() {
        val coder = "🧑🏽‍💻"
        assertEquals(coder.length, len("a$coder"))
    }

    @Test
    fun keycapIsOneStep() {
        assertEquals(3, len("a1️⃣"))
        assertEquals(2, len("a#⃣"))
    }

    @Test
    fun flagIsOneStepAndPairsAreRespected() {
        val ps = "🇵🇸"
        val us = "🇺🇸"
        assertEquals(4, len(ps))
        assertEquals(4, len(ps + us))
        assertEquals(2, len(ps + "🇺"))
    }

    @Test
    fun tagSequenceIsOneStep() {
        val england = "🏴" + "󠁧󠁢󠁥󠁮󠁧󠁿"
        assertEquals(england.length, len("a$england"))
    }

    @Test
    fun trailingJoinerDoesNotLoop() {
        assertEquals(1, len("a‍".substring(0, 1)))
        assertEquals(1, len("‍"))
    }

    @Test
    fun longHistoryOnlyLooksAtTheWindow() {
        val text = "x".repeat(500) + "😀"
        assertEquals(2, len(text))
    }
}
