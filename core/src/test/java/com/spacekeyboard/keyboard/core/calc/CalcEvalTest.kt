package com.spacekeyboard.keyboard.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcEvalTest {
    private fun value(expression: String): String? = CalcEval.preview(expression)

    @Test
    fun multiplicationBindsTighterThanAddition() {
        assertEquals("740", value("480*1.5+20"))
        assertEquals("14", value("2+3*4"))
    }

    @Test
    fun sameLevelOperatorsRunLeftToRight() {
        assertEquals("3", value("10-4-3"))
        assertEquals("2", value("8/2/2"))
    }

    @Test
    fun percentIsRelativeAfterPlusAndMinus() {
        assertEquals("220", value("200+10%"))
        assertEquals("180", value("200-10%"))
        assertEquals("20", value("200*10%"))
        assertEquals("0.5", value("50%"))
        assertEquals("-0.05", value("-5%"))
    }

    @Test
    fun decimalsStayExact() {
        assertEquals("0.3", value("0.1+0.2"))
        assertEquals("0.333333333333333", value("1/3"))
        assertEquals("0.666666666666667", value("2/3"))
        assertEquals("2.5", value("5/2"))
    }

    @Test
    fun trailingOperatorsAndDotsAreIgnored() {
        assertEquals("5", value("5+"))
        assertEquals("5", value("5."))
        assertEquals("12", value("12*"))
    }

    @Test
    fun incompleteOrWrongInputIsInvalid() {
        assertEquals(CalcOutcome.Invalid, CalcEval.evaluate(""))
        assertEquals(CalcOutcome.Invalid, CalcEval.evaluate("+5"))
        assertEquals(CalcOutcome.Invalid, CalcEval.evaluate("1.2.3+1"))
        assertNull(value("*"))
    }

    @Test
    fun divisionByZeroIsReported() {
        assertEquals(CalcOutcome.DivideByZero, CalcEval.evaluate("5/0"))
        assertEquals(CalcOutcome.DivideByZero, CalcEval.evaluate("1+2/0.0"))
    }

    @Test
    fun negativeNumbersWork() {
        assertEquals("-3", value("-5+2"))
        assertEquals("-10", value("-5*2"))
        assertEquals("0", value("5-5"))
    }

    @Test
    fun hugeResultsAreRejected() {
        assertEquals("999999999999998000000000000000", value("999999999999999*999999999999999"))
        assertEquals(CalcOutcome.Invalid, CalcEval.evaluate("999999999999999*999999999999999*10"))
    }

    @Test
    fun operationDetectionIgnoresALeadingMinus() {
        assertFalse(CalcEval.hasOperation("-5"))
        assertFalse(CalcEval.hasOperation("12.5"))
        assertTrue(CalcEval.hasOperation("5-3"))
        assertTrue(CalcEval.hasOperation("50%"))
        assertTrue(CalcEval.hasOperation("-5*2"))
    }
}
