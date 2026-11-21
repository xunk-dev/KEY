package com.spacekeyboard.keyboard.decorate

object Renderability {
    private fun isMark(cp: Int): Boolean =
        Character.getType(cp).let {
            it == Character.NON_SPACING_MARK.toInt() || it == Character.ENCLOSING_MARK.toInt() ||
                it == Character.COMBINING_SPACING_MARK.toInt()
        }

    fun ok(text: String, has: (Int) -> Boolean): Boolean =
        text.codePoints().toArray().all { cp -> Character.isWhitespace(cp) || isMark(cp) || has(cp) }

    fun probe(language: Language): String =
        if (language == Language.EN) "AaBbYy09 ★" else "بتمكياة"
}
