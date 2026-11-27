package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.suggest.Suggester

internal class EmojiOffer(private val lookup: (String) -> String?) {
    var shown: String? = null
        private set

    fun owns(item: String): Boolean = item == shown

    fun clear() {
        shown = null
    }

    fun merge(words: List<String>, prefix: String, enabled: Boolean): List<String> {
        shown = if (enabled && prefix.isNotEmpty()) lookup(prefix) else null
        val emoji = shown ?: return words
        return words.take(Suggester.LIMIT - 1) + emoji
    }
}
