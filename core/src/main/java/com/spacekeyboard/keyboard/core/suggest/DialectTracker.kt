package com.spacekeyboard.keyboard.core.suggest

class DialectTracker(private val book: DialectBook) {
    private val scores = DoubleArray(Dialect.values().size)

    fun observe(word: String) {
        for (index in scores.indices) scores[index] *= DECAY
        book.markerOf(ArabicFold.fold(word))?.let { scores[it.ordinal] += 1.0 }
    }

    fun reset() = scores.fill(0.0)

    fun detect(text: String = ""): Dialect? {
        if (book.isEmpty) return null
        val total = scores.copyOf()
        tokens(text).forEach { token -> book.markerOf(ArabicFold.fold(token))?.let { total[it.ordinal] += 1.0 } }
        val ranked = Dialect.values().sortedByDescending { total[it.ordinal] }
        val best = total[ranked[0].ordinal]
        val second = total[ranked[1].ordinal]
        return if (best >= MIN_SCORE && best - second >= MIN_GAP) ranked[0] else null
    }

    companion object {
        private const val DECAY = 0.95
        private const val MIN_SCORE = 0.5
        private const val MIN_GAP = 0.3
        private val NON_WORD = Regex("[^\\p{L}\\p{M}']+")

        fun tokens(text: String): List<String> = text.split(NON_WORD).filter { it.isNotEmpty() }
    }
}
