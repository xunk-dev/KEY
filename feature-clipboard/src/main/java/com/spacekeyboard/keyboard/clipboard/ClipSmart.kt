package com.spacekeyboard.keyboard.clipboard

data class SmartView(
    val items: List<ClipItem>,
    val kind: ClipKind? = null,
    val counts: Map<ClipKind, Int> = emptyMap(),
    val grouped: Boolean = false,
)

object ClipSmart {
    fun off(items: List<ClipItem>): SmartView = SmartView(items)

    fun arrange(items: List<ClipItem>, selected: ClipKind?): SmartView {
        val counts = LinkedHashMap<ClipKind, Int>()
        ClipKind.entries.forEach { kind ->
            val size = items.count { kind in it.kinds }
            if (size > 0) counts[kind] = size
        }
        val kind = selected?.takeIf { it in counts }
        if (kind != null) return SmartView(items.filter { kind in it.kinds }, kind, counts, false)
        val grouped = counts.size > 1
        val ordered = if (grouped) items.sortedBy { it.primaryKind.ordinal } else items
        return SmartView(ordered, null, counts, grouped)
    }
}
