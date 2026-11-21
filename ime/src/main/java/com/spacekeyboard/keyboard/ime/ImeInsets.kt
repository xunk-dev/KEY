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

internal fun excludeEdgeGestures(view: View) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val edge = (EDGE_DP * view.resources.displayMetrics.density).toInt()
    val width = view.width
    val height = view.height
    if (width <= edge * 2 || height <= 0) return
    view.setSystemGestureExclusionRects(listOf(Rect(0, 0, edge, height), Rect(width - edge, 0, width, height)))
}

private const val EDGE_DP = 36
