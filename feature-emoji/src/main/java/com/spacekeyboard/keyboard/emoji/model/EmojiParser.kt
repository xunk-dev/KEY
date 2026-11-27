package com.spacekeyboard.keyboard.emoji.model

object EmojiParser {
    private const val SEPARATOR = '\t'
    private const val TONE_SEPARATOR = '|'

    fun decode(hex: String): String? = try {
        val builder = StringBuilder()
        hex.trim().split(' ').filter { it.isNotEmpty() }.forEach { builder.appendCodePoint(it.toInt(16)) }
        builder.toString().takeIf { it.isNotEmpty() }
    } catch (_: IllegalArgumentException) {
        null
    }

    fun parse(lines: Sequence<String>): List<EmojiGroup> {
        val byGroup = LinkedHashMap<String, MutableList<Emoji>>()
        lines.forEach { line ->
            val parts = line.split(SEPARATOR)
            val group = parts.getOrNull(0)?.trim().orEmpty()
            val text = parts.getOrNull(1)?.let(::decode)
            if (group in EmojiGroups.ids && text != null) {
                byGroup.getOrPut(group) { ArrayList() } += Emoji(text, tones(parts.getOrNull(2)))
            }
        }
        return EmojiGroups.ids.mapNotNull { id -> byGroup[id]?.let { EmojiGroup(id, it) } }
    }

    private fun tones(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        val slots = raw.split(TONE_SEPARATOR).map { decode(it).orEmpty() }
        return if (slots.size == Emoji.TONE_COUNT) slots else emptyList()
    }
}
