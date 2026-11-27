package com.spacekeyboard.keyboard.ime

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets

internal fun applyNavigationInset(view: View) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    view.setOnApplyWindowInsetsListener { target, insets ->
        val sides = insets.getInsets(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        target.setPadding(sides.left, 0, sides.right, sides.bottom)
        insets
    }
}

internal fun excludeEdgeGestures(view: View, bandDp: Int = 0) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val edge = (EDGE_DP * view.resources.displayMetrics.density).toInt()
    val width = view.width
    val height = view.height
    if (width <= edge * 2 || height <= 0) return
    val band = (bandDp * view.resources.displayMetrics.density).toInt()
    val top = if (band in 1 until height) (height - band) / 2 else 0
    val bottom = if (band in 1 until height) top + band else height
    view.setSystemGestureExclusionRects(listOf(Rect(0, top, edge, bottom), Rect(width - edge, top, width, bottom)))
}

internal fun excludeWholeView(view: View) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    if (view.width <= 0 || view.height <= 0) return
    view.setSystemGestureExclusionRects(listOf(Rect(0, 0, view.width, view.height)))
}

private const val EDGE_DP = 36
