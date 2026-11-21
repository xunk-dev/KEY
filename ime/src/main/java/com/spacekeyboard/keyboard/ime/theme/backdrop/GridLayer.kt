package com.spacekeyboard.keyboard.ime.theme.backdrop

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette

class GridLayer(private val density: Float) : BackdropLayer {
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = density }
    private val sky = Paint()
    private var palette: ThemePalette? = null
    private var width = 0f
    private var height = 0f

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
        rebuild()
    }

    override fun recolor(palette: ThemePalette) {
        this.palette = palette
        rebuild()
    }

    private fun rebuild() {
        val p = palette ?: return
        val horizon = height * HORIZON
        sky.shader = LinearGradient(
            0f,
            horizon * 0.2f,
            0f,
            horizon,
            ColorMath.withAlpha(p.glow, 0f),
            ColorMath.withAlpha(p.glow, 0.35f),
            Shader.TileMode.CLAMP,
        )
    }

    override fun draw(canvas: Canvas, seconds: Float) {
        val p = palette ?: return
        val horizon = height * HORIZON
        canvas.drawRect(0f, 0f, width, horizon, sky)
        line.color = ColorMath.withAlpha(p.border, 0.9f)
        canvas.drawLine(0f, horizon, width, horizon, line)
        drawVertical(canvas, p, horizon)
        drawHorizontal(canvas, p, horizon, seconds)
    }

    private fun drawVertical(canvas: Canvas, p: ThemePalette, horizon: Float) {
        line.color = ColorMath.withAlpha(p.border, 0.45f)
        val cx = width / 2f
        for (i in -LANES..LANES) {
            canvas.drawLine(cx + i * width / (LANES * 8f), horizon, cx + i * width / LANES, height, line)
        }
    }

    private fun drawHorizontal(canvas: Canvas, p: ThemePalette, horizon: Float, seconds: Float) {
        val scroll = fract(seconds * SCROLL_SPEED)
        for (k in 0 until ROWS) {
            val t = (k + scroll) / ROWS
            val y = horizon + (height - horizon) * t * t
            line.color = ColorMath.withAlpha(p.glow, 0.15f + 0.6f * t)
            canvas.drawLine(0f, y, width, y, line)
        }
    }

    private companion object {
        const val HORIZON = 0.38f
        const val LANES = 6
        const val ROWS = 6
        const val SCROLL_SPEED = 0.25f
    }
}
