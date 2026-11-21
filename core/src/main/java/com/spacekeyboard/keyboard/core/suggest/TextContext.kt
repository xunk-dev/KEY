package com.spacekeyboard.keyboard.core.suggest

data class TextContext(val prefix: String, val previous: String?) {
    companion object {
        fun parse(before: String): TextContext {
            val prefix = before.takeLastWhile(::isWordChar)
            val rest = before.dropLast(prefix.length)
            val gap = rest.takeLastWhile { it.isWhitespace() }
            val head = rest.dropLast(gap.length)
            val previous = if (gap.isEmpty() || gap.contains('\n')) null else head.takeLastWhile(::isWordChar).ifEmpty { null }
            return TextContext(prefix, previous)
        }

        fun isWordChar(ch: Char): Boolean = Character.isLetter(ch) || ch == '\''
    }
}
