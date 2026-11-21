package com.spacekeyboard.keyboard.ime.theme.backdrop

import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette

interface BackdropLayer {
    fun resize(width: Int, height: Int)
    fun recolor(palette: ThemePalette)
    fun draw(canvas: Canvas, seconds: Float)
}

class LayerStack(private val layers: List<BackdropLayer>) : BackdropLayer {
    override fun resize(width: Int, height: Int) = layers.forEach { it.resize(width, height) }

    override fun recolor(palette: ThemePalette) = layers.forEach { it.recolor(palette) }

    override fun draw(canvas: Canvas, seconds: Float) = layers.forEach { it.draw(canvas, seconds) }
}

enum class ColorRole { GLOW, BORDER, MIX }

fun ColorRole.pick(palette: ThemePalette): Int = when (this) {
    ColorRole.GLOW -> palette.glow
    ColorRole.BORDER -> palette.border
    ColorRole.MIX -> ColorMath.mix(palette.glow, palette.border, 0.5f)
}

fun softGlow(radius: Float, color: Int, alpha: Float): RadialGradient = RadialGradient(
    0f,
    0f,
    radius.coerceAtLeast(1f),
    ColorMath.withAlpha(color, alpha),
    ColorMath.withAlpha(color, 0f),
    Shader.TileMode.CLAMP,
)

fun fract(value: Float): Float = value - kotlin.math.floor(value)
