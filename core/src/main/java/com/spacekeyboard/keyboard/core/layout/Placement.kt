package com.spacekeyboard.keyboard.core.layout

data class PlacedKey(val key: KeySpec, val slot: SlotRef, val cell: Box, val box: Box)

data class Placement(val keys: List<PlacedKey>) {
    fun hit(x: Float, y: Float): PlacedKey? = keys.firstOrNull { it.cell.contains(x, y) }

    fun movableAt(x: Float, y: Float): PlacedKey? = hit(x, y)?.takeIf { it.key.movable }
}

data class GeometryInput(
    val width: Float,
    val height: Float,
    val gap: Float,
    val mode: PanelMode,
    val oneHandPercent: Int,
    val splitGapPercent: Int,
    val bottomInset: Float = 0f,
    val edgeInset: Float = 0f,
    val widthPercent: Int = Limits.WIDTH_MAX,
)
