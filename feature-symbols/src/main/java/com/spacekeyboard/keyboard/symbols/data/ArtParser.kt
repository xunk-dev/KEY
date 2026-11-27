package com.spacekeyboard.keyboard.symbols.data

import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.model.ArtEntry

object ArtParser {
    private const val CATEGORY = "%cat "
    private const val SEPARATOR = "%--"
    private const val COMMENT = "%#"

    fun parse(lines: List<String>): List<ArtEntry> {
        val found = ArrayList<ArtEntry>()
        var category: ArtCategory? = null
        val pending = ArrayList<String>()
        fun flush() {
            val text = trimBlank(pending)
            val owner = category
            if (owner != null && text.isNotEmpty()) found += ArtEntry(owner, text)
            pending.clear()
        }
        for (raw in lines) {
            val line = raw.trimEnd('\r')
            when {
                line.startsWith(COMMENT) -> Unit
                line.startsWith(CATEGORY) -> {
                    flush()
                    category = ArtCategory.byId(line.removePrefix(CATEGORY).trim())
                }
                line == SEPARATOR -> flush()
                else -> pending += line
            }
        }
        flush()
        return found
    }

    private fun trimBlank(lines: List<String>): String =
        lines.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }.joinToString("\n")
}
