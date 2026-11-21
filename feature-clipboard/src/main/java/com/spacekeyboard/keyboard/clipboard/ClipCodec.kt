package com.spacekeyboard.keyboard.clipboard

object ClipCodec {
    private const val ITEM = "I"
    private const val SECTION = "S"
    private const val NEXT = "N"

    fun encode(book: ClipBook): String {
        val lines = ArrayList<String>()
        lines += listOf(NEXT, book.nextId.toString()).joinToString("\t")
        book.sections.forEach { lines += listOf(SECTION, escape(it.name), it.locked.toString()).joinToString("\t") }
        book.items.forEach {
            lines += listOf(ITEM, it.id, it.pinned, escape(it.section.orEmpty()), it.copiedAt, escape(it.text)).joinToString("\t")
        }
        return lines.joinToString("\n")
    }

    fun decode(raw: String): ClipBook {
        var book = ClipBook()
        raw.split('\n').forEach { line ->
            val parts = line.split('\t')
            book = runCatching { read(book, parts) }.getOrDefault(book)
        }
        return book
    }

    private fun read(book: ClipBook, parts: List<String>): ClipBook = when (parts[0]) {
        NEXT -> book.copy(nextId = parts[1].toLong())
        SECTION -> book.copy(sections = book.sections + Section(unescape(parts[1]), parts[2].toBooleanStrict()))
        ITEM -> {
            val section = unescape(parts[3]).ifEmpty { null }
            val item = ClipItem(parts[1].toLong(), unescape(parts[5]), parts[2].toBooleanStrict(), section, parts[4].toLong())
            book.copy(items = book.items + item)
        }
        else -> book
    }

    private fun escape(text: String): String = buildString {
        text.forEach {
            when (it) {
                '\\' -> append("\\\\")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(it)
            }
        }
    }

    private fun unescape(text: String): String = buildString {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\' && i + 1 < text.length) {
                i++
                append(when (text[i]) { 't' -> '\t'; 'n' -> '\n'; 'r' -> '\r'; else -> text[i] })
            } else {
                append(c)
            }
            i++
        }
    }
}
