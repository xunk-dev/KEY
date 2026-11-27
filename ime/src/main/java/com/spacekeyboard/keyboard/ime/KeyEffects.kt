package com.spacekeyboard.keyboard.ime

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.spacekeyboard.keyboard.core.theme.ColorMath

internal class KeyEffects(private val density: Float) {
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = density
    }
    private val spread = RectF()

    fun halo(canvas: Canvas, rect: RectF, radius: Float, color: Int) {
        for (layer in HALO_LAYERS downTo 1) {
            val grow = layer * HALO_STEP_DP * density
            spread.set(rect.left - grow, rect.top - grow, rect.right + grow, rect.bottom + grow)
            glow.color = ColorMath.withAlpha(color, HALO_ALPHA * (HALO_LAYERS + 1 - layer))
            canvas.drawRoundRect(spread, radius + grow, radius + grow, glow)
        }
    }

    fun shadow(canvas: Canvas, rect: RectF, radius: Float, color: Int) {
        val drop = SHADOW_DP * density
        spread.set(rect.left, rect.top + drop, rect.right, rect.bottom + drop)
        glow.color = color
        canvas.drawRoundRect(spread, radius, radius, glow)
    }

    fun orbit(canvas: Canvas, rect: RectF, ink: Int) {
        val reach = rect.width() / 2f - ORBIT_INSET_DP * density
        if (reach <= 0f) return
        stroke.color = ColorMath.withAlpha(ink, ORBIT_ALPHA)
        canvas.drawCircle(rect.centerX(), rect.centerY(), reach, stroke)
    }

    fun ruler(canvas: Canvas, rect: RectF, ink: Int, alpha: Float) {
        val step = TICK_STEP_DP * density
        val inset = rect.height() / 2f
        val usable = rect.width() - 2f * inset
        if (usable < step * MIN_TICKS) return
        val count = (usable / step).toInt()
        val gap = usable / count
        val base = rect.bottom - TICK_BASE_DP * density
        for (i in 0..count) {
            val major = i % TICK_MAJOR_EVERY == 0
            val length = (if (major) TICK_MAJOR_DP else TICK_MINOR_DP) * density
            val x = rect.left + inset + i * gap
            stroke.color = ColorMath.withAlpha(ink, (if (major) TICK_MAJOR_ALPHA else TICK_MINOR_ALPHA) * alpha)
            canvas.drawLine(x, base - length, x, base, stroke)
        }
    }

    private companion object {
        const val SHADOW_DP = 1.5f
        const val HALO_LAYERS = 3
        const val HALO_STEP_DP = 2f
        const val HALO_ALPHA = 0.07f
        const val ORBIT_INSET_DP = 4f
        const val ORBIT_ALPHA = 0.3f
        const val TICK_STEP_DP = 7f
        const val TICK_BASE_DP = 7f
        const val TICK_MINOR_DP = 3f
        const val TICK_MAJOR_DP = 6f
        const val TICK_MAJOR_EVERY = 5
        const val TICK_MINOR_ALPHA = 0.2f
        const val TICK_MAJOR_ALPHA = 0.4f
        const val MIN_TICKS = 6
    }
}
