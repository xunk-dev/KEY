package com.spacekeyboard.keyboard.ime

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon

internal class SpaceLabelPainter(
    private val density: Float,
    private val textPaint: Paint,
    private val effects: KeyEffects,
    private val icons: KeyIcons,
) {
    private val micAreas = ArrayList<RectF>()

    fun reset() = micAreas.clear()

    fun micAt(x: Float, y: Float): Boolean = micAreas.any { it.contains(x, y) }

    fun draw(canvas: Canvas, rect: RectF, label: String, ink: Int, accent: Int, alpha: Float, keyWidth: Float, baseSize: Float) {
        effects.ruler(canvas, rect, ink, alpha)
        val size = baseSize * SPACE_LABEL_RATIO
        val micSize = size * MIC_RATIO
        val inset = MIC_INSET_DP * density
        val color = ColorMath.withAlpha(ink, SPACE_LABEL_ALPHA * alpha)
        val micFits = keyWidth >= micSize + 2f * inset
        if (micFits) {
            val centre = rect.left + inset + micSize / 2f
            icons.drawIcon(canvas, SpaceIcon.VOICE, centre, rect.centerY(), micSize, color, accent)
            val half = maxOf(micSize, MIC_TAP_DP * density) / 2f
            micAreas += RectF(centre - half, rect.top, centre + half, rect.bottom)
        }
        val clear = if (micFits) inset + micSize + MIC_GAP_DP * density else 0f
        val room = 2f * (keyWidth / 2f - clear)
        if (room <= 0f || label.isEmpty()) return
        var fitted = size
        textPaint.textSize = fitted
        val wide = textPaint.measureText(label)
        if (wide > room) {
            fitted *= room / wide
            textPaint.textSize = fitted
        }
        if (fitted >= MIN_LABEL_DP * density) {
            val baseline = -(textPaint.ascent() + textPaint.descent()) / 2f
            textPaint.color = color
            canvas.drawText(label, rect.centerX(), rect.centerY() + baseline, textPaint)
        }
        textPaint.textSize = baseSize
    }

    private companion object {
        const val SPACE_LABEL_RATIO = 0.62f
        const val SPACE_LABEL_ALPHA = 0.85f
        const val MIC_RATIO = 1.5f
        const val MIC_GAP_DP = 6f
        const val MIN_LABEL_DP = 7f
        const val MIC_TAP_DP = 44f
        const val MIC_INSET_DP = 10f
    }
}
