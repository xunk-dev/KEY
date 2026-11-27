package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement

internal fun isLetterKey(key: KeySpec, label: String): Boolean =
    key.action == KeyAction.COMMIT && label.codePointCount(0, label.length) <= 1

internal fun iconUnitOf(placement: Placement, icons: KeyIcons): Float {
    val rows = placement.keys.filter { it.slot.row >= 0 }
    val shortest = rows.minOfOrNull { it.box.height }
    return shortest ?: placement.keys.firstOrNull()?.box?.height ?: 0f
}

internal fun spaceLabelOwners(placement: Placement): Set<PlacedKey> {
    val owners = HashSet<PlacedKey>()
    placement.keys.filter { it.key.action == KeyAction.SPACE }.groupBy { it.slot }.values.forEach { parts ->
        owners += parts.reduce { best, next -> if (next.box.width > best.box.width) next else best }
    }
    return owners
}

internal fun railOnLeft(placement: Placement): Boolean {
    val rail = placement.keys.firstOrNull { it.key.action == KeyAction.EXPAND } ?: return false
    val other = placement.keys.firstOrNull { it.slot.row >= 0 } ?: return false
    return rail.cell.centerX < other.cell.centerX
}
