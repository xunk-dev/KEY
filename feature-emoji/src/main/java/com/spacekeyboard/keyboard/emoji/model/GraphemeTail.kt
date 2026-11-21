package com.spacekeyboard.keyboard.emoji.model

object GraphemeTail {
    const val WINDOW = 48
    private const val ZWJ = 0x200D
    private const val KEYCAP = 0x20E3
    private const val REGIONAL_FIRST = 0x1F1E6
    private const val REGIONAL_LAST = 0x1F1FF

    fun lastLength(text: CharSequence): Int {
        if (text.isEmpty()) return 0
        val points = codePoints(text)
        var index = points.size - 1
        var guard = 0
        while (index >= 0 && guard++ < points.size) {
            while (index >= 0 && isExtender(points[index])) index--
            if (index < 0) break
            index -= if (isRegional(points[index])) regionalSpan(points, index) else 1
            if (index >= 0 && points[index] == ZWJ) index-- else break
        }
        val removed = points.size - 1 - index
        return points.takeLast(removed).sumOf { Character.charCount(it) }
    }

    private fun regionalSpan(points: IntArray, index: Int): Int {
        var run = 0
        var cursor = index
        while (cursor >= 0 && isRegional(points[cursor])) {
            run++
            cursor--
        }
        return if (run % 2 == 0) 2 else 1
    }

    private fun codePoints(text: CharSequence): IntArray {
        val start = (text.length - WINDOW).coerceAtLeast(0)
        val from = if (start > 0 && Character.isLowSurrogate(text[start])) start + 1 else start
        val list = ArrayList<Int>()
        var i = from
        while (i < text.length) {
            val cp = Character.codePointAt(text, i)
            list += cp
            i += Character.charCount(cp)
        }
        return list.toIntArray()
    }

    private fun isRegional(cp: Int) = cp in REGIONAL_FIRST..REGIONAL_LAST

    private fun isExtender(cp: Int): Boolean =
        cp == KEYCAP || cp in 0xFE00..0xFE0F || cp in 0x1F3FB..0x1F3FF || cp in 0xE0020..0xE007F
}
