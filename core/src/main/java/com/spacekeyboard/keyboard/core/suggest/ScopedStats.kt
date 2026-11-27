package com.spacekeyboard.keyboard.core.suggest

class ScopedStats(val scopes: LinkedHashMap<String, MutableMap<String, Int>> = LinkedHashMap()) {
    fun counts(scope: String): Map<String, Int> = scopes[scope] ?: emptyMap()

    fun learn(scope: String, word: String) {
        val key = WordStats.clean(word) ?: return
        if (scope.isEmpty() || scope.length > MAX_SCOPE_NAME) return
        val words = scopes.remove(scope) ?: HashMap()
        scopes[scope] = words
        words[key] = (words[key] ?: 0) + 1
        if (words.size > MAX_WORDS) trim(words)
        while (scopes.size > MAX_SCOPES) scopes.remove(scopes.keys.first())
    }

    fun remove(word: String) {
        val key = word.lowercase()
        scopes.values.forEach { it.remove(key) }
        scopes.values.removeAll { it.isEmpty() }
    }

    fun forget() = scopes.clear()

    fun encode(): String {
        val lines = ArrayList<String>()
        scopes.forEach { (scope, words) -> words.forEach { (word, n) -> lines += "$scope$SEP$word$SEP$n" } }
        return lines.joinToString("\n")
    }

    private fun trim(words: MutableMap<String, Int>) {
        val keep = words.entries.sortedByDescending { it.value }.take(MAX_WORDS * 4 / 5).map { it.key }.toSet()
        words.keys.retainAll(keep)
    }

    companion object {
        const val MAX_SCOPES = 40
        const val MAX_WORDS = 300
        private const val MAX_SCOPE_NAME = 120
        private const val SEP = '\t'

        fun decode(raw: String): ScopedStats {
            val stats = ScopedStats()
            raw.lineSequence().forEach { line ->
                val parts = line.split(SEP)
                val count = parts.getOrNull(2)?.toIntOrNull()
                if (parts.size == 3 && count != null && count > 0) restore(stats, parts[0], parts[1], count)
            }
            return stats
        }

        private fun restore(stats: ScopedStats, scope: String, word: String, count: Int) {
            if (scope.isEmpty() || scope.length > MAX_SCOPE_NAME || WordStats.clean(word) != word) return
            val words = stats.scopes.getOrPut(scope) { HashMap() }
            if (words.size < MAX_WORDS) words[word] = count
            while (stats.scopes.size > MAX_SCOPES) stats.scopes.remove(stats.scopes.keys.first())
        }
    }
}
