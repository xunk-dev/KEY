package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import kotlin.math.max
import kotlin.math.min

internal class KeyPopups(private val host: View) {
    private val density = host.resources.displayMetrics.density
    private val preview = Bubble(host.context)
    private val strip = AltStrip(host.context)
    private val note = Bubble(host.context)
    private val previewWindow = window(preview)
    private val stripWindow = window(strip)
    private val noteWindow = window(note)
    private val timer = Handler(Looper.getMainLooper())
    private val hideNote = Runnable { dismiss(noteWindow) }
    private var stripLeft = 0f
    private var stripCell = 0f
    private var stripCount = 0

    fun showPreview(text: String, key: Box, palette: ThemePalette) {
        if (text.isBlank()) return
        preview.set(text, palette, SIZE_RATIO_TEXT)
        val w = max(key.width * 1.15f, MIN_W_DP * density)
        val h = max(key.height * 1.25f, MIN_H_DP * density)
        val x = (key.centerX - w / 2f).coerceIn(0f, max(0f, host.width - w))
        place(previewWindow, w.toInt(), h.toInt(), x.toInt(), (key.top - h - GAP_DP * density).toInt())
    }

    fun hidePreview() = dismiss(previewWindow)

    fun showAlts(items: List<String>, key: Box, fingerX: Float, palette: ThemePalette): Int {
        if (items.isEmpty()) return -1
        val cell = min(max(ALT_CELL_DP * density, key.width), host.width / items.size.toFloat())
        val w = cell * items.size
        val h = max(key.height * 1.15f, ALT_H_DP * density)
        val left = key.left.coerceIn(0f, max(0f, host.width - w))
        stripLeft = left
        stripCell = cell
        stripCount = items.size
        val index = indexAt(fingerX)
        strip.set(items, index, cell, palette)
        place(stripWindow, w.toInt(), h.toInt(), left.toInt(), (key.top - h - GAP_DP * density).toInt())
        return index
    }

    fun indexAt(x: Float): Int =
        if (stripCount == 0 || stripCell <= 0f) -1 else ((x - stripLeft) / stripCell).toInt().coerceIn(0, stripCount - 1)

    fun selectAlt(index: Int) = strip.select(index)

    fun hideAlts() {
        stripCount = 0
        dismiss(stripWindow)
    }

    fun showNote(text: String, palette: ThemePalette) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = NOTE_TEXT_DP * density }
        val w = min(paint.measureText(text) + 2 * NOTE_PAD_DP * density, max(host.width.toFloat(), 1f))
        val h = NOTE_H_DP * density
        note.set(text, palette, NOTE_RATIO_TEXT)
        place(noteWindow, w.toInt(), h.toInt(), ((host.width - w) / 2f).toInt(), (-h - GAP_DP * density).toInt())
        timer.removeCallbacks(hideNote)
        timer.postDelayed(hideNote, NOTE_MS)
    }

    fun dismissKeys() {
        stripCount = 0
        dismiss(previewWindow)
        dismiss(stripWindow)
    }

    fun dismissAll() {
        timer.removeCallbacks(hideNote)
        dismissKeys()
        dismiss(noteWindow)
    }

    private fun window(content: View) = PopupWindow(content, 1, 1).apply {
        isClippingEnabled = false
        isTouchable = false
        isFocusable = false
    }

    private fun place(win: PopupWindow, w: Int, h: Int, x: Int, y: Int) {
        if (!host.isAttachedToWindow || host.windowToken == null) return
        val origin = IntArray(2)
        host.getLocationInWindow(origin)
        val absX = origin[0] + x
        val absY = origin[1] + y
        try {
            if (win.isShowing) {
                win.update(absX, absY, w, h)
            } else {
                win.width = w
                win.height = h
                win.showAtLocation(host, Gravity.TOP or Gravity.LEFT, absX, absY)
            }
        } catch (_: RuntimeException) {
        }
    }

    private fun dismiss(win: PopupWindow) {
        try {
            if (win.isShowing) win.dismiss()
        } catch (_: RuntimeException) {
        }
    }

    private class Bubble(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = density }
        private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        private val rect = RectF()
        private var text = ""
        private var ratio = SIZE_RATIO_TEXT

        fun set(value: String, palette: ThemePalette, textRatio: Float) {
            text = value
            ratio = textRatio
            fill.color = palette.keyPressed
            line.color = palette.glow
            ink.color = palette.keyTextPressed
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val radius = min(width, height) * 0.22f
            rect.set(density, density, width - density, height - density)
            canvas.drawRoundRect(rect, radius, radius, fill)
            canvas.drawRoundRect(rect, radius, radius, line)
            ink.textSize = if (ratio == NOTE_RATIO_TEXT) NOTE_TEXT_DP * density else height * ratio
            canvas.drawText(text, width / 2f, height / 2f - (ink.ascent() + ink.descent()) / 2f, ink)
        }
    }

    private class AltStrip(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mark = Paint(Paint.ANTI_ALIAS_FLAG)
        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = density }
        private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        private val rect = RectF()
        private var items: List<String> = emptyList()
        private var selected = -1
        private var cell = 0f
        private var pressedInk = 0

        fun set(values: List<String>, index: Int, cellWidth: Float, palette: ThemePalette) {
            items = values
            selected = index
            cell = cellWidth
            fill.color = palette.keyFill
            mark.color = palette.keyPressed
            line.color = palette.glow
            ink.color = palette.keyText
            pressedInk = palette.keyTextPressed
            invalidate()
        }

        fun select(index: Int) {
            if (index == selected) return
            selected = index
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val radius = min(width, height) * 0.2f
            rect.set(density, density, width - density, height - density)
            canvas.drawRoundRect(rect, radius, radius, fill)
            canvas.drawRoundRect(rect, radius, radius, line)
            ink.textSize = height * SIZE_RATIO_TEXT
            val baseline = height / 2f - (ink.ascent() + ink.descent()) / 2f
            items.forEachIndexed { i, text ->
                val left = i * cell
                if (i == selected) {
                    rect.set(left + 3 * density, 3 * density, left + cell - 3 * density, height - 3 * density)
                    canvas.drawRoundRect(rect, radius, radius, mark)
                }
                val color = ink.color
                if (i == selected) ink.color = pressedInk
                canvas.drawText(text, left + cell / 2f, baseline, ink)
                ink.color = color
            }
        }
    }

    private companion object {
        const val SIZE_RATIO_TEXT = 0.46f
        const val NOTE_RATIO_TEXT = -1f
        const val MIN_W_DP = 44f
        const val MIN_H_DP = 52f
        const val ALT_CELL_DP = 42f
        const val ALT_H_DP = 52f
        const val GAP_DP = 4f
        const val NOTE_TEXT_DP = 14f
        const val NOTE_PAD_DP = 14f
        const val NOTE_H_DP = 36f
        const val NOTE_MS = 2200L
    }
}
