package com.spacekeyboard.keyboard.decorate

enum class Language { EN, AR }

interface Transform {
    fun text(source: String): String
    fun typed(before: String, input: String): String
}

class DecorStyle(val id: String, val language: Language, private val transform: Transform) {
    fun apply(source: String): String = transform.text(source)

    fun typed(before: String, input: String): String = transform.typed(before, input)
}

fun codePointString(cp: Int): String = String(Character.toChars(cp))
