package com.spacekeyboard.keyboard.core.macro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroPlaybackTest {
    @Test
    fun cursorLoopsAndCountsRounds() {
        val cursor = MacroCursor(listOf(MacroStep.Text("a"), MacroStep.Enter))
        assertEquals(MacroStep.Text("a"), cursor.next())
        assertEquals(1, cursor.round)
        assertEquals(MacroStep.Enter, cursor.next())
        assertTrue(cursor.atRoundStart)
        assertEquals(2, cursor.round)
        assertEquals(MacroStep.Text("a"), cursor.next())
    }

    @Test
    fun emptyCursorGivesNothing() {
        assertNull(MacroCursor(emptyList()).next())
    }

    @Test
    fun speedsGetFasterAndCycle() {
        val delays = MacroSpeed.entries.map { it.stepMs }
        assertEquals(delays.sortedDescending(), delays)
        assertEquals(MacroSpeed.SLOW, MacroSpeed.TURBO.next())
        assertEquals(MacroSpeed.NORMAL, MacroSpeed.byId("unknown"))
        assertTrue(MacroSpeed.TURBO.roundMs >= MacroSpeed.MIN_ROUND_MS)
    }

    @Test
    fun cursorFinishesAfterTheLastRoundOfALimit() {
        val cursor = MacroCursor(listOf(MacroStep.Text("a"), MacroStep.Enter))
        val played = ArrayList<MacroStep>()
        while (!cursor.finished(3)) cursor.next()?.let(played::add)
        assertEquals(6, played.size)
        assertEquals(4, cursor.round)
    }

    @Test
    fun cursorWithNoLimitNeverFinishes() {
        val cursor = MacroCursor(listOf(MacroStep.Enter))
        repeat(500) { cursor.next() }
        assertTrue(!cursor.finished(0))
    }

    @Test
    fun onceFinishesAfterTheFirstRound() {
        val cursor = MacroCursor(listOf(MacroStep.Enter, MacroStep.Delete))
        cursor.next()
        assertTrue(!cursor.finished(MacroRepeat.ONCE.rounds))
        cursor.next()
        assertTrue(cursor.finished(MacroRepeat.ONCE.rounds))
    }

    @Test
    fun repeatChoicesAreOnceThreeFiveTenAndNoEnd() {
        assertEquals(listOf(1, 3, 5, 10, 0), MacroRepeat.entries.map { it.rounds })
        assertEquals(listOf(true, true, true, true, false), MacroRepeat.entries.map { it.limited })
    }
}
