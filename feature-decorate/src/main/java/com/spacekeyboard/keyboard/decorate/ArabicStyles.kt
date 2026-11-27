package com.spacekeyboard.keyboard.decorate

class ArabicTransform(
    private val tatweel: Int,
    private val substitution: Int,
    private val marks: Int,
    private val frame: Frame?,
) : Transform {
    private val replaced: Map<Char, Char> = ArabicJoin.lookalikes.filterKeys { it in SUBSTITUTION_LEVELS.take(substitution).joinToString("") }

    override fun text(source: String): String {
        val builder = StringBuilder()
        var letters = 0
        source.forEachIndexed { index, c ->
            builder.append(replaced[c] ?: c)
            if (ArabicJoin.isLetter(c)) {
                builder.append(mark(letters++))
                val next = source.getOrNull(index + 1)
                if (next != null && ArabicJoin.isLetter(next) && ArabicJoin.isDual(replaced[c] ?: c)) {
                    repeat(tatweel) { builder.append(ArabicJoin.TATWEEL) }
                }
            }
        }
        return if (frame == null) builder.toString() else frame.prefix + builder + frame.suffix
    }

    override fun typed(before: String, input: String): String {
        val c = input.singleOrNull() ?: return input
        if (!ArabicJoin.isLetter(c)) return input
        val previous = ArabicJoin.lastBase(before)
        val join = previous != null && previous != ArabicJoin.TATWEEL && ArabicJoin.isDual(previous)
        val lead = if (join) ArabicJoin.TATWEEL.toString().repeat(tatweel) else ""
        return lead + (replaced[c] ?: c) + mark(before.count { ArabicJoin.isLetter(it) })
    }

    private fun mark(index: Int): String = when (marks) {
        0 -> ""
        CYCLE -> CYCLE_MARKS[index % CYCLE_MARKS.size].toString()
        else -> SINGLE_MARKS[marks - 1].toString()
    }

    private companion object {
        const val CYCLE = 4
        val SUBSTITUTION_LEVELS = listOf(" كي", "اة", "ه")
        val SINGLE_MARKS = listOf('َ', 'ُ', 'ِ')
        val CYCLE_MARKS = listOf('َ', 'ُ', 'ِ')
    }
}

object ArabicStyles {
    private val frames = EnglishStyles.frames
    private val framedBases = listOf(Triple(0, 0, 0), Triple(1, 0, 0), Triple(0, 1, 0), Triple(0, 0, 4), Triple(2, 1, 0), Triple(1, 3, 1))

    val all: List<DecorStyle> = buildList {
        for (t in 0..3) for (s in 0..3) for (m in 0..4) {
            if (t + s + m > 0) add(style("ar_t${t}_s${s}_m$m", t, s, m, null))
        }
        frames.forEachIndexed { f, frame ->
            framedBases.forEach { (t, s, m) -> add(style("ar_f${f}_t${t}_s${s}_m$m", t, s, m, frame)) }
        }
    }

    private fun style(id: String, t: Int, s: Int, m: Int, frame: Frame?) =
        DecorStyle(id, Language.AR, ArabicTransform(t, s, m, frame))
}
