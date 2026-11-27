package com.spacekeyboard.keyboard.core.suggest

class WordStats(
    val words: MutableMap<String, Int> = HashMap(),
    val pairs: MutableMap<String, MutableMap<String, Int>> = HashMap(),
    val blocked: MutableSet<String> = LinkedHashSet(),
) {
    var revision = 0
        private set

    fun learn(previous: String?, word: String) {
        val key = word.lowercase()
        if (key.length < MIN_WORD || key.length > MAX_WORD || key in blocked) return
        words[key] = (words[key] ?: 0) + 1
        if (previous != null) {
            val follows = pairs.getOrPut(previous.lowercase()) { HashMap() }
            follows[key] = (follows[key] ?: 0) + 1
        }
        revision++
        if (words.size > MAX_WORDS) trim()
    }

    fun forget() {
        words.clear()
        pairs.clear()
        blocked.clear()
        revision++
    }

    fun isBlocked(word: String): Boolean = blocked.isNotEmpty() && word.lowercase() in blocked

    fun block(word: String) {
        val key = word.lowercase()
        if (key.isEmpty() || key.length > MAX_WORD) return
        words.remove(key)
        pairs.remove(key)
        pairs.values.forEach { it.remove(key) }
        pairs.values.removeAll { it.isEmpty() }
        blocked.remove(key)
        blocked.add(key)
        while (blocked.size > MAX_BLOCKED) blocked.remove(blocked.first())
        revision++
    }

    fun add(word: String): Boolean {
        val key = clean(word) ?: return false
        blocked.remove(key)
        words[key] = maxOf(words[key] ?: 0, ADDED_COUNT)
        revision++
        if (words.size > MAX_WORDS) trim()
        return true
    }

    fun addAll(lines: Sequence<String>): Int {
        val added = LinkedHashSet<String>()
        for (line in lines) {
            if (added.size >= MAX_IMPORT || words.size >= MAX_WORDS) break
            val key = clean(line.removePrefix(BOM))?.takeIf { importable(it) } ?: continue
            if (key !in words && add(key)) added.add(key)
        }
        return added.size
    }

    fun listed(): List<String> =
        words.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key }).map { it.key }

    fun restoreBlocked() {
        blocked.clear()
        revision++
    }

    fun encode(): String {
        val lines = ArrayList<String>()
        words.forEach { (w, n) -> lines += "w$SEP$w$SEP$n" }
        pairs.forEach { (p, follows) -> follows.forEach { (w, n) -> lines += "p$SEP$p$SEP$w$SEP$n" } }
        blocked.forEach { lines += "b$SEP$it" }
        return lines.joinToString("\n")
    }

    private fun trim() {
        val keep = words.entries.sortedByDescending { it.value }.take(MAX_WORDS * 4 / 5).map { it.key }.toSet()
        words.keys.retainAll(keep)
        pairs.keys.retainAll(keep)
        pairs.values.forEach { it.keys.retainAll(keep) }
        pairs.values.removeAll { it.isEmpty() }
    }

    companion object {
        const val ADDED_COUNT = 10
        const val MIN_WORD = 2
        const val MAX_WORD = 24
        const val MAX_WORDS = 4000
        const val MAX_BLOCKED = 500
        const val MAX_IMPORT = 2000
        private const val BOM = "\uFEFF"
        private const val SEP = '\t'

        private fun importable(key: String): Boolean = key.none { it == '\uFFFD' || it.isISOControl() }

        fun clean(word: String): String? {
            val key = word.trim().lowercase()
            if (key.length < MIN_WORD || key.length > MAX_WORD || key.any { it.isWhitespace() }) return null
            return key
        }

        fun decode(raw: String): WordStats {
            val stats = WordStats()
            raw.lineSequence().forEach { line ->
                val parts = line.split(SEP)
                if (parts[0] == "b" && parts.size == 2 && parts[1].isNotEmpty()) stats.blocked.add(parts[1])
                val count = parts.lastOrNull()?.toIntOrNull() ?: return@forEach
                if (parts[0] == "w" && parts.size == 3) stats.words[parts[1]] = count
                if (parts[0] == "p" && parts.size == 4) stats.pairs.getOrPut(parts[1]) { HashMap() }[parts[2]] = count
            }
            return stats
        }
    }
}
