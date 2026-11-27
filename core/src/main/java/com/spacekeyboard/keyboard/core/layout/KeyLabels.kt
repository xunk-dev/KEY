package com.spacekeyboard.keyboard.core.layout

object KeyLabels {
    const val TATWEEL = '\u0640'
    const val ZWNJ = '\u200C'
    const val ZWNJ_KEY = "\u0640\u200C\u0640"

    fun output(label: String): String = when {
        label == ZWNJ_KEY -> ZWNJ.toString()
        label.length == 2 && label[0] == TATWEEL && isMark(label[1]) -> label.substring(1)
        else -> label
    }

    fun isMark(ch: Char): Boolean = Character.getType(ch) == Character.NON_SPACING_MARK.toInt()

    fun isMarkOnly(text: String): Boolean = text.isNotEmpty() && text.all { isMark(it) || it == ZWNJ }
}
