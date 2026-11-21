package com.spacekeyboard.keyboard.core.layout

class TouchCorrector(private val prior: (prefix: String, letter: String) -> Int) {
    fun choose(placement: Placement, hit: PlacedKey, x: Float, y: Float, prefix: () -> String): PlacedKey {
        if (!typable(hit.key)) return hit
        val reach = hit.cell.width * EDGE_RATIO
        val near = placement.keys.filter { it !== hit && typable(it.key) && it.cell.distanceTo(x, y) <= reach }
        if (near.isEmpty()) return hit
        val typed = prefix()
        if (typed.isEmpty()) return hit
        val own = prior(typed, hit.key.label)
        val best = near.maxByOrNull { prior(typed, it.key.label) } ?: return hit
        val score = prior(typed, best.key.label)
        return if (score > own && score >= own * RATIO + 1) best else hit
    }

    private fun typable(key: KeySpec): Boolean =
        key.action == KeyAction.COMMIT && key.label.length == 1 && Character.isLetter(key.label[0]) &&
            !key.id.startsWith(LayoutSpec.NUMBER_ROW_PREFIX)

    companion object {
        const val EDGE_RATIO = 0.22f
        const val RATIO = 4
    }
}
