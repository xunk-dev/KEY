package com.spacekeyboard.keyboard.autofix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TouchTest {
    private val off = AutoFixConfig(words = false)

    @Test
    fun zonesSplitTheKeyIntoNineCells() {
        assertEquals(4, Zone.of(0f, 0f))
        assertEquals(0, Zone.of(-0.4f, -0.4f))
        assertEquals(8, Zone.of(0.4f, 0.4f))
        assertEquals(2, Zone.of(0.4f, -0.4f))
        assertEquals(3, Zone.of(-0.4f, 0f))
    }

    private fun slipAt(model: ErrorModel, zone: Int, times: Int) {
        repeat(times) { Typing(off, model).zones('a' to 4, 'k' to zone).back().type("l", 4).space() }
    }

    @Test
    fun aConsistentlyMissedCornerIsRemapped() {
        val model = ErrorModel()
        slipAt(model, 8, 12)
        val session = Typing(off, model).zones('a' to 4, 'k' to 8)
        assertEquals("al", session.text)
    }

    @Test
    fun theSameKeyPressedInTheCenterIsLeftAlone() {
        val model = ErrorModel()
        slipAt(model, 8, 12)
        assertEquals("ak", Typing(off, model).zones('a' to 4, 'k' to 4).text)
    }

    @Test
    fun aMixedHabitIsNotRemapped() {
        val model = ErrorModel()
        repeat(40) { Typing(off, model).zones('a' to 4, 'k' to 8).space() }
        slipAt(model, 8, 12)
        assertEquals("ak", Typing(off, model).zones('a' to 4, 'k' to 8).text)
    }

    @Test
    fun fewPressesAreNotEnoughForARemap() {
        val model = ErrorModel()
        slipAt(model, 8, 5)
        assertNull(model.zoneFix('k', 8, AutoFixLevel.BOLD))
    }

    @Test
    fun repeatedUndosDropARemap() {
        val model = ErrorModel()
        slipAt(model, 8, 12)
        repeat(5) { Typing(off, model).zones('a' to 4, 'k' to 8).back() }
        assertEquals(4, model.zones.rule(Cell('k', 8), 'l')?.undone)
        assertNull(model.zoneFix('k', 8, AutoFixLevel.BALANCED))
        assertEquals("ak", Typing(off, model).zones('a' to 4, 'k' to 8).text)
    }

    @Test
    fun pressCountsAreHalvedBeforeTheyGrowForever() {
        val model = ErrorModel()
        repeat(ErrorModel.HALVE_AT) { model.press('k', 8) }
        assertEquals(ErrorModel.HALVE_AT / 2, model.presses[Cell('k', 8)])
    }
}
