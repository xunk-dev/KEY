package com.spacekeyboard.keyboard.core.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

sealed interface CalcOutcome {
    data class Value(val number: BigDecimal) : CalcOutcome

    data object DivideByZero : CalcOutcome

    data object Invalid : CalcOutcome
}

object CalcEval {
    private const val WORK_DIGITS = 24
    private const val SHOWN_DIGITS = 15
    private const val MAX_INTEGER_DIGITS = 30
    private const val OPERATIONS = "+*/%"
    private const val NONE = '\u0000'
    private val work = MathContext(WORK_DIGITS, RoundingMode.HALF_EVEN)
    private val shown = MathContext(SHOWN_DIGITS, RoundingMode.HALF_EVEN)
    private val hundred = BigDecimal(100)

    fun trimmed(expression: String): String = expression.trimEnd('+', '-', '*', '/', '.')

    fun hasOperation(expression: String): Boolean =
        expression.indices.any { expression[it] in OPERATIONS || (expression[it] == '-' && it > 0) }

    fun evaluate(expression: String): CalcOutcome {
        val text = trimmed(expression)
        if (text.isEmpty()) return CalcOutcome.Invalid
        return Parser(text).parse()
    }

    fun preview(expression: String): String? = (evaluate(expression) as? CalcOutcome.Value)?.let { plain(it.number) }

    fun plain(number: BigDecimal): String {
        val rounded = number.round(shown)
        if (rounded.signum() == 0) return "0"
        return rounded.stripTrailingZeros().toPlainString()
    }

    private class Factor(val value: BigDecimal, val percent: Boolean)

    private class ZeroDivision : RuntimeException()

    private class Parser(private val text: String) {
        private var pos = 0

        fun parse(): CalcOutcome = try {
            val value = expression()
            val rounded = value.round(shown)
            val digits = rounded.precision() - rounded.scale()
            if (pos != text.length || digits > MAX_INTEGER_DIGITS) CalcOutcome.Invalid else CalcOutcome.Value(rounded)
        } catch (_: ZeroDivision) {
            CalcOutcome.DivideByZero
        } catch (_: NumberFormatException) {
            CalcOutcome.Invalid
        } catch (_: ArithmeticException) {
            CalcOutcome.Invalid
        }

        private fun expression(): BigDecimal {
            var total = term().first
            while (peek() == '+' || peek() == '-') {
                val plus = text[pos++] == '+'
                val (value, percentOnly) = term()
                val delta = if (percentOnly) total.multiply(value, work) else value
                total = if (plus) total.add(delta, work) else total.subtract(delta, work)
            }
            return total
        }

        private fun term(): Pair<BigDecimal, Boolean> {
            val head = factor()
            var value = head.value
            var single = head.percent
            while (peek() == '*' || peek() == '/') {
                val times = text[pos++] == '*'
                val right = factor().value
                single = false
                value = if (times) value.multiply(right, work) else divide(value, right)
            }
            return value to single
        }

        private fun divide(left: BigDecimal, right: BigDecimal): BigDecimal {
            if (right.signum() == 0) throw ZeroDivision()
            return left.divide(right, work)
        }

        private fun factor(): Factor {
            if (peek() == '-') {
                pos++
                val inner = factor()
                return Factor(inner.value.negate(), inner.percent)
            }
            val number = number()
            if (peek() != '%') return Factor(number, false)
            pos++
            return Factor(number.divide(hundred, work), true)
        }

        private fun number(): BigDecimal {
            val start = pos
            while (pos < text.length && (text[pos] in '0'..'9' || text[pos] == '.')) pos++
            return BigDecimal(text.substring(start, pos).trimEnd('.'))
        }

        private fun peek(): Char = if (pos < text.length) text[pos] else NONE
    }
}
