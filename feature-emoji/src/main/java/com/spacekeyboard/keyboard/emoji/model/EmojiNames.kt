package com.spacekeyboard.keyboard.emoji.model

data class EmojiName(val name: String, val keywords: List<String>)

object NameParser {
    fun parse(lines: Sequence<String>): Map<String, EmojiName> {
        val result = HashMap<String, EmojiName>()
        lines.forEach { line ->
            val parts = line.split('\t')
            val text = parts.getOrNull(0)?.let(EmojiParser::decode)
            val name = parts.getOrNull(1)?.trim().orEmpty()
            if (text != null && name.isNotEmpty()) {
                val keywords = parts.getOrNull(2).orEmpty().split('|').map { it.trim() }.filter { it.isNotEmpty() }
                result[text] = EmojiName(name, keywords)
            }
        }
        return result
    }
}
