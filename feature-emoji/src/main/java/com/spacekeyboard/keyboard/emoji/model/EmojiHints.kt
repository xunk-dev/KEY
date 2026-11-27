package com.spacekeyboard.keyboard.emoji.model

class EmojiHints(
    emojis: List<Emoji>,
    names: List<Map<String, EmojiName>>,
    curated: Map<String, List<String>> = emptyMap(),
) {
    private val catalog: Map<String, Emoji> = LinkedHashMap<String, Emoji>().also { map ->
        emojis.forEach { map.putIfAbsent(plain(it.text), it) }
    }
    private val exact: Map<String, Emoji>
    private val curatedHits: Map<String, Emoji>

    init {
        val whole = HashMap<String, MutableSet<Emoji>>()
        val keyword = HashMap<String, MutableSet<Emoji>>()
        emojis.forEach { emoji -> names.mapNotNull { it[emoji.text] }.forEach { collect(emoji, it, whole, keyword) } }
        exact = unique(whole) + unique(keyword).filterKeys { it !in whole }
        curatedHits = curated.mapNotNull { (word, list) -> resolve(list)?.let { SearchText.normalize(word).trim() to it } }.toMap()
    }

    fun find(word: String): Emoji? {
        val key = SearchText.normalize(word).trim()
        if (key.isEmpty() || key.length > MAX_WORD) return null
        lookup(key)?.let { return it }
        return if (key.startsWith(ARTICLE) && key.length >= ARTICLE.length + 2) lookup(key.removePrefix(ARTICLE)) else null
    }

    private fun lookup(key: String): Emoji? = curatedHits[key] ?: if (lengthOk(key)) exact[key] else null

    private fun lengthOk(key: String): Boolean = key.length >= MIN_LATIN || key.any { it.code > LATIN_END }

    private fun resolve(list: List<String>): Emoji? = list.firstNotNullOfOrNull { catalog[plain(it)] }

    private fun collect(
        emoji: Emoji,
        name: EmojiName,
        whole: MutableMap<String, MutableSet<Emoji>>,
        keyword: MutableMap<String, MutableSet<Emoji>>,
    ) {
        val words = SearchText.words(name.name)
        if (words.size == 1) whole.getOrPut(words[0]) { HashSet() }.add(emoji)
        name.keywords.forEach { kw ->
            val parts = SearchText.words(kw)
            if (parts.size == 1) keyword.getOrPut(parts[0]) { HashSet() }.add(emoji)
        }
    }

    private fun unique(source: Map<String, Set<Emoji>>): Map<String, Emoji> =
        source.filterValues { it.size == 1 }.mapValues { it.value.first() }

    private companion object {
        const val ARTICLE = "ال"
        const val MAX_WORD = 24
        const val MIN_LATIN = 3
        const val LATIN_END = 0x24F

        fun plain(text: String): String = text.replace("\uFE0F", "")
    }
}
