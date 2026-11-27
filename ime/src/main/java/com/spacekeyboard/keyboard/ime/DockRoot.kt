package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.core.graphics.ColorUtils
import android.widget.LinearLayout
import androidx.compose.ui.platform.ComposeView
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController

class DockRoot(
    private val context: Context,
    private val theme: ThemeController,
    private val layouts: LayoutController,
    private val resize: ResizeController,
) {
    private var frame: FloatFrame? = null
    private var chrome: FloatChrome? = null
    private var overlay: ComposeView? = null
    private val float = FloatController()
    private val bounds = Rect()
    private var resumeFloat = false
    private var resizeBase = 0

    fun build(dock: LinearLayout): View {
        val root = FloatFrame(context).also { frame = it }
        val mover = FloatMover(root, layouts)
        val resizer = FloatResizer(layouts, root)
        val bar = buildFloatChrome(context, theme, layouts, float, mover, resizer).also { chrome = it }
        val resizeView = createOverlay()
        root.onPlaced = { left, top -> layouts.setFloatPosition(left, top) }
        root.attach(dock, resizeView, bar)
        float.onChange = ::syncPopover
        dock.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            resizeView.setPadding(v.paddingLeft, 0, v.paddingRight, v.paddingBottom)
            if (resizeView.visibility == View.VISIBLE) resizeView.post { excludeEdgeGestures(resizeView, GRIP_BAND_DP) }
        }
        resizeView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (v.visibility == View.VISIBLE) excludeEdgeGestures(v, GRIP_BAND_DP)
        }
        apply(layouts.snapshot)
        return root
    }

    fun apply(snapshot: LayoutSnapshot) {
        val prefs = snapshot.prefs
        val floating = prefs.mode == PanelMode.FLOAT
        if (!floating) {
            float.closePopover()
            float.closeRail()
        }
        val resizing = resize.open && !floating
        val (edgeLeft, edgeRight) = if (resizing) edgesOf(prefs, 1f) else 0f to 1f
        frame?.edgeColor = ColorUtils.setAlphaComponent(theme.snapshot.palette.glow, EDGE_ALPHA)
        frame?.apply(
            floating, prefs.floatWidthPercent, prefs.floatLeft, prefs.floatTop, prefs.floatOpacityPercent,
            resizing = resizing, edgeLeft = edgeLeft, edgeRight = edgeRight,
        )
        syncPopover()
        syncOverlay()
    }
    fun toggleFloat() {
        if (layouts.snapshot.prefs.mode == PanelMode.FLOAT) {
            layouts.dockFloat()
            return
        }
        resize.close()
        layouts.enterFloat()
        frame?.requestCenter()
    }

    fun startResize() {
        resumeFloat = layouts.snapshot.prefs.mode == PanelMode.FLOAT
        if (resumeFloat) layouts.setMode(PanelMode.NORMAL)
        resizeBase = frame?.dockHeight() ?: 0
        resize.start()
    }

    fun fillInsets(outInsets: InputMethodService.Insets, windowHeight: Int) {
        val root = frame ?: return
        if (!root.dockBounds(bounds)) return
        val top = if (root.resizing && resizeBase > 0) (windowHeight - resizeBase).coerceIn(0, windowHeight) else windowHeight
        outInsets.contentTopInsets = top
        outInsets.visibleTopInsets = top
        outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_REGION
        outInsets.touchableRegion.set(bounds)
    }

    private fun createOverlay(): ComposeView {
        val view = ComposeView(context)
        view.setContent { ResizeOverlay(theme, layouts, resize) }
        overlay = view
        resize.onChange = {
            apply(layouts.snapshot)
            syncOverlay()
            if (!resize.open) restoreFloat()
        }
        syncOverlay()
        return view
    }

    private fun restoreFloat() {
        if (!resumeFloat) return
        resumeFloat = false
        if (layouts.snapshot.prefs.mode == PanelMode.NORMAL) layouts.setMode(PanelMode.FLOAT)
    }

    private fun syncPopover() {
        val open = float.popoverOpen && frame?.active == true
        chrome?.setPopoverShown(open)
        frame?.setRailOpen(float.railOpen && frame?.active == true)
        frame?.requestLayout()
    }

    private fun syncOverlay() {
        val show = resize.open && frame?.active != true
        overlay?.apply {
            visibility = if (show) View.VISIBLE else View.GONE
            if (!show && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) systemGestureExclusionRects = emptyList()
        }
    }

    private companion object {
        const val GRIP_BAND_DP = 160
        const val EDGE_ALPHA = 0x99
    }
}
