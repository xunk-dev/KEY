package com.spacekeyboard.keyboard.core.text

data class SpellFix(val original: String, val fixed: String)

class ArabicSpelling(private val known: (String) -> Boolean) {
    fun fix(before: String): SpellFix? {
        val word = before.takeLastWhile(::isBaseLetter)
        if (word.length < MIN_WORD) return null
        val lead = before.dropLast(word.length).lastOrNull()
        if (lead != null && (Character.isLetterOrDigit(lead) || lead == TATWEEL || Tashkeel.isMark(lead))) return null
        if (known(word)) return null
        val fixed = repair(word) ?: return null
        return SpellFix(word, fixed)
    }

    private fun repair(word: String): String? {
        variantOf(word)?.let { return it }
        for (prefix in PREFIXES) {
            val stem = word.removePrefix(prefix)
            if (stem.length == word.length || stem.length < minStem(prefix)) continue
            if (known(stem)) return null
            variantOf(stem)?.let { return prefix + it }
        }
        return null
    }

    private fun minStem(prefix: String): Int = if (prefix.length == 1) MIN_STEM_SINGLE else MIN_STEM

    private fun variantOf(word: String): String? {
        var level = setOf(word)
        repeat(MAX_CHANGES) {
            val next = LinkedHashSet<String>()
            level.forEach { neighbours(it, next) }
            next.remove(word)
            val hits = next.filter(known)
            if (hits.size == 1) return hits.single()
            if (hits.isNotEmpty() || next.size > MAX_VARIANTS) return null
            level = next
        }
        return null
    }

    private fun neighbours(word: String, out: MutableSet<String>) {
        for (index in word.indices) {
            for (other in alternatives(word[index], index, word.lastIndex)) {
                out.add(word.substring(0, index) + other + word.substring(index + 1))
            }
        }
    }

    private fun alternatives(ch: Char, index: Int, last: Int): String = when {
        index == last -> FINAL[ch].orEmpty()
        index == 0 -> if (ch in INITIAL) INITIAL.replace(ch.toString(), "") else ""
        else -> if (ch in MEDIAL) MEDIAL.replace(ch.toString(), "") else ""
    }

    private fun isBaseLetter(ch: Char): Boolean = ch in FIRST_LETTER..LAST_LETTER && ch != TATWEEL

    private companion object {
        const val MIN_WORD = 3
        const val MIN_STEM = 3
        const val MIN_STEM_SINGLE = 4
        const val MAX_CHANGES = 2
        const val MAX_VARIANTS = 2000
        const val FIRST_LETTER = 'ء'
        const val LAST_LETTER = 'ي'
        const val TATWEEL = 'ـ'
        const val INITIAL = "اأإآ"
        const val MEDIAL = "اأإآؤئء"
        val FINAL: Map<Char, String> = mapOf(
            'ه' to "ة",
            'ة' to "ه",
            'ى' to "ي",
            'ي' to "ى",
            'ء' to "أئؤ",
            'أ' to "ءئؤ",
            'ئ' to "ءأؤ",
            'ؤ' to "ءأئ",
        )
        val PREFIXES: List<String> = listOf(
            "وبال", "وكال", "ولل", "وال", "فال",
            "بال", "كال", "لل", "ال",
            "و", "ف", "ب", "ل", "ك",
        )
    }
}
