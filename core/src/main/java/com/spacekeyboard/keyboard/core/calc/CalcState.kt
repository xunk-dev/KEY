package com.spacekeyboard.keyboard.core.calc

enum class CalcKey(val code: Char) {
    D0('0'), D1('1'), D2('2'), D3('3'), D4('4'), D5('5'), D6('6'), D7('7'), D8('8'), D9('9'),
    DOT('.'),
    ADD('+'), SUB('-'), MUL('*'), DIV('/'),
    PERCENT('%'),
    EQUALS('='),
    DELETE('<'),
    CLEAR('C'),
    GROUP(',');

    val isDigit: Boolean get() = code in '0'..'9'

    val isOperator: Boolean get() = this == ADD || this == SUB || this == MUL || this == DIV

    companion object {
        fun digit(value: Int): CalcKey = entries[value.coerceIn(0, 9)]
    }
}

data class CalcState(
    val expression: String = "",
    val previous: String = "",
    val done: Boolean = false,
    val error: Boolean = false,
    val grouping: Boolean = false,
)

object CalcReducer {
    private const val MAX_NUMBER_DIGITS = 15
    private const val MAX_LENGTH = 60
    private const val OPERATORS = "+-*/"

    fun press(state: CalcState, key: CalcKey): CalcState {
        if (key == CalcKey.GROUP) return state.copy(grouping = !state.grouping)
        if (key == CalcKey.CLEAR) return CalcState(grouping = state.grouping)
        val base = if (state.error) CalcState(grouping = state.grouping) else state
        return when {
            key.isDigit -> digit(base, key.code)
            key == CalcKey.DOT -> dot(base)
            key.isOperator -> operator(base, key.code)
            key == CalcKey.PERCENT -> percent(base)
            key == CalcKey.DELETE -> delete(base)
            else -> evaluate(base)
        }
    }

    fun currentNumber(text: String): String = text.takeLastWhile { it in '0'..'9' || it == '.' }

    private fun reopened(state: CalcState): CalcState =
        if (state.done) state.copy(previous = "", done = false) else state

    private fun digit(state: CalcState, c: Char): CalcState {
        val start = if (state.done) CalcState(grouping = state.grouping) else state
        val text = start.expression
        val number = currentNumber(text)
        return when {
            text.endsWith('%') || text.length >= MAX_LENGTH -> start
            number.count { it in '0'..'9' } >= MAX_NUMBER_DIGITS -> start
            number == "0" -> start.copy(expression = if (c == '0') text else text.dropLast(1) + c)
            else -> start.copy(expression = text + c)
        }
    }

    private fun dot(state: CalcState): CalcState {
        val start = if (state.done) CalcState(grouping = state.grouping) else state
        val text = start.expression
        val number = currentNumber(text)
        return when {
            text.endsWith('%') || text.length >= MAX_LENGTH -> start
            '.' in number -> start
            number.isEmpty() -> start.copy(expression = text + "0.")
            else -> start.copy(expression = text + ".")
        }
    }

    private fun operator(state: CalcState, c: Char): CalcState {
        val base = reopened(state)
        val text = base.expression
        return when {
            text.isEmpty() -> if (c == '-') base.copy(expression = "-") else base
            text == "-" -> base
            text.last() in OPERATORS || text.last() == '.' -> base.copy(expression = text.dropLast(1) + c)
            text.length >= MAX_LENGTH -> base
            else -> base.copy(expression = text + c)
        }
    }

    private fun percent(state: CalcState): CalcState {
        val base = reopened(state)
        val text = base.expression.removeSuffix(".")
        val allowed = text.isNotEmpty() && text.last() in '0'..'9' && text.length < MAX_LENGTH
        return if (allowed) base.copy(expression = text + "%") else base
    }

    private fun delete(state: CalcState): CalcState {
        val base = reopened(state)
        return base.copy(expression = base.expression.dropLast(1))
    }

    private fun evaluate(state: CalcState): CalcState {
        val text = state.expression
        if (state.done || !CalcEval.hasOperation(text)) return state
        return when (val outcome = CalcEval.evaluate(text)) {
            is CalcOutcome.Value -> state.copy(
                previous = CalcEval.trimmed(text),
                expression = CalcEval.plain(outcome.number),
                done = true,
            )
            else -> CalcState(grouping = state.grouping, error = true)
        }
    }
}
