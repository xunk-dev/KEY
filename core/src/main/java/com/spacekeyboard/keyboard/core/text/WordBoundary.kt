package com.spacekeyboard.keyboard.core.text

object WordBoundary {
    fun lengthBefore(before: CharSequence): Int {
        var index = before.length
        while (index > 0 && before[index - 1].isWhitespace()) index--
        if (index == 0) return before.length.coerceAtLeast(1)
        val letters = isWordChar(before[index - 1])
        while (index > 0 && !before[index - 1].isWhitespace() && isWordChar(before[index - 1]) == letters) index--
        return (before.length - index).coerceAtLeast(1)
    }

    private fun isWordChar(ch: Char): Boolean = Character.isLetterOrDigit(ch) || ch == '\'' || ch == '\u0640' || Character.getType(ch) == Character.NON_SPACING_MARK.toInt()
}
