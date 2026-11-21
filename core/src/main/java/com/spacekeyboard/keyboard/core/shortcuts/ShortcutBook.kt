package com.spacekeyboard.keyboard.core.shortcuts

data class Expansion(val delete: Int, val insert: String)

class ShortcutBook(val entries: Map<String, String>) {
    fun expansionFor(before: String): Expansion? {
        if (entries.isEmpty()) return null
        val word = before.takeLastWhile { !it.isWhitespace() }
        if (word.isEmpty()) return null
        val text = entries[word] ?: entries[word.lowercase()] ?: return null
        return Expansion(word.length, text)
    }

    companion object {
        val EMPTY = ShortcutBook(emptyMap())
    }
}
