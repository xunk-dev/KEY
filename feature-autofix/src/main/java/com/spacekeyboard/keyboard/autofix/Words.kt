package com.spacekeyboard.keyboard.autofix

object Words {
    fun isLetter(ch: Char): Boolean = Character.isLetter(ch)

    fun isDigit(ch: Char): Boolean = Character.isDigit(ch)

    fun joins(text: String, word: String): Boolean =
        text.length > word.length && isLetter(text[text.length - word.length - 1])
}
