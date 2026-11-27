package com.spacekeyboard.keyboard.ime

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.theme.ColorMath

internal class HintPainter(private val density: Float) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT }
    private val bounds = Rect()
    private val newlineInk = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val labelInk = RectF()

    fun newline(canvas: Canvas, box: Box, ink: Int, unit: Float) {
        newlineInk.color = ink
        newlineInk.alpha = NEWLINE_ALPHA
        newlineInk.textSize = unit * NEWLINE_SIZE
        canvas.drawText("\u21B5", box.centerX + unit * NEWLINE_X, box.bottom - unit * NEWLINE_Y, newlineInk)
    }

    fun draw(
        canvas: Canvas,
        area: RectF,
        labelPaint: Paint,
        baseSize: Float,
        hint: String,
        label: String,
        labelSize: Float,
        ink: Int,
        alpha: Float,
        box: Box,
    ) {
        val cell = minOf(box.width, box.height)
        val pad = (cell * PAD_RATIO).coerceIn(PAD_MIN_DP * density, PAD_MAX_DP * density)
        val slot = cell * SLOT_RATIO
        paint.typeface = labelPaint.typeface
        measureLabel(area, labelPaint, label, labelSize, baseSize)
        val centreX = area.right - pad - slot / 2f
        val centreY = area.top + pad + slot / 2f
        val size = fit(hint, baseSize, slot, pad, centreX, centreY) ?: return
        paint.textSize = size
        paint.getTextBounds(hint, 0, hint.length, bounds)
        if (size < MIN_LABEL_DP * density) return
        paint.color = ColorMath.withAlpha(ink, ALPHA * alpha)
        val originX = centreX - (bounds.left + bounds.right) / 2f
        val originY = centreY - (bounds.top + bounds.bottom) / 2f
        canvas.drawText(hint, originX, originY, paint)
    }

    private fun measureLabel(area: RectF, labelPaint: Paint, label: String, labelSize: Float, baseSize: Float) {
        labelPaint.textSize = labelSize
        labelPaint.getTextBounds(label, 0, label.length, bounds)
        val shift = -(labelPaint.ascent() + labelPaint.descent()) / 2f
        val half = labelPaint.measureText(label) / 2f
        labelInk.set(
            area.centerX() - half + bounds.left,
            area.centerY() + shift + bounds.top,
            area.centerX() - half + bounds.right,
            area.centerY() + shift + bounds.bottom,
        )
        labelPaint.textSize = baseSize
    }

    private fun fit(hint: String, baseSize: Float, slot: Float, pad: Float, centreX: Float, centreY: Float): Float? {
        var size = minOf(baseSize * RATIO, slot)
        val floor = baseSize * RATIO * MIN_SCALE
        for (pass in 0 until FIT_PASSES) {
            paint.textSize = size
            paint.getTextBounds(hint, 0, hint.length, bounds)
            val wide = bounds.width().toFloat()
            val tall = bounds.height().toFloat()
            if (wide <= 0f || tall <= 0f) return null
            if (wide > slot || tall > slot) {
                size *= minOf(slot / wide, slot / tall) * FIT_MARGIN
                continue
            }
            val clashes = centreX - wide / 2f < labelInk.right + pad / 2f && centreY + tall / 2f > labelInk.top - pad / 2f
            if (!clashes || size <= floor) break
            size = maxOf(size * SHRINK_STEP, floor)
        }
        return size
    }

    private companion object {
        const val NEWLINE_ALPHA = 150
        const val NEWLINE_SIZE = 0.26f
        const val NEWLINE_X = 0.34f
        const val NEWLINE_Y = 0.12f
        const val RATIO = 0.52f
        const val ALPHA = 0.78f
        const val PAD_RATIO = 0.07f
        const val PAD_MIN_DP = 3f
        const val PAD_MAX_DP = 6f
        const val SLOT_RATIO = 0.3f
        const val MIN_SCALE = 0.7f
        const val SHRINK_STEP = 0.92f
        const val FIT_MARGIN = 0.98f
        const val FIT_PASSES = 12
        const val MIN_LABEL_DP = 7f
    }
}
