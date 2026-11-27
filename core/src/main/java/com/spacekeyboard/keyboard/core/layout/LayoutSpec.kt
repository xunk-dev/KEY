package com.spacekeyboard.keyboard.core.layout

data class LayoutSpec(val id: String, val rows: List<List<KeySpec>>) {
    fun order(): List<List<String>> = rows.map { row -> row.map { it.id } }

    fun key(slot: SlotRef): KeySpec? = rows.getOrNull(slot.row)?.getOrNull(slot.col)

    fun swapped(a: SlotRef, b: SlotRef): List<List<String>>? {
        val first = key(a)
        val second = key(b)
        if (a == b || first == null || second == null) return null
        if (!first.movable || !second.movable) return null
        val next = order().map { it.toMutableList() }
        next[a.row][a.col] = second.id
        next[b.row][b.col] = first.id
        return next
    }

    fun arranged(wanted: List<List<String>>): LayoutSpec? {
        if (wanted.size != rows.size) return null
        if (wanted.indices.any { wanted[it].size != rows[it].size }) return null
        val flat = wanted.flatten()
        val byId = rows.flatten().associateBy { it.id }
        if (flat.toSet().size != flat.size || !byId.keys.containsAll(flat)) return null
        val next = rows.mapIndexed { r, row ->
            row.mapIndexed { c, slot ->
                val key = byId.getValue(wanted[r][c])
                if (!slot.movable && key.id != slot.id) return null
                if (slot.movable && !key.movable) return null
                if (slot.movable) key.copy(weight = slot.weight) else slot
            }
        }
        return LayoutSpec(id, next)
    }

    fun withFunctions(chosen: Map<String, String>, rtl: Boolean): LayoutSpec {
        if (chosen.isEmpty()) return this
        return LayoutSpec(id, rows.map { row -> row.map { key -> replaced(key, chosen[key.id], rtl) } })
    }

    private fun replaced(key: KeySpec, fnId: String?, rtl: Boolean): KeySpec {
        val fn = KeyFn.byId(fnId)
        return if (fn == null || !KeyFn.isSlot(key)) key else fn.replacing(key, rtl)
    }

    fun withNumberRow(digits: List<String>, alternates: List<String> = emptyList(), extra: KeySpec? = null): LayoutSpec {
        val each = rows.first().sumOf { it.weight.toDouble() }.toFloat() / (digits.size + if (extra != null) 1 else 0)
        val top = digits.mapIndexed { i, digit ->
            KeySpec(NUMBER_ROW_PREFIX + digit, digit, KeyAction.COMMIT, each, false, listOfNotNull(alternates.getOrNull(i)))
        } + listOfNotNull(extra?.copy(weight = each))
        return LayoutSpec(id, listOf(top) + rows)
    }

    fun withoutNumberRow(): LayoutSpec =
        if (rows.first().firstOrNull()?.id?.startsWith(NUMBER_ROW_PREFIX) == true) LayoutSpec(id, rows.drop(1)) else this

    companion object {
        const val NUMBER_ROW_PREFIX = "nr_"

        fun fallback(id: String): LayoutSpec = LayoutSpec(
            id,
            listOf(
                listOf(
                    KeySpec("space", "", KeyAction.SPACE, 5f, false),
                    KeySpec("delete", "⌫", KeyAction.DELETE, 1.5f, true),
                ),
            ),
        )
    }
}
