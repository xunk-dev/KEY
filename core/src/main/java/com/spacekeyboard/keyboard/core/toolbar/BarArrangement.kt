package com.spacekeyboard.keyboard.core.toolbar

import com.spacekeyboard.keyboard.core.shortcuts.ShortcutTile

object BarSlots {
    const val TOOLS = "tools"
    const val EMOJI = "emoji"
    const val TRANSLATE = "translate"
    const val CLIPBOARD = "clipboard"
    const val GLOBE = "globe"
    const val MACRO_PLAY = "macro_play"
    private const val LANGUAGE_PREFIX = "lang:"

    fun language(id: String): String = LANGUAGE_PREFIX + id

    fun isLanguage(slot: String): Boolean = slot.startsWith(LANGUAGE_PREFIX)

    fun hiddenWhileTyping(slot: String): Boolean = slot == GLOBE || isLanguage(slot)

    fun movable(slot: String): Boolean = ShortcutTile.byId(slot) != null

    const val MAX_MOVABLE = 6
}

class BarArrangement private constructor(val left: List<String>, val right: List<String>, val off: List<String> = emptyList()) {
    val all: List<String> get() = left + right

    val movableCount: Int get() = all.count(BarSlots::movable)

    fun canBring(slot: String): Boolean = BarSlots.movable(slot) && slot !in all && movableCount < BarSlots.MAX_MOVABLE

    fun bring(slot: String): BarArrangement =
        if (!canBring(slot)) this else BarArrangement(left + slot, right, off - slot)

    fun sendOff(slot: String): BarArrangement =
        if (slot !in all || !BarSlots.movable(slot)) this else BarArrangement(left - slot, right - slot, off + slot)

    fun place(slot: String, toRight: Boolean, index: Int): BarArrangement {
        if (slot !in all) return this
        val rest = BarArrangement(left - slot, right - slot, off)
        return if (toRight) {
            BarArrangement(rest.left, rest.right.inserted(index, slot), off)
        } else {
            BarArrangement(rest.left.inserted(index, slot), rest.right, off)
        }
    }

    fun step(slot: String, delta: Int): BarArrangement {
        val inLeft = slot in left
        val group = if (inLeft) left else right
        val at = group.indexOf(slot)
        if (at < 0 || delta == 0) return this
        val to = at + if (delta > 0) 1 else -1
        return when {
            to in group.indices -> place(slot, !inLeft, to)
            inLeft && to >= group.size -> place(slot, true, 0)
            !inLeft && to < 0 -> place(slot, false, left.size)
            else -> this
        }
    }

    fun dropAt(slot: String, centerX: Float, widths: Map<String, Float>, total: Float): BarArrangement {
        if (slot !in all) return this
        val leftEnd = left.sumOf { (widths[it] ?: 0f).toDouble() }.toFloat()
        val rightStart = total - right.sumOf { (widths[it] ?: 0f).toDouble() }.toFloat()
        val toRight = centerX >= (leftEnd + rightStart) / 2f
        var edge = if (toRight) rightStart else 0f
        var index = 0
        for (other in if (toRight) right else left) {
            val width = widths[other] ?: 0f
            if (other != slot && centerX > edge + width / 2f) index++
            edge += width
        }
        return place(slot, toRight, index)
    }

    fun encode(): String {
        val base = left.joinToString(",") + SEPARATOR + right.joinToString(",")
        return if (off.isEmpty()) base else base + SEPARATOR + off.joinToString(",")
    }

    override fun equals(other: Any?): Boolean = other is BarArrangement && other.left == left && other.right == right && other.off == off

    override fun hashCode(): Int = 31 * (31 * left.hashCode() + right.hashCode()) + off.hashCode()

    override fun toString(): String = encode()

    companion object {
        private const val SEPARATOR = "|"

        fun default(languages: List<String>): BarArrangement = BarArrangement(
            listOf(BarSlots.TOOLS, BarSlots.EMOJI, BarSlots.TRANSLATE, BarSlots.CLIPBOARD, BarSlots.MACRO_PLAY),
            listOf(BarSlots.GLOBE) + languages.distinct().reversed().map(BarSlots::language),
        )

        fun resolve(raw: String?, languages: List<String>): BarArrangement {
            val base = default(languages)
            val parts = raw.orEmpty().split(SEPARATOR)
            if (parts.size !in 2..3) return base
            val known = base.all.toSet() + ShortcutTile.entries.map { it.id }
            val left = parts[0].split(',').filter { it in known }.distinct()
            val right = parts[1].split(',').filter { it in known && it !in left }.distinct()
            val off = parts.getOrNull(2).orEmpty().split(',').filter { BarSlots.movable(it) && it !in left && it !in right }.distinct()
            return base.missingFilled(BarArrangement(left, right, off))
        }

        private fun BarArrangement.missingFilled(saved: BarArrangement): BarArrangement {
            var result = saved
            val missing = all.filter { it !in saved.all && it !in saved.off }
            val ordered = missing.filterNot(BarSlots::isLanguage) + missing.filter(BarSlots::isLanguage).reversed()
            ordered.forEach { slot -> result = result.withNew(slot, slot in right) }
            return result
        }

        private fun BarArrangement.withNew(slot: String, defaultRight: Boolean): BarArrangement {
            val anchor = if (BarSlots.isLanguage(slot)) all.firstOrNull(BarSlots::isLanguage) else null
            val toRight = if (anchor != null) anchor in right else defaultRight
            val group = if (toRight) right else left
            val grown = if (toRight) BarArrangement(left, right + slot, off) else BarArrangement(left + slot, right, off)
            return grown.place(slot, toRight, if (anchor != null) group.indexOf(anchor) else group.size)
        }
    }
}

private fun List<String>.inserted(index: Int, slot: String): List<String> =
    toMutableList().also { it.add(index.coerceIn(0, it.size), slot) }
