package com.spacekeyboard.keyboard.core.layout

object PageRows {
    const val SHORT = 4
    const val TALL = 5

    private const val DELETE_PERCENT = 14f

    fun count(numberRow: Boolean): Int = if (numberRow) TALL else SHORT

    fun numbers(base: LayoutSpec, tall: Boolean): LayoutSpec = base

    fun symbols(base: LayoutSpec, tall: Boolean): LayoutSpec {
        if (tall || base.rows.size <= SHORT) return base
        val first = base.rows.first()
        if (!first.all { key -> key.label.length == 1 && key.label[0].isDigit() }) return base
        return LayoutSpec(base.id, base.rows.drop(1))
    }

    fun edit(base: LayoutSpec, tall: Boolean): LayoutSpec {
        if (!tall || base.rows.size >= TALL) return base
        val byId = base.rows.flatten().associateBy { it.id }
        val select = byId["edit_select"] ?: return base
        val undo = byId["edit_undo"] ?: return base
        val redo = byId["edit_redo"] ?: return base
        val up = byId["edit_up"] ?: return base
        val down = byId["edit_down"] ?: return base
        val delete = byId["delete"] ?: return base
        if (base.rows.size != SHORT) return base
        val sum = total(base)
        val third = sum / 3f
        val deleteWeight = sum * DELETE_PERCENT / 100f
        val arrow = (sum - deleteWeight) / 2f
        val rows = listOf(
            base.rows[0],
            listOf(select, undo, redo).map { it.copy(weight = third) },
            base.rows[1],
            listOf(up.copy(weight = arrow), down.copy(weight = arrow), delete.copy(weight = deleteWeight)),
            base.rows[3],
        )
        return LayoutSpec(base.id, rows)
    }

    private fun total(spec: LayoutSpec): Float = spec.rows.first().sumOf { it.weight.toDouble() }.toFloat()
}
