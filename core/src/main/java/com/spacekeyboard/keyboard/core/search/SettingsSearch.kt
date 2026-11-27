package com.spacekeyboard.keyboard.core.search

import com.spacekeyboard.keyboard.core.suggest.ArabicFold

object SettingsSearch {
    fun <T> filter(query: String, items: List<T>, text: (T) -> String): List<T> {
        val words = ArabicFold.fold(query).split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) return items
        return items.filter { item ->
            val haystack = ArabicFold.fold(text(item))
            words.all { it in haystack }
        }
    }
}
