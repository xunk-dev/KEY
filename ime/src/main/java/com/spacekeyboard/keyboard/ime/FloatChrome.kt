package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import androidx.compose.ui.platform.ComposeView
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController

class FloatChrome(val rail: View, val popover: View) {
    fun all(): List<View> = listOf(rail, popover)

    fun setShown(on: Boolean) {
        rail.visibility = if (on) View.VISIBLE else View.GONE
        if (!on) setPopoverShown(false)
    }

    fun setPopoverShown(on: Boolean) {
        popover.visibility = if (on) View.VISIBLE else View.GONE
    }

    fun measure(dockWidth: Int, railPx: Int, popoverPx: Int) {
        rail.measure(exact(dockWidth), exact(railPx))
        popover.measure(exact(dockWidth), exact(popoverPx))
    }

    fun layout(dock: Rect, frameHeight: Int, railPx: Int) {
        rail.layout(dock.left, dock.bottom, dock.right, dock.bottom + railPx)
        layoutPopover(dock, frameHeight, railPx)
    }

    private fun layoutPopover(dock: Rect, frameHeight: Int, railPx: Int) {
        val height = popover.measuredHeight
        val gap = (GAP_DP * popover.resources.displayMetrics.density).toInt()
        val above = dock.top - gap - height
        val below = dock.bottom + railPx + gap
        val top = when {
            above >= 0 -> above
            below + height <= frameHeight -> below
            else -> dock.top + gap
        }
        popover.layout(dock.left, top, dock.left + popover.measuredWidth, top + height)
    }

    private fun exact(value: Int) = makeMeasureSpec(value, EXACTLY)

    private companion object {
        const val GAP_DP = 8
    }
}

internal fun buildFloatChrome(
    context: Context,
    theme: ThemeController,
    layouts: LayoutController,
    float: FloatController,
    mover: FloatMover,
    resizer: FloatResizer,
): FloatChrome {
    val rail = ComposeView(context).apply { setContent { FloatRailContent(theme, layouts, float, mover, resizer) } }
    val popover = ComposeView(context).apply {
        visibility = View.GONE
        setContent { FloatPopoverContent(theme, layouts) }
    }
    rail.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ -> excludeWholeView(v) }
    return FloatChrome(rail, popover)
}
