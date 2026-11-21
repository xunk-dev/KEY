package com.spacekeyboard.keyboard.emoji.model

object Recents {
    const val LIMIT = 60
    private const val SEPARATOR = "\n"

    fun record(current: List<String>, text: String): List<String> {
        if (text.isEmpty()) return current
        return (listOf(text) + current.filter { it != text }).take(LIMIT)
    }

    fun encode(items: List<String>): String = items.joinToString(SEPARATOR)

    fun decode(raw: String?): List<String> =
        raw.orEmpty().split(SEPARATOR).filter { it.isNotEmpty() }.distinct().take(LIMIT)
}
