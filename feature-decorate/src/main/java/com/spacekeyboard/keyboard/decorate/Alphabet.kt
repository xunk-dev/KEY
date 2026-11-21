package com.spacekeyboard.keyboard.decorate

class Alphabet(
    val id: String,
    private val upper: Int? = null,
    private val lower: Int? = null,
    private val digit: Int? = null,
    private val exceptions: Map<Char, Int> = emptyMap(),
    private val custom: Map<Char, String> = emptyMap(),
    private val foldCase: Boolean = false,
    val reversed: Boolean = false,
) {
    fun map(c: Char): String {
        val key = if (foldCase) c.lowercaseChar() else c
        custom[key]?.let { return it }
        exceptions[c]?.let { return codePointString(it) }
        return when {
            c in 'A'..'Z' && upper != null && !foldCase -> codePointString(upper + (c - 'A'))
            c in 'a'..'z' && lower != null -> codePointString(lower + (c - 'a'))
            c in 'A'..'Z' && foldCase && lower != null -> codePointString(lower + (c - 'A'))
            c in '0'..'9' && digit != null -> codePointString(digit + (c - '0'))
            else -> c.toString()
        }
    }

    fun text(source: String): String {
        val mapped = source.map(::map)
        return (if (reversed) mapped.asReversed() else mapped).joinToString("")
    }
}
