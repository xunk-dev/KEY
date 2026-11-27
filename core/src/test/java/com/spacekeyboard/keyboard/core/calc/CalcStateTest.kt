package com.spacekeyboard.keyboard.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcStateTest {
    private fun run(vararg keys: CalcKey): CalcState = keys.fold(CalcState()) { state, key -> CalcReducer.press(state, key) }

    private fun typed(text: String): CalcState = text.fold(CalcState()) { state, c ->
        CalcReducer.press(state, CalcKey.entries.first { it.code == c })
    }

    @Test
    fun digitsBuildTheExpression() {
        assertEquals("480", typed("480").expression)
    }

    @Test
    fun leadingZerosCollapse() {
        assertEquals("0", typed("00").expression)
        assertEquals("5", typed("05").expression)
        assertEquals("10", typed("10").expression)
    }

    @Test
    fun dotHandlingKeepsNumbersValid() {
        assertEquals("0.", typed(".").expression)
        assertEquals("1.5", typed("1.5").expression)
        assertEquals("1.5", typed("1..5").expression)
        assertEquals("1.5+0.", typed("1.5+.").expression)
    }

    @Test
    fun operatorsReplaceEachOther() {
        assertEquals("5*", typed("5+*").expression)
        assertEquals("5-", typed("5*-").expression)
        assertEquals("5+", typed("5.+").expression)
    }

    @Test
    fun onlyMinusCanStartAnExpression() {
        assertEquals("", typed("+").expression)
        assertEquals("", typed("*").expression)
        assertEquals("-", typed("-").expression)
        assertEquals("-", typed("--").expression)
        assertEquals("-5", typed("-5").expression)
    }

    @Test
    fun percentNeedsANumberBeforeIt() {
        assertEquals("", typed("%").expression)
        assertEquals("5+", typed("5+%").expression)
        assertEquals("50%", typed("50%").expression)
        assertEquals("50%", typed("50%%").expression)
        assertEquals("50%", typed("50.%").expression)
        assertEquals("50%", typed("50%5").expression)
    }

    @Test
    fun equalsFinishesAndKeepsTheOldExpression() {
        val state = typed("480*1.5+20=")
        assertTrue(state.done)
        assertEquals("740", state.expression)
        assertEquals("480*1.5+20", state.previous)
    }

    @Test
    fun equalsOnAPlainNumberDoesNothing() {
        val state = typed("42=")
        assertFalse(state.done)
        assertEquals("42", state.expression)
    }

    @Test
    fun digitAfterEqualsStartsOver() {
        val state = typed("2+3=7")
        assertFalse(state.done)
        assertEquals("7", state.expression)
        assertEquals("", state.previous)
    }

    @Test
    fun operatorAfterEqualsContinuesFromTheResult() {
        val state = typed("2+3=*4=")
        assertEquals("20", state.expression)
        assertEquals("5*4", state.previous)
    }

    @Test
    fun deleteRemovesOneCharacter() {
        assertEquals("48", typed("480<").expression)
        assertEquals("", typed("<").expression)
        assertEquals("", typed("5<<").expression)
    }

    @Test
    fun deleteAfterEqualsEditsTheResult() {
        val state = typed("2+3=<")
        assertFalse(state.done)
        assertEquals("", state.expression)
        assertEquals("74", typed("480*1.5+20=<").expression)
    }

    @Test
    fun clearResetsButKeepsGrouping() {
        val cleared = CalcReducer.press(typed(",12+3"), CalcKey.CLEAR)
        assertEquals("", cleared.expression)
        assertTrue(cleared.grouping)
    }

    @Test
    fun groupingToggles() {
        assertTrue(run(CalcKey.GROUP).grouping)
        assertFalse(run(CalcKey.GROUP, CalcKey.GROUP).grouping)
    }

    @Test
    fun divisionByZeroGivesAnErrorThatAnyDigitClears() {
        val failed = typed("5/0=")
        assertTrue(failed.error)
        assertEquals("", failed.expression)
        val next = CalcReducer.press(failed, CalcKey.D7)
        assertFalse(next.error)
        assertEquals("7", next.expression)
    }

    @Test
    fun anOperatorAfterAnErrorDoesNothing() {
        val state = CalcReducer.press(typed("5/0="), CalcKey.ADD)
        assertFalse(state.error)
        assertEquals("", state.expression)
    }

    @Test
    fun numbersAreCappedAtFifteenDigits() {
        assertEquals("123456789012345", typed("1234567890123456789").expression)
        assertEquals("123456789012345+1", typed("1234567890123456789+1").expression)
    }

    @Test
    fun trailingOperatorIsDroppedByEquals() {
        val state = typed("5+=")
        assertTrue(state.done)
        assertEquals("5", state.expression)
    }
}
