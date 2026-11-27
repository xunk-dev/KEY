package com.spacekeyboard.keyboard.clipboard

object ClipTabOrder {
    val DEFAULT: List<ClipTab> = ClipTab.entries.toList()

    fun normalize(raw: List<ClipTab>): List<ClipTab> = (raw + DEFAULT).distinct()

    fun parse(text: String?): List<ClipTab> {
        val known = text.orEmpty().split(',').mapNotNull { name -> ClipTab.entries.firstOrNull { it.name == name.trim() } }
        return normalize(known)
    }

    fun encode(order: List<ClipTab>): String = normalize(order).joinToString(",") { it.name }

    fun moveTo(order: List<ClipTab>, tab: ClipTab, slot: Int): List<ClipTab> {
        val base = normalize(order)
        val from = base.indexOf(tab)
        val to = slot.coerceIn(0, base.lastIndex)
        if (from == to) return base
        return base.toMutableList().apply {
            removeAt(from)
            add(to, tab)
        }
    }

    fun shift(order: List<ClipTab>, tab: ClipTab, delta: Int): List<ClipTab> {
        val base = normalize(order)
        return moveTo(base, tab, base.indexOf(tab) + delta)
    }

    fun slotAt(x: Float, width: Float, count: Int, rtl: Boolean): Int {
        if (count <= 0 || width <= 0f) return 0
        val physical = (x / (width / count)).toInt().coerceIn(0, count - 1)
        return if (rtl) count - 1 - physical else physical
    }

    fun slotLeft(slot: Int, cell: Float, count: Int, rtl: Boolean): Float =
        (if (rtl) count - 1 - slot else slot) * cell
}
