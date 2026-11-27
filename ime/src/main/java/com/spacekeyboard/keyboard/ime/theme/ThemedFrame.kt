package com.spacekeyboard.keyboard.ime.theme

import android.content.Context
import android.graphics.Canvas
import android.widget.LinearLayout

class ThemedFrame(
    context: Context,
    private val controller: ThemeController,
    @Suppress("UNUSED_PARAMETER") idlePause: Boolean = true,
) : LinearLayout(context) {
    private var snapshot = controller.snapshot
    private var subscription: AutoCloseable? = null
    private val wallpaper = WallpaperPresenter(this, controller)

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
    }

    fun setShown(value: Boolean) = wallpaper.setShown(value)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscription = controller.subscribe(::render)
    }

    override fun onDetachedFromWindow() {
        subscription?.close()
        subscription = null
        wallpaper.release()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        wallpaper.setVisible(isVisible)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(snapshot.palette.background)
    }

    private fun render(next: ThemeSnapshot) {
        snapshot = next
        wallpaper.render(next)
        invalidate()
    }
}
