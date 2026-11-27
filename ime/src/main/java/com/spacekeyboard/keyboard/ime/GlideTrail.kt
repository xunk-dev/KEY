package com.spacekeyboard.keyboard.ime

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.suggest.GlideKey
import com.spacekeyboard.keyboard.core.suggest.GlidePoint
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import kotlin.math.hypot

internal class GlideTrail(private val view: KeyboardView) {
    var keys: List<GlideKey> = emptyList()
    private val points = ArrayList<GlidePoint>()
    private var owner = NONE
    private var drawing = false
    private val line = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = WIDTH_DP * view.density
    }

    fun arms(key: KeySpec, shown: String): Boolean {
        if (!view.glideOn() || key.action != KeyAction.COMMIT || key.pair) return false
        val text = KeyLabels.output(shown)
        return text.length == 1 && Character.isLetter(text[0])
    }

    fun track(touch: KeyTouch, x: Float, y: Float): Boolean {
        if (!touch.glideable || touch.alts.isNotEmpty()) return false
        if (owner == NONE) {
            owner = touch.id
            points.clear()
            points += GlidePoint(touch.downX, touch.downY)
        }
        if (owner != touch.id) return false
        add(x, y)
        if (!touch.glide) {
            if (hypot(x - touch.downX, y - touch.downY) < reach(touch)) return false
            start(touch)
        }
        view.invalidate()
        return true
    }

    fun end(touch: KeyTouch, commit: Boolean) {
        if (touch.id != owner) return
        val path = ArrayList(points)
        owner = NONE
        points.clear()
        drawing = false
        if (!touch.glide) return
        view.invalidate()
        if (commit) view.onGlide(path, keys)
    }

    fun draw(canvas: Canvas, palette: ThemePalette) {
        if (!drawing || points.size < 2) return
        line.rewind()
        line.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) line.lineTo(points[i].x, points[i].y)
        paint.color = palette.glow
        paint.alpha = ALPHA
        canvas.drawPath(line, paint)
    }

    private fun add(x: Float, y: Float) {
        val last = points.last()
        if (points.size < MAX_POINTS && hypot(x - last.x, y - last.y) >= 1f) points += GlidePoint(x, y)
    }

    private fun reach(touch: KeyTouch): Float = maxOf(touch.placed.cell.width * REACH_KEYS, view.slop * 2f)

    private fun start(touch: KeyTouch) {
        touch.glide = true
        touch.swiping = true
        drawing = true
        view.timer.removeCallbacks(touch.longPress)
        view.popups.dismissKeys()
    }

    private companion object {
        const val NONE = -1
        const val WIDTH_DP = 5f
        const val ALPHA = 200
        const val REACH_KEYS = 1.1f
        const val MAX_POINTS = 1500
    }
}
