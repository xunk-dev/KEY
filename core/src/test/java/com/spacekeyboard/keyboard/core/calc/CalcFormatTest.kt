package com.spacekeyboard.keyboard.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcFormatTest {
    private fun typed(text: String): CalcState = text.fold(CalcState()) { state, c ->
        CalcReducer.press(state, CalcKey.entries.first { it.code == c })
    }

    @Test
    fun latinDigitsAndSeparators() {
        assertEquals("1234567.5", CalcFormat.number("1234567.5", native = false, grouping = false))
        assertEquals("1,234,567.5", CalcFormat.number("1234567.5", native = false, grouping = true))
        assertEquals("123", CalcFormat.number("123", native = false, grouping = true))
        assertEquals("-1,000", CalcFormat.number("-1000", native = false, grouping = true))
    }

    @Test
    fun nativeDigitsAndSeparators() {
        assertEquals("٠١٢٣٤٥٦٧٨٩", CalcFormat.number("0123456789", native = true, grouping = false))
        assertEquals("١٬٢٣٤٬٥٦٧٫٥", CalcFormat.number("1234567.5", native = true, grouping = true))
        assertEquals("١٢٫", CalcFormat.number("12.", native = true, grouping = false))
    }

    @Test
    fun expressionGetsSpacedOperators() {
        assertEquals("480 × 1.5 + 20", CalcFormat.expression("480*1.5+20", native = false, grouping = false))
        assertEquals("٤٨٠ × ١٫٥ + ٢٠", CalcFormat.expression("480*1.5+20", native = true, grouping = false))
        assertEquals("−5 ÷ 2", CalcFormat.expression("-5/2", native = false, grouping = false))
        assertEquals("200 + 10%", CalcFormat.expression("200+10%", native = false, grouping = false))
        assertEquals("٢٠٠ + ١٠٪", CalcFormat.expression("200+10%", native = true, grouping = false))
        assertEquals("5 +", CalcFormat.expression("5+", native = false, grouping = false))
    }

    @Test
    fun liveViewShowsTheResultWhileTyping() {
        val view = CalcFormat.view(typed("480*1.5+20"), native = true)
        assertEquals("٤٨٠ × ١٫٥ + ٢٠", view.expression)
        assertEquals("٧٤٠", view.result)
        assertEquals("٧٤٠", view.insertText)
    }

    @Test
    fun aPlainNumberHasNoSmallLine() {
        val view = CalcFormat.view(typed("12."), native = false)
        assertEquals("", view.expression)
        assertEquals("12.", view.result)
        assertEquals("12", view.insertText)
    }

    @Test
    fun emptyStateShowsZeroAndCannotInsert() {
        val view = CalcFormat.view(CalcState(), native = false)
        assertEquals("0", view.result)
        assertNull(view.insertText)
    }

    @Test
    fun doneViewKeepsTheOldExpression() {
        val view = CalcFormat.view(typed("2+3="), native = false)
        assertEquals("2 + 3 =", view.expression)
        assertEquals("5", view.result)
        assertEquals("5", view.insertText)
    }

    @Test
    fun errorViewHasNothingToInsert() {
        val view = CalcFormat.view(typed("5/0="), native = false)
        assertTrue(view.error)
        assertNull(view.insertText)
    }

    @Test
    fun negativeResultsUseAMinusSignForDisplayAndAHyphenForInsert() {
        val view = CalcFormat.view(typed("2-5="), native = false)
        assertEquals("−3", view.result)
        assertEquals("-3", view.insertText)
    }

    @Test
    fun groupingAppliesToResultAndInsert() {
        val view = CalcFormat.view(typed(",1000*1000="), native = false)
        assertEquals("1,000,000", view.result)
        assertEquals("1,000,000", view.insertText)
    }

    @Test
    fun divisionByZeroWhileTypingFallsBackToTheOperand() {
        val view = CalcFormat.view(typed("5/0"), native = false)
        assertEquals("0", view.result)
    }

    @Test
    fun keyLabelsFollowTheDigitStyle() {
        assertEquals("7", CalcFormat.label(CalcKey.D7, native = false))
        assertEquals("٧", CalcFormat.label(CalcKey.D7, native = true))
        assertEquals(".", CalcFormat.label(CalcKey.DOT, native = false))
        assertEquals("٫", CalcFormat.label(CalcKey.DOT, native = true))
        assertEquals("،", CalcFormat.label(CalcKey.GROUP, native = true))
        assertEquals("%", CalcFormat.label(CalcKey.PERCENT, native = false))
        assertEquals("٪", CalcFormat.label(CalcKey.PERCENT, native = true))
        assertEquals("×", CalcFormat.label(CalcKey.MUL, native = false))
        assertEquals("÷", CalcFormat.label(CalcKey.DIV, native = true))
        assertEquals("\u2212", CalcFormat.label(CalcKey.SUB, native = false))
        assertEquals("+", CalcFormat.label(CalcKey.ADD, native = true))
        assertEquals("=", CalcFormat.label(CalcKey.EQUALS, native = true))
        assertEquals("C", CalcFormat.label(CalcKey.CLEAR, native = true))
    }
}
