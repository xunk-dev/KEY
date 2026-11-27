package com.spacekeyboard.keyboard.emoji.ui

object HubTabOrder {
    fun <T> arrange(order: List<T>, available: List<T>): List<T> =
        order.filter { it in available } + available.filter { it !in order }

    fun <T> merge(full: List<T>, shown: List<T>): List<T> {
        val queue = shown.iterator()
        val kept = full.map { if (it in shown && queue.hasNext()) queue.next() else it }
        return (kept + shown).distinct()
    }

    fun <T> moveTo(order: List<T>, item: T, slot: Int): List<T> {
        val from = order.indexOf(item)
        val to = slot.coerceIn(0, order.lastIndex)
        if (from < 0 || from == to) return order
        return order.toMutableList().apply {
            removeAt(from)
            add(to, item)
        }
    }

    fun slotAt(x: Float, width: Float, count: Int, rtl: Boolean): Int {
        if (count <= 0 || width <= 0f) return 0
        val physical = (x / (width / count)).toInt().coerceIn(0, count - 1)
        return if (rtl) count - 1 - physical else physical
    }

    fun slotLeft(slot: Int, cell: Float, count: Int, rtl: Boolean): Float =
        (if (rtl) count - 1 - slot else slot) * cell
}
