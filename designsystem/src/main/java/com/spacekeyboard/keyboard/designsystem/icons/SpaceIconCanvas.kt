package com.spacekeyboard.keyboard.designsystem.icons

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.asAndroidPath

class SpaceIconCanvas {
    private val rect = RectF()
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = PEN
    }
    private val solid = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dash = DashPathEffect(floatArrayOf(DASH, DASH), 0f)
    private var inkColor = 0
    private var accentColor = 0

    fun draw(canvas: Canvas, icon: SpaceIcon, centerX: Float, centerY: Float, size: Float, color: Int, mirror: Boolean = false, accent: Int = color) {
        stroke.color = color
        solid.color = color
        inkColor = color
        accentColor = accent
        val flip = mirror
        val unit = size / GRID
        canvas.save()
        canvas.translate(centerX - size / 2f, centerY - size / 2f)
        if (flip) canvas.translate(size, 0f)
        canvas.scale(if (flip) -unit else unit, unit)
        icon.parts().forEach { drawPart(canvas, it) }
        canvas.restore()
    }

    private fun drawPart(canvas: Canvas, part: IconPart) {
        when (part) {
            is Toned -> drawToned(canvas, part)
            is StrokePath -> canvas.drawPath(part.path.asAndroidPath(), stroke)
            is Fill -> {
                val android = part.path.asAndroidPath()
                canvas.drawPath(android, solid)
                canvas.drawPath(android, stroke)
            }
            is Dot -> {
                canvas.drawCircle(part.x, part.y, part.r, solid)
                canvas.drawCircle(part.x, part.y, part.r, stroke)
            }
            is Ring -> canvas.drawCircle(part.x, part.y, part.r, stroke)
            is Box -> {
                rect.set(part.x, part.y, part.x + part.w, part.y + part.h)
                stroke.pathEffect = if (part.dashed) dash else null
                canvas.drawRoundRect(rect, part.radius, part.radius, stroke)
                stroke.pathEffect = null
            }
        }
    }

    private fun drawToned(canvas: Canvas, part: Toned) {
        val tone = if (part.tone == Tone.ACCENT) accentColor else softOf(inkColor)
        stroke.color = tone
        solid.color = tone
        drawPart(canvas, part.part)
        stroke.color = inkColor
        solid.color = inkColor
    }

    private fun softOf(argb: Int): Int {
        val alpha = ((argb ushr 24) * SOFT_ALPHA).toInt().coerceIn(0, 255)
        return (alpha shl 24) or (argb and 0x00FFFFFF)
    }

    private companion object {
        const val GRID = 24f
        const val PEN = 2f
        const val DASH = 3.2f
    }
}
