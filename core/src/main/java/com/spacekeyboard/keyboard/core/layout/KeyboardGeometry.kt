package com.spacekeyboard.keyboard.core.layout

object KeyboardGeometry {
    private const val EDGE = 0.0001f

    fun place(layout: LayoutSpec, input: GeometryInput): Placement {
        val full = Box(0f, 0f, input.width, input.height)
        val fraction = input.oneHandPercent / 100f
        val area = when (input.mode) {
            PanelMode.ONE_HAND_LEFT -> full.copy(right = input.width * fraction)
            PanelMode.ONE_HAND_RIGHT -> full.copy(left = input.width * (1f - fraction))
            else -> full
        }
        val placed = ArrayList<PlacedKey>()
        val rowHeight = input.height / layout.rows.size
        layout.rows.forEachIndexed { r, row ->
            val band = Box(area.left, r * rowHeight, area.right, (r + 1) * rowHeight)
            val cells = if (input.mode == PanelMode.SPLIT) splitRow(row, band, input.splitGapPercent) else straightRow(row, band)
            cells.forEach { cell -> placed += PlacedKey(cell.key, SlotRef(r, cell.col), cell.box, cell.box.inset(input.gap / 2f)) }
        }
        if (area != full) placed += rail(input, full, area)
        return Placement(placed.filter { it.key.visible })
    }

    private class Cell(val key: KeySpec, val col: Int, val box: Box)

    private fun straightRow(row: List<KeySpec>, band: Box): List<Cell> {
        val total = row.sumOf { it.weight.toDouble() }.toFloat()
        var left = band.left
        return row.mapIndexed { col, key ->
            val width = band.width * key.weight / total
            Cell(key, col, band.copy(left = left, right = left + width)).also { left += width }
        }
    }

    private class Part(val key: KeySpec, val col: Int, val from: Float, val to: Float)

    private fun splitRow(row: List<KeySpec>, band: Box, gapPercent: Int): List<Cell> {
        val total = row.sumOf { it.weight.toDouble() }.toFloat()
        val mid = total / 2f
        val left = ArrayList<Part>()
        val right = ArrayList<Part>()
        var start = 0f
        row.forEachIndexed { col, key ->
            val from = start
            val to = start + key.weight
            start = to
            when {
                to <= mid + EDGE -> left += Part(key, col, from, to)
                from >= mid - EDGE -> right += Part(key, col, from, to)
                key.splittable -> {
                    left += Part(key, col, from, mid)
                    right += Part(key, col, mid, to)
                }
                (from + to) / 2f < mid -> left += Part(key, col, from, to)
                else -> right += Part(key, col, from, to)
            }
        }
        val gap = band.width * gapPercent / 100f
        val sideWidth = (band.width - gap) / 2f
        return placeSide(left, band, band.left, sideWidth) + placeSide(right, band, band.left + sideWidth + gap, sideWidth)
    }

    private fun placeSide(parts: List<Part>, band: Box, origin: Float, width: Float): List<Cell> {
        val total = parts.sumOf { (it.to - it.from).toDouble() }.toFloat()
        if (total <= 0f) return emptyList()
        var left = origin
        return parts.map { part ->
            val w = width * (part.to - part.from) / total
            Cell(part.key, part.col, band.copy(left = left, right = left + w)).also { left += w }
        }
    }

    private fun rail(input: GeometryInput, full: Box, area: Box): List<PlacedKey> {
        val onLeft = area.left > full.left
        val strip = if (onLeft) full.copy(right = area.left) else full.copy(left = area.right)
        val expand = KeySpec("rail_expand", if (onLeft) "‹" else "›", KeyAction.EXPAND, 1f, false)
        val flip = KeySpec("rail_flip", "↔", KeyAction.FLIP, 1f, false)
        val half = strip.height / 2f
        return listOf(expand, flip).mapIndexed { index, key ->
            val cell = strip.copy(top = index * half, bottom = (index + 1) * half)
            PlacedKey(key, SlotRef(-1, index), cell, cell.inset(input.gap / 2f))
        }
    }
}
