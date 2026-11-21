package com.spacekeyboard.keyboard.core.suggest

class KeyPrior(private val seed: List<String>, private val stats: WordStats) {
    fun weight(prefix: String, letter: String): Int {
        val head = (prefix + letter).lowercase()
        var total = 0
        for (word in seed) if (word.length >= head.length && word.startsWith(head, ignoreCase = true)) total++
        for ((word, count) in stats.words) if (word.length >= head.length && word.startsWith(head)) total += count.coerceAtMost(MAX_USER) * USER_WEIGHT
        return total
    }

    private companion object {
        const val MAX_USER = 5
        const val USER_WEIGHT = 2
    }
}
