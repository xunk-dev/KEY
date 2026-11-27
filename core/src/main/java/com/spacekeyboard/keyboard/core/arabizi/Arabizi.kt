package com.spacekeyboard.keyboard.core.arabizi

object Arabizi {
    private val digraphs = mapOf(
        "sh" to "ش", "kh" to "خ", "gh" to "غ", "th" to "ث", "dh" to "ذ", "ch" to "تش",
        "aa" to "ا", "ee" to "ي", "ii" to "ي", "oo" to "و", "ou" to "و", "uu" to "و",
    )
    private val letters = mapOf(
        'b' to "ب", 't' to "ت", 'j' to "ج", 'h' to "ه", 'd' to "د", 'r' to "ر", 'z' to "ز", 's' to "س",
        'f' to "ف", 'q' to "ق", 'k' to "ك", 'l' to "ل", 'm' to "م", 'n' to "ن", 'w' to "و", 'y' to "ي",
        'g' to "ج", 'p' to "ب", 'v' to "ف", 'c' to "ك", 'x' to "كس",
    )
    private val digits = mapOf('2' to "ء", '3' to "ع", '5' to "خ", '6' to "ط", '7' to "ح", '8' to "ق", '9' to "ص")
    private val finals = mapOf('a' to "ا", 'e' to "ي", 'i' to "ي", 'o' to "و", 'u' to "و")
    private val longs = mapOf('a' to "ا", 'i' to "ي", 'u' to "و")
    private const val VOWELS = "aeiou"

    fun accepts(ch: Char): Boolean = ch.lowercaseChar() in 'a'..'z' || ch in '0'..'9'

    fun convert(source: String): String {
        if (source.all { it in '0'..'9' }) return source
        val word = source.lowercase()
        val out = StringBuilder()
        var i = 0
        while (i < word.length) {
            val pair = if (i + 1 < word.length) word.substring(i, i + 2) else ""
            val joined = digraphs[pair]
            if (joined != null) {
                out.append(joined)
                i += 2
            } else {
                out.append(single(word, i))
                i += 1
            }
        }
        return out.toString()
    }

    private fun single(word: String, at: Int): String {
        val ch = word[at]
        return when {
            ch in digits -> digits.getValue(ch)
            ch in letters -> letters.getValue(ch)
            ch in VOWELS -> vowel(word, at)
            else -> ch.toString()
        }
    }

    private fun vowel(word: String, at: Int): String {
        val ch = word[at]
        if (at == 0) return "ا"
        if (at == word.length - 1) return finals.getValue(ch)
        val tail = word.substring(at + 1)
        val longVowel = ch in longs && tail.length == 1 && tail[0] !in VOWELS && tail[0] != 'h' && tail[0] !in digits
        return if (longVowel) longs.getValue(ch) else ""
    }
}
