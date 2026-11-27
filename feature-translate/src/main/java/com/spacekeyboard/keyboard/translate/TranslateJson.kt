package com.spacekeyboard.keyboard.translate

object TranslateJson {
    fun encode(request: TranslateRequest): String =
        "{\"text\":${quote(request.text)},\"source\":${quote(request.source)}," +
            "\"target\":${quote(request.target)},\"style\":${quote(request.style.id)}}"

    fun quote(value: String): String = buildString(value.length + 2) {
        append('"')
        value.forEach { append(escape(it)) }
        append('"')
    }

    private fun escape(ch: Char): String = when {
        ch == '"' -> "\\\""
        ch == '\\' -> "\\\\"
        ch == '\n' -> "\\n"
        ch == '\r' -> "\\r"
        ch == '\t' -> "\\t"
        ch < ' ' -> "\\u%04x".format(ch.code)
        else -> ch.toString()
    }

    fun parseFlat(body: String): Map<String, Any?>? = runCatching { Reader(body).readObject() }.getOrNull()

    private class Reader(private val text: String) {
        private var pos = 0

        fun readObject(): Map<String, Any?> {
            skip()
            expect('{')
            val out = LinkedHashMap<String, Any?>()
            skip()
            if (peek() == '}') return out.also { pos++ }
            while (true) {
                skip()
                val key = readString()
                skip()
                expect(':')
                skip()
                out[key] = readValue()
                skip()
                if (peek() == ',') pos++ else break
            }
            expect('}')
            return out
        }

        private fun readValue(): Any? = when (val ch = peek()) {
            '"' -> readString()
            't' -> literal("true", true)
            'f' -> literal("false", false)
            'n' -> literal("null", null)
            else -> if (ch == '-' || ch.isDigit()) readNumber() else error("bad value")
        }

        private fun literal(word: String, value: Any?): Any? {
            check(text.startsWith(word, pos))
            pos += word.length
            return value
        }

        private fun readNumber(): Number {
            val start = pos
            while (pos < text.length && (text[pos].isDigit() || text[pos] in "+-.eE")) pos++
            val raw = text.substring(start, pos)
            return raw.toLongOrNull() ?: raw.toDouble()
        }

        private fun readString(): String {
            expect('"')
            val out = StringBuilder()
            while (true) {
                check(pos < text.length)
                val ch = text[pos++]
                when (ch) {
                    '"' -> return out.toString()
                    '\\' -> out.append(readEscape())
                    else -> out.append(ch)
                }
            }
        }

        private fun readEscape(): Char = when (val ch = text[pos++]) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'b' -> '\b'
            'f' -> '\u000C'
            'u' -> text.substring(pos, pos + 4).toInt(16).toChar().also { pos += 4 }
            '"', '\\', '/' -> ch
            else -> error("bad escape")
        }

        private fun peek(): Char = if (pos < text.length) text[pos] else '\u0000'

        private fun skip() {
            while (pos < text.length && text[pos].isWhitespace()) pos++
        }

        private fun expect(ch: Char) {
            check(peek() == ch)
            pos++
        }
    }
}
