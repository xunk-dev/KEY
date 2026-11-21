package com.spacekeyboard.keyboard.ime.theme.backdrop

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import kotlin.math.sin

class BandLayer : BackdropLayer {
    private val roles = listOf(ColorRole.GLOW, ColorRole.MIX, ColorRole.BORDER)
    private val shaders = arrayOfNulls<LinearGradient>(roles.size)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
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
        roles.forEachIndexed { i, role ->
            val color = role.pick(p)
            shaders[i] = LinearGradient(
                0f,
                0f,
                0f,
                height,
                intArrayOf(ColorMath.withAlpha(color, 0f), ColorMath.withAlpha(color, 0.55f), ColorMath.withAlpha(color, 0f)),
                null,
                Shader.TileMode.CLAMP,
            )
        }
    }

    override fun draw(canvas: Canvas, seconds: Float) {
        if (palette == null || width <= 0f) return
        for (i in roles.indices) {
            paint.shader = shaders[i]
            buildCurtain(i, seconds)
            canvas.drawPath(path, paint)
        }
    }

    private fun buildCurtain(index: Int, seconds: Float) {
        val base = height * (0.25f + 0.2f * index)
        val thick = height * 0.45f
        val step = width / POINTS
        path.reset()
        for (n in 0..POINTS) {
            val y = base + wave(n * step, index, seconds)
            if (n == 0) path.moveTo(0f, y) else path.lineTo(n * step, y)
        }
        for (n in POINTS downTo 0) {
            path.lineTo(n * step, base + thick + wave(n * step, index, seconds))
        }
        path.close()
    }

    private fun wave(x: Float, index: Int, seconds: Float): Float =
        height * 0.12f * sin(x / width * (3f + index) + seconds * (0.5f + 0.2f * index) + index * 2f)

    private companion object {
        const val POINTS = 20
    }
}
