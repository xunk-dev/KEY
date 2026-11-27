package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout

internal class KeyboardStack(context: Context, private val keyboard: View, val results: View) : FrameLayout(context) {
    init {
        addView(keyboard, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        addView(results, LayoutParams(MATCH_PARENT, 0, Gravity.TOP))
        results.visibility = View.GONE
    }

    fun coverTopRow(covered: Boolean, rowPx: Int) {
        (keyboard as? KeyboardView)?.topRowClipPx = if (covered) rowPx else 0
    }
}
