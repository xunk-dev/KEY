package com.spacekeyboard.keyboard.core.suggest

class Suggester(
    seed: List<String>,
    private val stats: WordStats,
    seedPairs: Map<String, List<String>> = emptyMap(),
    private val dialects: DialectBook = DialectBook.EMPTY,
) {
    private class Entry(val word: String, val folded: String, val position: Int)

    private val entries: List<Entry> = LinkedHashMap<String, Int>().also { map ->
        seed.forEachIndexed { index, word -> map.putIfAbsent(word.lowercase(), index) }
    }.map { (word, position) -> Entry(word, ArabicFold.fold(word), position) }

    private val known: Set<String> = entries.mapTo(HashSet()) { it.word }.also { it.addAll(dialects.words()) }
    private val foldCache = HashMap<String, String>()
    private val nextSeed: Map<String, List<String>> = seedPairs.mapKeys { ArabicFold.fold(it.key) }

    fun suggest(
        context: TextContext,
        limit: Int = LIMIT,
        includeTyped: Boolean = false,
        dialect: Dialect? = null,
        personal: Personal = Personal.DEFAULT,
    ): List<String> {
        val prefix = context.prefix.lowercase()
        val folded = ArabicFold.fold(prefix)
        val previous = context.previous?.lowercase()
        val follows: Map<String, Int> = if (personal.global) previous?.let { stats.pairs[it] }.orEmpty() else emptyMap()
        val scored = HashMap<String, Long>()
        if (prefix.isEmpty()) {
            follows.forEach { (word, n) -> scored[word] = PAIR_WEIGHT * n }
            previous?.let { nextSeed[ArabicFold.fold(it)] }.orEmpty().forEachIndexed { index, word ->
                scored.merge(word.lowercase(), SEED_PAIR_BASE - index, Long::plus)
            }
            addDialectNext(scored, dialect, previous)
        } else {
            if (personal.global) stats.words.forEach { (word, n) ->
                if (matches(word, foldOf(word), prefix, folded)) scored[word] = USER_BASE + USER_WEIGHT * n + PAIR_WEIGHT * (follows[word] ?: 0)
            }
            personal.local.forEach { (word, n) ->
                if (matches(word, foldOf(word), prefix, folded)) scored.merge(word, LOCAL_BASE + LOCAL_WEIGHT * n, Long::plus)
            }
            entries.forEach {
                if (matches(it.word, it.folded, prefix, folded)) {
                    scored.merge(it.word, SEED_BASE - it.position + foreignShift(dialect, it.folded), Long::plus)
                }
            }
            addDialectWords(scored, dialect, prefix, folded)
        }
        val ranked = scored.entries
            .filter { !stats.isBlocked(it.key) }
            .sortedByDescending { it.value }
            .map { cased(it.key, context.prefix) }
        val unknown = prefix !in known && !(personal.global && stats.words.containsKey(prefix)) && !personal.local.containsKey(prefix)
        if (includeTyped && unknown && context.prefix.length >= MIN_TYPED && limit > 1) {
            return listOf(context.prefix) + ranked.filter { !it.equals(context.prefix, ignoreCase = true) }.take(limit - 1)
        }
        return ranked.take(limit)
    }

    private fun addDialectNext(scored: MutableMap<String, Long>, dialect: Dialect?, previous: String?) {
        if (dialect == null || previous == null) return
        dialects.next(dialect, previous).forEachIndexed { index, word ->
            scored.merge(word.lowercase(), DIALECT_PAIR_BASE - index, Long::plus)
        }
    }

    private fun addDialectWords(scored: MutableMap<String, Long>, dialect: Dialect?, prefix: String, folded: String) {
        if (dialect == null) return
        dialects.entries(dialect).forEach {
            if (matches(it.word, it.folded, prefix, folded)) scored.merge(it.word, DIALECT_BASE - it.position, Long::plus)
        }
    }

    private fun foreignShift(dialect: Dialect?, folded: String): Long {
        val owner = if (dialect == null) null else dialects.markerOf(folded)
        return if (owner != null && owner != dialect) -FOREIGN_PENALTY else 0L
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
        private const val LOCAL_BASE = 14_000L
        private const val LOCAL_WEIGHT = 600L
        private const val SEED_PAIR_BASE = 5_000L
        private const val DIALECT_BASE = 12_000L
        private const val DIALECT_PAIR_BASE = 6_000L
        private const val FOREIGN_PENALTY = 3_000L
    }
}
