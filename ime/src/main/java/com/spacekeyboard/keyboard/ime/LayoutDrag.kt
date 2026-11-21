package com.spacekeyboard.keyboard.ime

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.layout.SlotRef
import com.spacekeyboard.keyboard.ime.layout.DragState
import kotlin.math.hypot

internal class LayoutDrag(
    private val view: View,
    private val placement: () -> Placement,
    private val slop: Int,
    private val onSwap: (SlotRef, SlotRef) -> Unit,
    private val onDragging: (Boolean) -> Unit,
) {
    var state: DragState? = null
        private set

    fun touch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> start(event.x, event.y)
            MotionEvent.ACTION_MOVE -> move(event.x, event.y)
            MotionEvent.ACTION_UP -> end(true)
            MotionEvent.ACTION_CANCEL -> end(false)
        }
        return true
    }

    private fun start(x: Float, y: Float) {
        val key = placement().movableAt(x, y) ?: return
        state = DragState(key, x, y)
        view.parent?.requestDisallowInterceptTouchEvent(true)
        onDragging(true)
    }

    private fun move(x: Float, y: Float) {
        val current = state ?: return
        current.x = x
        current.y = y
        if (!current.active && hypot(x - current.startX, y - current.startY) > slop) current.active = true
        if (current.active) current.target = placement().movableAt(x, y)?.takeIf { it.slot != current.from.slot }
        view.invalidate()
    }

    fun end(commit: Boolean) {
        val current = state
        state = null
        if (current != null) onDragging(false)
        val target = current?.target
        if (commit && current != null && current.active && target != null) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onSwap(current.from.slot, target.slot)
        }
        view.invalidate()
    }
}
