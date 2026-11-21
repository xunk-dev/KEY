package com.spacekeyboard.keyboard.ime.theme.backdrop

import android.graphics.Canvas
import android.graphics.Paint
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class StarMotion { TWINKLE, DRIFT, ORBIT }

data class StarSpec(
    val count: Int,
    val minRadiusDp: Float,
    val maxRadiusDp: Float,
    val motion: StarMotion,
    val tint: Float,
    val alpha: Float,
    val dx: Float = 0f,
    val dy: Float = 0f,
)

class StarLayer(private val spec: StarSpec, private val density: Float, seed: Int) : BackdropLayer {
    private val random = Random(seed)
    private val a = FloatArray(spec.count) { random.nextFloat() }
    private val b = FloatArray(spec.count) { random.nextFloat() }
    private val radius = FloatArray(spec.count) {
        (spec.minRadiusDp + random.nextFloat() * (spec.maxRadiusDp - spec.minRadiusDp)) * density
    }
    private val phase = FloatArray(spec.count) { random.nextFloat() * TAU }
    private val speed = FloatArray(spec.count) { 0.6f + random.nextFloat() * 1.8f }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var width = 0f
    private var height = 0f
    private var color = 0

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun recolor(palette: ThemePalette) {
        color = ColorMath.mix(ColorMath.WHITE, palette.glow, spec.tint)
    }

    override fun draw(canvas: Canvas, seconds: Float) {
        for (i in 0 until spec.count) {
            val twinkle = 0.55f + 0.45f * sin(seconds * speed[i] + phase[i])
            paint.color = ColorMath.withAlpha(color, spec.alpha * twinkle)
            when (spec.motion) {
                StarMotion.ORBIT -> orbit(canvas, i, seconds)
                else -> place(canvas, i, seconds)
            }
        }
    }

    private fun place(canvas: Canvas, i: Int, seconds: Float) {
        val x = fract(a[i] + spec.dx * seconds * speed[i])
        val y = fract(b[i] + spec.dy * seconds * speed[i])
        canvas.drawCircle(x * width, y * height, radius[i], paint)
    }

    private fun orbit(canvas: Canvas, i: Int, seconds: Float) {
        val span = 0.08f + a[i] * 0.92f
        val angle = phase[i] + seconds * ORBIT_SPEED / (0.3f + span)
        val x = width * 0.5f + cos(angle) * span * width * 0.55f
        val y = height * 0.5f + sin(angle) * span * height * 0.55f
        canvas.drawCircle(x, y, radius[i], paint)
    }

    private companion object {
        const val TAU = 6.2831855f
        const val ORBIT_SPEED = 0.12f
    }
}
