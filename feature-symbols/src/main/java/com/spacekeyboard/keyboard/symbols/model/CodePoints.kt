package com.spacekeyboard.keyboard.symbols.model

fun Int.asText(): String = StringBuilder().appendCodePoint(this).toString()

fun String.codePointList(): List<Int> {
    val result = ArrayList<Int>(length)
    var index = 0
    while (index < length) {
        val code = codePointAt(index)
        result += code
        index += Character.charCount(code)
    }
    return result
}
