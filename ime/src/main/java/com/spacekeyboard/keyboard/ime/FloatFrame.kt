package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.spacekeyboard.keyboard.core.layout.Limits

class FloatFrame(context: Context) : FrameLayout(context) {
    var active = false
        private set
    var resizing = false
        private set
    var leftPos = 0
        private set
    var topPos = 0
        private set
    var onPlaced: (left: Int, top: Int) -> Unit = { _, _ -> }
    var edgeColor: Int = 0x9935B3C7.toInt()
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }
    private var widthPercent = Limits.FLOAT_WIDTH_DEFAULT
    private var holding = false
    private var centerPending = false
    private var railOpen = false
    private var edgeLeft = 0f
    private var edgeRight = 1f
    private var dock: View? = null
    private var overlay: View? = null
    private var chrome: FloatChrome? = null
    private val density = resources.displayMetrics.density
    private val spot = IntArray(2)
    private val card = RectF()
    private val cardPath = Path()
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = density
    }

    private val windowed: Boolean get() = active || resizing

    init {
        setWillNotDraw(false)
    }

    fun attach(dock: View, overlay: View, chrome: FloatChrome) {
        this.dock = dock
        this.overlay = overlay
        this.chrome = chrome
        addView(dock, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        addView(overlay, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        chrome.all().forEach { addView(it, LayoutParams(WRAP_CONTENT, WRAP_CONTENT)) }
        chrome.setShown(false)
    }

    fun apply(
        active: Boolean,
        widthPercent: Int,
        left: Int,
        top: Int,
        opacity: Int,
        resizing: Boolean = false,
        edgeLeft: Float = 0f,
        edgeRight: Float = 1f,
    ) {
        val moved = !holding && (leftPos != left || topPos != top)
        val edgesMoved = this.edgeLeft != edgeLeft || this.edgeRight != edgeRight
        val same = this.active == active && this.resizing == resizing && this.widthPercent == widthPercent && !moved && !edgesMoved
        dock?.alpha = if (active) opacity / ALPHA_SCALE else 1f
        if (same) return
        this.active = active
        this.resizing = resizing
        this.widthPercent = widthPercent
        this.edgeLeft = edgeLeft
        this.edgeRight = edgeRight
        if (!holding) {
            leftPos = left
            topPos = top
        }
        chrome?.setShown(active)
        requestLayout()
        invalidate()
    }

    fun setRailOpen(open: Boolean) {
        if (railOpen == open) return
        railOpen = open
        requestLayout()
        invalidate()
    }

    fun holdPosition(on: Boolean) {
        holding = on
    }

    fun requestCenter() {
        centerPending = true
        requestLayout()
    }

    fun dragFrom(baseLeft: Int, baseTop: Int, dx: Float, dy: Float) {
        if (width <= 0 || height <= 0) return
        leftPos = (baseLeft + dx * Limits.FLOAT_POS_MAX / width).toInt().coerceIn(0, maxLeft(width))
        topPos = (baseTop + dy * Limits.FLOAT_POS_MAX / height).toInt().coerceIn(0, maxTop(height))
        requestLayout()
    }

    fun placedLeft(): Int = if (width > 0) (dock?.left ?: 0) * Limits.FLOAT_POS_MAX / width else leftPos

    fun placedTop(): Int = if (height > 0) (dock?.top ?: 0) * Limits.FLOAT_POS_MAX / height else topPos

    fun dockHeight(): Int = dock?.height ?: 0

    fun dockBounds(out: Rect): Boolean {
        if (!windowed) return false
        val dock = dock ?: return false
        getLocationInWindow(spot)
        if (!active) {
            out.set(
                spot[0] + dock.left + (dock.width * edgeLeft).toInt(),
                spot[1] + dock.top,
                spot[0] + dock.left + (dock.width * edgeRight).toInt(),
                spot[1] + dock.bottom,
            )
            return true
        }
        out.set(spot[0] + dock.left, spot[1] + dock.top, spot[0] + dock.right, spot[1] + dock.bottom)
        chrome?.all().orEmpty().filter { it.visibility == VISIBLE }.forEach {
            out.union(spot[0] + it.left, spot[1] + it.top, spot[0] + it.right, spot[1] + it.bottom)
        }
        return true
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val dock = dock
        if (!windowed || dock == null) return super.onMeasure(widthSpec, heightSpec)
        val width = MeasureSpec.getSize(widthSpec)
        val travel = resources.displayMetrics.heightPixels * TRAVEL_PERCENT / PERCENT
        val rail = railPx()
        val dockWidth = if (resizing) width else width * widthPercent / PERCENT
        dock.measure(makeMeasureSpec(dockWidth, EXACTLY), makeMeasureSpec(travel - rail, AT_MOST))
        overlay?.measure(makeMeasureSpec(dock.measuredWidth, EXACTLY), makeMeasureSpec(dock.measuredHeight, EXACTLY))
        if (active) chrome?.measure(dock.measuredWidth, rail, popoverPx())
        setMeasuredDimension(width, travel)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val dock = dock
        if (!windowed || dock == null) return super.onLayout(changed, left, top, right, bottom)
        val w = right - left
        val h = bottom - top
        if (centerPending && active && w > 0 && h > 0) centerNow(dock, w, h)
        val x = if (resizing) 0 else (w * leftPos / Limits.FLOAT_POS_MAX).coerceIn(0, maxOf(0, w - dock.measuredWidth))
        val y = if (resizing) maxOf(0, h - dock.measuredHeight) else topFor(h, dock.measuredHeight)
        dock.layout(x, y, x + dock.measuredWidth, y + dock.measuredHeight)
        overlay?.layout(x, y, x + dock.measuredWidth, y + dock.measuredHeight)
        if (active) chrome?.layout(Rect(x, y, x + dock.measuredWidth, y + dock.measuredHeight), h, railPx())
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val shape = if (child === dock || child === chrome?.rail) cardShape() else null
        if (shape == null) return super.drawChild(canvas, child, drawingTime)
        val saved = canvas.save()
        canvas.clipPath(shape)
        val drawn = super.drawChild(canvas, child, drawingTime)
        canvas.restoreToCount(saved)
        return drawn
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!active || cardShape() == null) return
        stroke.color = edgeColor
        val radius = CARD_RADIUS_DP * density
        canvas.drawRoundRect(card.apply { inset(density / 2f, density / 2f) }, radius, radius, stroke)
    }

    private fun cardShape(): Path? {
        val dock = dock ?: return null
        if (dock.width <= 0 || dock.height <= 0) return null
        when {
            active -> {
                val footer = chrome?.rail?.takeIf { it.visibility == VISIBLE }?.bottom ?: dock.bottom
                card.set(dock.left.toFloat(), dock.top.toFloat(), dock.right.toFloat(), footer.toFloat())
            }
            resizing -> card.set(
                dock.left + dock.width * edgeLeft,
                dock.top.toFloat(),
                dock.left + dock.width * edgeRight,
                dock.bottom.toFloat(),
            )
            else -> return null
        }
        val radius = CARD_RADIUS_DP * density
        return cardPath.apply {
            reset()
            addRoundRect(card, radius, radius, Path.Direction.CW)
        }
    }

    private fun topFor(h: Int, dockHeight: Int): Int =
        (h * topPos / Limits.FLOAT_POS_MAX).coerceIn(0, maxOf(0, h - dockHeight - railPx()))

    private fun centerNow(dock: View, w: Int, h: Int) {
        centerPending = false
        val left = maxOf(0, w - dock.measuredWidth) / 2
        val top = maxOf(0, h - dock.measuredHeight - railPx()) / 2
        leftPos = left * Limits.FLOAT_POS_MAX / w
        topPos = top * Limits.FLOAT_POS_MAX / h
        post { onPlaced(leftPos, topPos) }
    }

    private fun maxLeft(w: Int): Int = maxOf(0, w - (dock?.width ?: 0)) * Limits.FLOAT_POS_MAX / w

    private fun maxTop(h: Int): Int = maxOf(0, h - (dock?.height ?: 0) - railPx()) * Limits.FLOAT_POS_MAX / h

    private fun railPx(): Int = when {
        !active -> 0
        railOpen -> (RAIL_OPEN_DP * density).toInt()
        else -> (RAIL_IDLE_DP * density).toInt()
    }

    private fun popoverPx(): Int = (POPOVER_DP * density).toInt()

    private companion object {
        const val PERCENT = 100
        const val ALPHA_SCALE = 100f
        const val TRAVEL_PERCENT = 85
        const val POPOVER_DP = 76
        const val RAIL_IDLE_DP = 20
        const val RAIL_OPEN_DP = 42
        const val CARD_RADIUS_DP = 22
    }
}
