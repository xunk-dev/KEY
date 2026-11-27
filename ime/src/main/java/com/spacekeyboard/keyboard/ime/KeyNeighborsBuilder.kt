package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.autofix.KeyMap
import com.spacekeyboard.keyboard.autofix.KeyNeighbors
import com.spacekeyboard.keyboard.autofix.KeyPoint
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.Placement

internal object KeyNeighborsBuilder {
    fun from(placement: Placement, shown: (KeySpec) -> String): KeyNeighbors = KeyMap(points(placement, shown))

    fun points(placement: Placement, shown: (KeySpec) -> String): List<KeyPoint> {
        val points = ArrayList<KeyPoint>()
        for (placed in placement.keys) {
            val key = placed.key
            if (key.action != KeyAction.COMMIT) continue
            val cell = placed.cell
            val letters = if (key.pair && key.letters.size == 2) key.letters else listOf(null)
            letters.forEachIndexed { i, letter ->
                val spec = if (letter == null) key else key.copy(label = letter, pair = false)
                val text = KeyLabels.output(shown(spec))
                if (text.length != 1 || !Character.isLetter(text[0])) return@forEachIndexed
                val width = if (letter == null) cell.width else cell.width / 2f
                val x = if (letter == null) cell.centerX else cell.left + width * (i + 0.5f)
                points.add(KeyPoint(text[0], x, cell.centerY, width, cell.height))
            }
        }
        return points
    }
}
