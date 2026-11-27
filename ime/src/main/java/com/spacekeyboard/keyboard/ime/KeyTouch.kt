package com.spacekeyboard.keyboard.ime

import android.os.SystemClock
import com.spacekeyboard.keyboard.core.layout.PlacedKey

internal class KeyTouch(
    val id: Int,
    val placed: PlacedKey,
    val downX: Float,
    val downY: Float,
    onLongPress: (KeyTouch) -> Unit,
    onRepeat: (KeyTouch) -> Unit,
) {
    val dx = offset(downX, placed.cell.centerX, placed.cell.width)
    val dy = offset(downY, placed.cell.centerY, placed.cell.height)
    val downAt = SystemClock.uptimeMillis()
    var lastX = downX
    var lastY = downY
    var armed = false
    var anchorX = downX
    var handled = false
    var deferred = false
    var glideable = false
    var glide = false
    var swiping = false
    var cursor = false
    var movedAt = 0L
    var typed = ""
    var alts: List<String> = emptyList()
    var altIndex = -1
    var altStart = -1
    var repeats = 0
    val longPress = Runnable { onLongPress(this) }
    val repeat = Runnable { onRepeat(this) }

    private fun offset(at: Float, center: Float, size: Float): Float =
        if (size <= 0f) 0f else ((at - center) / size).coerceIn(-0.5f, 0.5f)
}
