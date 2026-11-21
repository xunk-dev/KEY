package com.spacekeyboard.keyboard.ime

import android.graphics.Rect
import android.os.Bundle
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.PlacedKey

internal class KeyAccessibility(
    private val host: KeyboardView,
    private val keys: () -> List<PlacedKey>,
    private val describe: (KeySpec) -> String,
    private val press: (PlacedKey) -> Unit,
) : ExploreByTouchHelper(host) {
    override fun getVirtualViewAt(x: Float, y: Float): Int {
        val index = keys().indexOfFirst { it.cell.contains(x, y) }
        return if (index < 0) INVALID_ID else index
    }

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        keys().indices.forEach { virtualViewIds.add(it) }
    }

    override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
        val placed = keys().getOrNull(virtualViewId)
        if (placed == null) {
            node.contentDescription = ""
            node.setBoundsInParent(Rect(0, 0, 1, 1))
            return
        }
        node.contentDescription = describe(placed.key)
        node.className = "android.widget.Button"
        node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
        val c = placed.cell
        node.setBoundsInParent(Rect(c.left.toInt(), c.top.toInt(), c.right.toInt().coerceAtLeast(c.left.toInt() + 1), c.bottom.toInt().coerceAtLeast(c.top.toInt() + 1)))
    }

    override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
        if (action != AccessibilityNodeInfoCompat.ACTION_CLICK) return false
        val placed = keys().getOrNull(virtualViewId) ?: return false
        press(placed)
        invalidateVirtualView(virtualViewId)
        return true
    }
}
