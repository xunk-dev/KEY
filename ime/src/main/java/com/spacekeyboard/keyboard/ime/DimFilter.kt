package com.spacekeyboard.keyboard.ime

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View

internal class DimFilter {
    private var level = FULL
    private var views: List<View> = emptyList()

    fun apply(brightness: Float, targets: List<View>) {
        if (brightness == level && targets == views) return
        level = brightness
        views = targets
        val paint = if (brightness >= FULL) null else dimPaint(brightness)
        val type = if (paint == null) View.LAYER_TYPE_NONE else View.LAYER_TYPE_HARDWARE
        targets.forEach { it.setLayerType(type, paint) }
    }

    private fun dimPaint(brightness: Float) = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setScale(brightness, brightness, brightness, FULL) })
    }

    private companion object {
        const val FULL = 1f
    }
}
