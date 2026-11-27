package com.spacekeyboard.keyboard.core.calc

class CalcView(
    val expression: String,
    val result: String,
    val insertText: String?,
    val error: Boolean,
)

object CalcFormat {
    private const val NATIVE_ZERO = '\u0660'
    private const val NATIVE_DECIMAL = '\u066B'
    private const val NATIVE_GROUP = '\u066C'
    private const val NATIVE_PERCENT = '\u066A'
    private const val ARABIC_COMMA = '\u060C'
    private const val MINUS = '\u2212'
    private const val GROUP_SIZE = 3

    fun number(canonical: String, native: Boolean, grouping: Boolean): String {
        val negative = canonical.startsWith("-")
        val body = canonical.removePrefix("-")
        val dot = body.indexOf('.')
        val whole = if (dot < 0) body else body.substring(0, dot)
        val digits = if (grouping) grouped(whole, if (native) NATIVE_GROUP else ',') else whole
        val fraction = if (dot < 0) "" else (if (native) NATIVE_DECIMAL else '.') + body.substring(dot + 1)
        return (if (negative) "-" else "") + localDigits(digits + fraction, native)
    }

    fun label(key: CalcKey, native: Boolean): String = when {
        key.isDigit -> localDigits(key.code.toString(), native)
        key == CalcKey.DOT -> (if (native) NATIVE_DECIMAL else '.').toString()
        key == CalcKey.GROUP -> (if (native) ARABIC_COMMA else ',').toString()
        key == CalcKey.CLEAR -> "C"
        key == CalcKey.EQUALS -> "="
        key == CalcKey.DELETE -> ""
        else -> symbol(key.code, native).toString()
    }

    fun expression(canonical: String, native: Boolean, grouping: Boolean): String {
        val out = StringBuilder()
        var index = 0
        while (index < canonical.length) {
            val c = canonical[index]
            if (c in '0'..'9' || c == '.') {
                val end = (index until canonical.length).firstOrNull { canonical[it] !in '0'..'9' && canonical[it] != '.' } ?: canonical.length
                out.append(number(canonical.substring(index, end), native, grouping))
                index = end
            } else {
                val spaced = c in "+-*/" && index > 0
                out.append(if (spaced) " ${symbol(c, native)} " else symbol(c, native).toString())
                index++
            }
        }
        return out.toString().trimEnd()
    }

    fun view(state: CalcState, native: Boolean): CalcView {
        if (state.error) return CalcView("", "", null, true)
        val text = state.expression
        val operation = !state.done && CalcEval.hasOperation(text)
        val live = if (operation) CalcEval.preview(text) else null
        val canonical = when {
            live != null -> live
            operation -> CalcReducer.currentNumber(text).ifEmpty { "0" }
            else -> text.ifEmpty { "0" }
        }
        val small = when {
            state.done -> expression(state.previous, native, state.grouping) + " ="
            operation -> expression(text, native, state.grouping)
            else -> ""
        }
        val shown = number(canonical, native, state.grouping).replace('-', MINUS)
        return CalcView(small, shown, insertable(canonical, native, state), false)
    }

    private fun insertable(canonical: String, native: Boolean, state: CalcState): String? {
        if (canonical == "-" || canonical.isEmpty()) return null
        if (!state.done && state.expression.isEmpty()) return null
        return number(canonical.removeSuffix("."), native, state.grouping)
    }

    private fun symbol(c: Char, native: Boolean): Char = when (c) {
        '*' -> '\u00D7'
        '/' -> '\u00F7'
        '-' -> MINUS
        '%' -> if (native) NATIVE_PERCENT else '%'
        else -> c
    }

    private fun localDigits(text: String, native: Boolean): String {
        if (!native) return text
        return text.map { c -> if (c in '0'..'9') NATIVE_ZERO + (c - '0') else c }.joinToString("")
    }

    private fun grouped(whole: String, separator: Char): String {
        if (whole.length <= GROUP_SIZE) return whole
        val out = StringBuilder()
        whole.forEachIndexed { i, c ->
            if (i > 0 && (whole.length - i) % GROUP_SIZE == 0) out.append(separator)
            out.append(c)
        }
        return out.toString()
    }
}
