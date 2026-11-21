package com.spacekeyboard.keyboard.ime.theme.backdrop

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

data class BlobSpec(
    val roles: List<ColorRole>,
    val radiusFraction: Float,
    val alpha: Float,
    val speed: Float,
)

class BlobLayer(private val spec: BlobSpec, seed: Int) : BackdropLayer {
    private val random = Random(seed)
    private val count = spec.roles.size
    private val phaseX = FloatArray(count) { random.nextFloat() * TAU }
    private val phaseY = FloatArray(count) { random.nextFloat() * TAU }
    private val rateX = FloatArray(count) { 0.6f + random.nextFloat() }
    private val rateY = FloatArray(count) { 0.6f + random.nextFloat() }
    private val shaders = arrayOfNulls<RadialGradient>(count)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
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
        val current = palette ?: return
        val radius = max(width, height) * spec.radiusFraction
        for (i in 0 until count) {
            shaders[i] = softGlow(radius, spec.roles[i].pick(current), spec.alpha)
        }
    }

    override fun draw(canvas: Canvas, seconds: Float) {
        if (palette == null) return
        val radius = max(width, height) * spec.radiusFraction
        for (i in 0 until count) {
            val x = width * (0.5f + 0.42f * sin(seconds * spec.speed * rateX[i] + phaseX[i]))
            val y = height * (0.5f + 0.42f * sin(seconds * spec.speed * rateY[i] + phaseY[i]))
            paint.shader = shaders[i]
            canvas.save()
            canvas.translate(x, y)
            canvas.drawCircle(0f, 0f, radius, paint)
            canvas.restore()
        }
    }

    private companion object {
        const val TAU = 6.2831855f
    }
}
