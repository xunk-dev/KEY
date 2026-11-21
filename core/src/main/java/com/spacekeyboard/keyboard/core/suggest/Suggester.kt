package com.spacekeyboard.keyboard.core.suggest

class Suggester(
    seed: List<String>,
    private val stats: WordStats,
    seedPairs: Map<String, List<String>> = emptyMap(),
) {
    private class Entry(val word: String, val folded: String, val position: Int)

    private val entries: List<Entry> = LinkedHashMap<String, Int>().also { map ->
        seed.forEachIndexed { index, word -> map.putIfAbsent(word.lowercase(), index) }
    }.map { (word, position) -> Entry(word, ArabicFold.fold(word), position) }

    private val known: Set<String> = entries.mapTo(HashSet()) { it.word }
    private val foldCache = HashMap<String, String>()
    private val nextSeed: Map<String, List<String>> = seedPairs.mapKeys { ArabicFold.fold(it.key) }

    fun suggest(context: TextContext, limit: Int = LIMIT, includeTyped: Boolean = false): List<String> {
        val prefix = context.prefix.lowercase()
        val folded = ArabicFold.fold(prefix)
        val previous = context.previous?.lowercase()
        val follows = previous?.let { stats.pairs[it] }.orEmpty()
        val scored = HashMap<String, Long>()
        if (prefix.isEmpty()) {
            follows.forEach { (word, n) -> scored[word] = PAIR_WEIGHT * n }
            previous?.let { nextSeed[ArabicFold.fold(it)] }.orEmpty().forEachIndexed { index, word ->
                scored.merge(word.lowercase(), SEED_PAIR_BASE - index, Long::plus)
            }
        } else {
            stats.words.forEach { (word, n) ->
                if (matches(word, foldOf(word), prefix, folded)) scored[word] = USER_BASE + USER_WEIGHT * n + PAIR_WEIGHT * (follows[word] ?: 0)
            }
            entries.forEach { if (matches(it.word, it.folded, prefix, folded)) scored.merge(it.word, SEED_BASE - it.position, Long::plus) }
        }
        val ranked = scored.entries
            .filter { !stats.isBlocked(it.key) }
            .sortedByDescending { it.value }
            .map { cased(it.key, context.prefix) }
        val unknown = prefix !in known && !stats.words.containsKey(prefix)
        if (includeTyped && unknown && context.prefix.length >= MIN_TYPED && limit > 1) {
            return listOf(context.prefix) + ranked.filter { !it.equals(context.prefix, ignoreCase = true) }.take(limit - 1)
        }
        return ranked.take(limit)
    }

    private fun foldOf(word: String): String = foldCache.getOrPut(word) { ArabicFold.fold(word) }

    private fun matches(word: String, foldedWord: String, prefix: String, foldedPrefix: String): Boolean {
        if (!foldedWord.startsWith(foldedPrefix)) return false
        return foldedWord.length > foldedPrefix.length || word != prefix
    }

    private fun cased(word: String, typed: String): String =
        if (typed.length > 1 && typed.all { it.isUpperCase() }) word.uppercase()
        else if (typed.isNotEmpty() && typed[0].isUpperCase()) word.replaceFirstChar { it.uppercase() }
        else word

    companion object {
        const val LIMIT = 3
        private const val MIN_TYPED = 2
        private const val USER_BASE = 20_000L
        private const val USER_WEIGHT = 1_000L
        private const val PAIR_WEIGHT = 100_000L
        private const val SEED_BASE = 10_000L
        private const val SEED_PAIR_BASE = 5_000L
    }
}
