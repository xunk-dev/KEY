package com.spacekeyboard.keyboard.ime.layout

import com.spacekeyboard.keyboard.core.layout.PlacedKey

class DragState(val from: PlacedKey, val startX: Float, val startY: Float) {
    var x = startX
    var y = startY
    var active = false
    var target: PlacedKey? = null
}
