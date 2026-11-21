package com.spacekeyboard.keyboard.ime.theme

import android.content.Context
import android.graphics.Canvas
import android.view.Choreographer
import android.view.MotionEvent
import android.widget.LinearLayout
import com.spacekeyboard.keyboard.core.theme.BackdropKind
import com.spacekeyboard.keyboard.ime.theme.backdrop.BackdropFactory
import com.spacekeyboard.keyboard.ime.theme.backdrop.BackdropLayer

class ThemedFrame(
    context: Context,
    private val controller: ThemeController,
    private val idlePause: Boolean = true,
) : LinearLayout(context) {
    private val density = resources.displayMetrics.density
    private val frames = Choreographer.getInstance()
    private var backdrop: BackdropLayer? = null
    private var kind: BackdropKind? = null
    private var snapshot = controller.snapshot
    private var subscription: AutoCloseable? = null
    private var shown = true
    private var running = false
    private var lastFrame = 0L
    private var lastTouch = System.nanoTime()
    private var seconds = 0f

    private val callback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            if (lastFrame == 0L) lastFrame = frameTimeNanos
            val elapsed = frameTimeNanos - lastFrame
            if (elapsed >= FRAME_NANOS) {
                lastFrame = frameTimeNanos
                seconds += elapsed / NANOS_PER_SECOND
                invalidate()
            }
            if (idlePause && System.nanoTime() - lastTouch > IDLE_NANOS) stopFrames() else frames.postFrameCallback(this)
        }
    }

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
    }

    fun setShown(value: Boolean) {
        shown = value
        wake()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) wake()
        return super.dispatchTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscription = controller.subscribe(::render)
        wake()
    }

    override fun onDetachedFromWindow() {
        subscription?.close()
        subscription = null
        stopFrames()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backdrop?.resize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(snapshot.palette.background)
        backdrop?.draw(canvas, if (animated()) seconds else STILL_SECONDS)
    }

    private fun render(next: ThemeSnapshot) {
        snapshot = next
        val wanted = next.choice.theme.backdrop
        if (wanted != kind) {
            kind = wanted
            backdrop = BackdropFactory.create(wanted, density).also { it.resize(width, height) }
        }
        backdrop?.recolor(next.palette)
        wake()
        invalidate()
    }

    private fun animated(): Boolean = snapshot.motion && shown && isAttachedToWindow

    private fun wake() {
        lastTouch = System.nanoTime()
        if (animated()) startFrames() else stopFrames()
    }

    private fun startFrames() {
        if (running) return
        running = true
        lastFrame = 0L
        frames.postFrameCallback(callback)
    }

    private fun stopFrames() {
        if (!running) return
        running = false
        frames.removeFrameCallback(callback)
        invalidate()
    }

    private companion object {
        const val FRAME_NANOS = 50_000_000L
        const val IDLE_NANOS = 8_000_000_000L
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val STILL_SECONDS = 4f
    }
}
