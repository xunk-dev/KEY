package com.spacekeyboard.keyboard.ime.theme.backdrop

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import kotlin.math.sin

enum class DiscStyle { BLACK_HOLE, ECLIPSE, MOON }

class DiscLayer(private val style: DiscStyle, private val density: Float) : BackdropLayer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val halo = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.style = Paint.Style.STROKE }
    private var cx = 0f
    private var cy = 0f
    private var radius = 0f
    private var palette: ThemePalette? = null

    override fun resize(width: Int, height: Int) {
        radius = height * if (style == DiscStyle.MOON) MOON_SCALE else DISC_SCALE
        cx = width * if (style == DiscStyle.BLACK_HOLE) 0.5f else 0.8f
        cy = height * 0.5f
        rebuild()
    }

    override fun recolor(palette: ThemePalette) {
        this.palette = palette
        rebuild()
    }

    private fun rebuild() {
        val p = palette ?: return
        if (radius <= 0f) return
        when (style) {
            DiscStyle.BLACK_HOLE -> buildHole(p)
            DiscStyle.ECLIPSE -> buildEclipse(p)
            DiscStyle.MOON -> buildMoon(p)
        }
    }

    private fun buildHole(p: ThemePalette) {
        halo.shader = softGlow(radius * 2.6f, p.glow, 0.5f)
        fill.shader = null
        fill.color = ColorMath.BLACK
        val clear = ColorMath.withAlpha(p.glow, 0f)
        val bright = ColorMath.withAlpha(p.glow, 0.95f)
        ring.shader = SweepGradient(0f, 0f, intArrayOf(bright, clear, bright, clear, bright), null)
        ring.strokeWidth = radius * 0.3f
    }

    private fun buildEclipse(p: ThemePalette) {
        halo.shader = softGlow(radius * 2.2f, p.glow, 0.85f)
        fill.shader = null
        fill.color = ColorMath.BLACK
        ring.shader = null
        ring.color = ColorMath.withAlpha(p.glow, 0.9f)
        ring.strokeWidth = 1.5f * density
    }

    private fun buildMoon(p: ThemePalette) {
        halo.shader = softGlow(radius * 2f, p.glow, 0.4f)
        val dark = ColorMath.mix(p.glow, ColorMath.BLACK, 0.55f)
        val light = ColorMath.mix(p.glow, ColorMath.BLACK, 0.15f)
        fill.shader = RadialGradient(-radius * 0.3f, -radius * 0.3f, radius * 1.3f, light, dark, Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas, seconds: Float) {
        if (palette == null || radius <= 0f) return
        val bob = if (style == DiscStyle.MOON) sin(seconds * 0.5f) * radius * 0.12f else 0f
        canvas.save()
        canvas.translate(cx, cy + bob)
        canvas.drawCircle(0f, 0f, radius * 2.6f, halo)
        if (style == DiscStyle.BLACK_HOLE) drawAccretion(canvas, seconds)
        canvas.drawCircle(0f, 0f, radius, fill)
        if (style == DiscStyle.ECLIPSE) canvas.drawCircle(0f, 0f, radius, ring)
        canvas.restore()
    }

    private fun drawAccretion(canvas: Canvas, seconds: Float) {
        canvas.save()
        canvas.rotate(seconds * SPIN_DEGREES)
        canvas.drawCircle(0f, 0f, radius * 1.18f, ring)
        canvas.restore()
    }

    private companion object {
        const val DISC_SCALE = 0.34f
        const val MOON_SCALE = 0.3f
        const val SPIN_DEGREES = 24f
    }
}
