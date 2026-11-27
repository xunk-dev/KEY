package com.spacekeyboard.keyboard.decorate

enum class DecorCategory {
    ALL, FONTS, FRAMES, MARKS, STRETCH;

    fun matches(style: DecorStyle): Boolean {
        val facts = StyleFacts.of(style)
        return when (this) {
            ALL -> true
            FONTS -> style.language == Language.EN && style.id != PLAIN && !facts.framed && !facts.marked
            FRAMES -> facts.framed
            MARKS -> facts.marked && !facts.framed
            STRETCH -> facts.stretched && !facts.framed
        }
    }

    companion object {
        private const val PLAIN = "en_plain"

        fun forLanguage(language: Language): List<DecorCategory> = when (language) {
            Language.EN -> listOf(ALL, FONTS, FRAMES, MARKS)
            Language.AR -> listOf(ALL, FRAMES, MARKS, STRETCH)
        }
    }
}

private class StyleFacts(val framed: Boolean, val marked: Boolean, val stretched: Boolean) {
    companion object {
        private val Arabic = Regex("^ar_(f\\d+_)?t(\\d)_s\\d_m(\\d)$")
        private val Empty = StyleFacts(framed = false, marked = false, stretched = false)

        fun of(style: DecorStyle): StyleFacts {
            val id = style.id
            if (style.language == Language.EN) return StyleFacts("frame" in id, id.startsWith("en_mark"), false)
            val parts = Arabic.matchEntire(id)?.groupValues ?: return Empty
            return StyleFacts(parts[1].isNotEmpty(), parts[3] != "0", parts[2] != "0")
        }
    }
}
