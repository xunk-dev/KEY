package com.spacekeyboard.keyboard.emoji.model

object HintParser {
    fun parse(lines: Sequence<String>): Map<String, List<String>> {
        val result = LinkedHashMap<String, List<String>>()
        lines.forEach { line ->
            val parts = line.split('\t')
            val word = parts.getOrNull(0)?.let { SearchText.normalize(it).trim() }.orEmpty()
            val emojis = parts.getOrNull(1).orEmpty().split(' ').filter { it.isNotEmpty() }
            if (word.isNotEmpty() && emojis.isNotEmpty() && word !in result) result[word] = emojis
        }
        return result
    }
}
