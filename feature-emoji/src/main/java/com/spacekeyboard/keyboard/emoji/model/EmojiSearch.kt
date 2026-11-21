package com.spacekeyboard.keyboard.emoji.model

class EmojiSearch(emojis: List<Emoji>, names: List<Map<String, EmojiName>>) {
    private class Entry(val emoji: Emoji, val order: Int, val nameWords: List<String>, val keywordWords: List<String>)

    private val entries: List<Entry> = emojis.mapIndexed { index, emoji ->
        val found = names.mapNotNull { it[emoji.text] }
        Entry(
            emoji,
            index,
            found.flatMap { SearchText.words(it.name) }.distinct(),
            found.flatMap { n -> n.keywords.flatMap { SearchText.words(it) } }.distinct(),
        )
    }

    fun find(query: String, limit: Int = LIMIT): List<Emoji> {
        val tokens = SearchText.words(query)
        if (tokens.isEmpty()) return emptyList()
        return entries
            .mapNotNull { entry -> score(entry, tokens)?.let { entry to it } }
            .sortedWith(compareBy({ -it.second }, { it.first.order }))
            .take(limit)
            .map { it.first.emoji }
    }

    private fun score(entry: Entry, tokens: List<String>): Int? {
        var total = 0
        for (token in tokens) {
            total += tokenScore(entry, token) ?: return null
        }
        return total
    }

    private fun tokenScore(entry: Entry, token: String): Int? = when {
        token in entry.nameWords -> EXACT_NAME
        entry.nameWords.any { it.startsWith(token) } -> PREFIX_NAME
        token in entry.keywordWords -> EXACT_KEYWORD
        entry.keywordWords.any { it.startsWith(token) } -> PREFIX_KEYWORD
        else -> null
    }

    private companion object {
        const val LIMIT = 80
        const val EXACT_NAME = 4
        const val PREFIX_NAME = 3
        const val EXACT_KEYWORD = 2
        const val PREFIX_KEYWORD = 1
    }
}
