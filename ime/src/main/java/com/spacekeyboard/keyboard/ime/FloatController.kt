package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import kotlin.math.roundToInt

class FloatController {
    var popoverOpen by mutableStateOf(false)
        private set

    var railOpen by mutableStateOf(false)
        private set

    var railStamp by mutableIntStateOf(0)
        private set

    var holding by mutableStateOf(false)
        private set

    var onChange: () -> Unit = {}

    fun togglePopover() {
        touchRail()
        set(!popoverOpen)
    }

    fun closePopover() = set(false)

    fun touchRail() {
        railStamp++
        if (railOpen) return
        railOpen = true
        onChange()
    }

    fun holdRail(on: Boolean) {
        holding = on
        railStamp++
    }

    fun closeRail() {
        if (!railOpen && !popoverOpen) return
        railOpen = false
        popoverOpen = false
        onChange()
    }

    private fun set(value: Boolean) {
        if (popoverOpen == value) return
        popoverOpen = value
        onChange()
    }
}

internal class FloatMover(private val frame: FloatFrame, private val layouts: LayoutController) {
    private var baseLeft = 0
    private var baseTop = 0

    fun start() {
        baseLeft = frame.placedLeft()
        baseTop = frame.placedTop()
        frame.holdPosition(true)
    }

    fun drag(dx: Float, dy: Float) = frame.dragFrom(baseLeft, baseTop, dx, dy)

    fun end() {
        frame.holdPosition(false)
        layouts.setFloatPosition(frame.leftPos, frame.topPos)
    }

    fun center() = frame.requestCenter()
}

private data class FloatBox(val width: Int, val height: Int, val left: Int, val top: Int)

internal class FloatResizer(private val layouts: LayoutController, private val frame: FloatFrame) {
    private val metrics = frame.resources.displayMetrics
    private var width0 = Limits.FLOAT_WIDTH_DEFAULT
    private var height0 = Limits.FLOAT_HEIGHT_DEFAULT
    private var left0 = 0
    private var top0 = 0
    private var rows = 1
    private var last: FloatBox? = null

    fun start() {
        val snapshot = layouts.snapshot
        width0 = snapshot.prefs.floatWidthPercent
        height0 = snapshot.prefs.floatHeightPercent
        rows = snapshot.stableRows
        left0 = frame.placedLeft()
        top0 = frame.placedTop()
        last = null
    }

    fun drag(dx: Float, dy: Float, fromLeft: Boolean) {
        if (frame.width <= 0) return
        val shift = dx * PERCENT / frame.width
        val width = if (fromLeft) fitLeft(width0 - shift) else fitRight(width0 + shift)
        val left = if (fromLeft) left0 + (width0 - width) * PER_PERCENT else left0
        val next = FloatBox(width, fitHeight(dy), left, top0)
        if (next == last) return
        last = next
        layouts.setFloatFrame(next.width, next.height, next.left, next.top)
    }

    private fun fitLeft(raw: Float): Int {
        val room = (left0 + width0 * PER_PERCENT) / PER_PERCENT.toFloat()
        return raw.coerceIn(Limits.FLOAT_WIDTH_MIN.toFloat(), maxOf(Limits.FLOAT_WIDTH_MIN.toFloat(), minOf(Limits.FLOAT_WIDTH_MAX.toFloat(), room))).roundToInt()
    }

    private fun fitRight(raw: Float): Int {
        val room = (Limits.FLOAT_POS_MAX - left0) / PER_PERCENT.toFloat()
        return raw.coerceIn(Limits.FLOAT_WIDTH_MIN.toFloat(), maxOf(Limits.FLOAT_WIDTH_MIN.toFloat(), minOf(Limits.FLOAT_WIDTH_MAX.toFloat(), room))).roundToInt()
    }

    private fun fitHeight(dy: Float): Int {
        val perRow = Limits.ROW_DP * rows * metrics.density
        val screen = metrics.heightPixels
        val fit = layouts.snapshot.prefs.maxHeightPercent(rows, metrics.density, screen).coerceAtMost(Limits.FLOAT_HEIGHT_MAX)
        return (height0 + dy / perRow * PERCENT).roundToInt().coerceIn(Limits.FLOAT_HEIGHT_MIN, maxOf(Limits.FLOAT_HEIGHT_MIN, fit))
    }

    private companion object {
        const val PERCENT = 100
        const val PER_PERCENT = Limits.FLOAT_POS_MAX / PERCENT
    }
}
