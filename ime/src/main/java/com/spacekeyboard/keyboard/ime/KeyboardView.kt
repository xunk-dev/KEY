package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import com.spacekeyboard.keyboard.core.layout.GeometryInput
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.layout.KeyboardGeometry
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.layout.SlotRef
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.ThemeSnapshot
import kotlin.math.abs
import kotlin.math.hypot

class KeyboardView(
    context: Context,
    private val theme: ThemeController,
    private val layouts: LayoutController,
    private val fixedLayoutId: String? = null,
    private val editing: Boolean = false,
    private val onSwap: (SlotRef, SlotRef) -> Unit = { _, _ -> },
    private val onKey: (KeySpec) -> Unit = {},
    private val onKeyTouch: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    private val onDragging: (Boolean) -> Unit = {},
    private val onCursorMove: (direction: Int) -> Unit = {},
    private val onKeyLongPress: (KeySpec) -> Boolean = { false },
    private val onResolve: (Placement, PlacedKey, Float, Float) -> PlacedKey = { _, hit, _, _ -> hit },
) : View(context) {
    private val density = resources.displayMetrics.density
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private val painter = KeyPainter(context)
    private val popups = KeyPopups(this)
    private val feedback = KeyFeedback(this)
    private val timer = Handler(Looper.getMainLooper())
    private val touches = SparseArray<KeyTouch>()
    private val pressedKeys = ArrayList<PlacedKey>()
    private var themeSnapshot = theme.snapshot
    private var layoutSnapshot = layouts.snapshot
    private var cached: Placement? = null
    private val dragger = LayoutDrag(this, ::placement, slop, onSwap, onDragging)
    private val subscriptions = ArrayList<AutoCloseable>()
    private val wordDelete = KeySpec(KeyTiming.WORD_ID, "", KeyAction.DELETE_WORD, 1f, false)
    var showPreviews = true
    private val access = KeyAccessibility(this, { placement().keys }, { describeKey(context, layoutSnapshot, it) }, ::pressForAccessibility)

    init {
        if (!editing) ViewCompat.setAccessibilityDelegate(this, access)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscriptions += theme.subscribe(::renderTheme)
        subscriptions += layouts.subscribe(::renderLayout)
    }

    override fun onDetachedFromWindow() {
        dragger.end(false)
        cancelAll()
        popups.dismissAll()
        subscriptions.forEach { it.close() }
        subscriptions.clear()
        super.onDetachedFromWindow()
    }

    fun showNote(text: String) = popups.showNote(text, themeSnapshot.palette)

    private fun renderTheme(next: ThemeSnapshot) {
        themeSnapshot = next
        invalidate()
    }

    private fun renderLayout(next: LayoutSnapshot) {
        val previous = layoutSnapshot
        layoutSnapshot = next
        if (next.prefs == previous.prefs && specOf(next) == specOf(previous)) {
            invalidate()
            return
        }
        cancelAll()
        cached = null
        requestLayout()
        invalidate()
        access.invalidateRoot()
    }

    private fun specOf(snapshot: LayoutSnapshot): LayoutSpec =
        snapshot.layouts.getValue(fixedLayoutId ?: snapshot.activeId)

    private fun spec(): LayoutSpec = specOf(layoutSnapshot)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val rows = spec().rows.size
        val height = rows * layoutSnapshot.rowPx(rows, density, resources.displayMetrics.heightPixels)
        setMeasuredDimension(MeasureSpec.getSize(widthSpec), height.toInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!editing) excludeEdgeGestures(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cached = null
        access.invalidateRoot()
    }

    private fun placement(): Placement = cached ?: compute().also { cached = it }

    private fun compute(): Placement {
        val prefs = layoutSnapshot.prefs
        val mode = if (editing) PanelMode.NORMAL else prefs.mode
        val input = GeometryInput(width.toFloat(), height.toFloat(), prefs.gapDp() * density, mode, prefs.oneHandPercent, prefs.splitGapPercent)
        return KeyboardGeometry.place(spec(), input)
    }

    override fun onDraw(canvas: Canvas) {
        pressedKeys.clear()
        for (i in 0 until touches.size()) pressedKeys += touches.valueAt(i).placed
        painter.draw(canvas, placement(), themeSnapshot.palette, KeyStyle.of(themeSnapshot.choice.theme), layoutSnapshot, pressedKeys, dragger.state)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        if (editing) dragger.touch(event) else typeTouch(event)

    override fun dispatchHoverEvent(event: MotionEvent): Boolean =
        (!editing && access.dispatchHoverEvent(event)) || super.dispatchHoverEvent(event)

    override fun performClick(): Boolean = super.performClick()

    private fun typeTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = event.actionIndex
                down(event.getPointerId(i), event.getX(i), event.getY(i))
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) move(event.getPointerId(i), event.getX(i), event.getY(i))
            }
            MotionEvent.ACTION_POINTER_UP -> release(event.getPointerId(event.actionIndex), true)
            MotionEvent.ACTION_UP -> {
                release(event.getPointerId(event.actionIndex), true)
                cancelAll()
            }
            MotionEvent.ACTION_CANCEL -> cancelAll()
        }
        return true
    }

    private fun down(id: Int, x: Float, y: Float) {
        release(id, false)
        val hit = placement().hit(x, y) ?: return
        val placed = onResolve(placement(), hit, x, y)
        val touch = KeyTouch(id, placed, x, y, ::startLongPress, ::repeatDelete)
        touches.put(id, touch)
        val key = placed.key
        feedback.down(key.action, layoutSnapshot.prefs.feedback)
        if (showPreviews && layoutSnapshot.prefs.feedback.preview && key.action == KeyAction.COMMIT) popups.showPreview(layoutSnapshot.shown(key), placed.box, themeSnapshot.palette)
        if (key.firesOnDown()) {
            touch.handled = true
            fire(touch)
        }
        when {
            key.action == KeyAction.DELETE -> timer.postDelayed(touch.repeat, KeyTiming.REPEAT_DELAY_MS)
            key.hasLongPress() -> timer.postDelayed(touch.longPress, KeyTiming.LONG_PRESS_MS)
        }
        invalidate()
    }

    private fun move(id: Int, x: Float, y: Float) {
        val touch = touches.get(id) ?: return
        touch.lastX = x
        when {
            touch.alts.isNotEmpty() -> {
                touch.altIndex = popups.indexAt(x)
                popups.selectAlt(touch.altIndex)
            }
            touch.placed.key.action == KeyAction.SPACE -> swipeSpace(touch, x)
            touch.placed.key.action == KeyAction.DELETE -> swipeDelete(touch, x)
            hypot(x - touch.downX, y - touch.placed.cell.centerY) > touch.placed.cell.height * KeyTiming.CANCEL_LONG_RATIO ->
                timer.removeCallbacks(touch.longPress)
        }
    }

    private fun swipeSpace(touch: KeyTouch, x: Float) {
        if (!touch.swiping) {
            if (abs(x - touch.downX) <= slop * 2) return
            touch.swiping = true
            touch.anchorX = x
            popups.dismissKeys()
        }
        val step = KeyTiming.SWIPE_STEP_DP * density
        while (abs(x - touch.anchorX) >= step) {
            val direction = if (x > touch.anchorX) 1 else -1
            touch.anchorX += direction * step
            feedback.tick(layoutSnapshot.prefs.feedback)
            onCursorMove(direction)
        }
    }

    private fun swipeDelete(touch: KeyTouch, x: Float) {
        val step = KeyTiming.SWIPE_DELETE_DP * density
        while (touch.anchorX - x >= step) {
            if (!touch.swiping) {
                touch.swiping = true
                timer.removeCallbacks(touch.repeat)
            }
            touch.anchorX -= step
            feedback.tick(layoutSnapshot.prefs.feedback)
            fireUntouched(wordDelete)
        }
    }

    private fun release(id: Int, commit: Boolean) {
        val touch = touches.get(id) ?: return
        touches.remove(id)
        timer.removeCallbacks(touch.longPress)
        timer.removeCallbacks(touch.repeat)
        popups.hidePreview()
        if (touch.alts.isNotEmpty()) popups.hideAlts()
        if (commit) {
            when {
                touch.alts.isNotEmpty() -> touch.alts.getOrNull(touch.altIndex)?.let { fireAlt(touch, it) }
                !touch.handled && !touch.swiping -> fire(touch)
            }
        }
        invalidate()
    }

    private fun cancelAll() {
        for (i in touches.size() - 1 downTo 0) release(touches.keyAt(i), false)
        popups.dismissKeys()
    }

    private fun fire(touch: KeyTouch) {
        if (touch.placed.key.action == KeyAction.COMMIT) touch.typed = KeyLabels.output(layoutSnapshot.shown(touch.placed.key))
        onKeyTouch(touch.dx, touch.dy)
        onKey(touch.placed.key)
    }

    private fun fireAlt(touch: KeyTouch, text: String) {
        if (touch.typed.isNotEmpty()) fireUntouched(KeySpec(KeyTiming.REPLACE_ID, touch.typed, KeyAction.DELETE_WORD, 1f, false))
        fireUntouched(KeySpec(KeyTiming.ALT_ID, text, KeyAction.COMMIT, 1f, false))
    }

    private fun pressForAccessibility(placed: PlacedKey) = fireUntouched(placed.key)

    private fun fireUntouched(key: KeySpec) {
        onKeyTouch(Float.NaN, Float.NaN)
        onKey(key)
    }

    private fun startLongPress(touch: KeyTouch) {
        if (touches.get(touch.id) !== touch) return
        val key = touch.placed.key
        when {
            key.action == KeyAction.LANGUAGE -> if (!touch.handled && onKeyLongPress(key)) {
                touch.handled = true
                feedback.longPress(layoutSnapshot.prefs.feedback)
                touches.remove(touch.id)
                invalidate()
            }
            key.action == KeyAction.COMMIT && key.alts.isNotEmpty() -> {
                popups.hidePreview()
                val list = layoutSnapshot.alts(key)
                touch.alts = list
                touch.altIndex = popups.showAlts(list, touch.placed.box, touch.lastX, themeSnapshot.palette)
                feedback.longPress(layoutSnapshot.prefs.feedback)
            }
        }
    }

    private fun repeatDelete(touch: KeyTouch) {
        if (touches.get(touch.id) !== touch) return
        val words = ++touch.repeats > KeyTiming.WORD_AFTER
        if (words) fireUntouched(wordDelete) else fire(touch)
        val fast = touch.repeats > KeyTiming.FAST_AFTER
        timer.postDelayed(touch.repeat, if (words) KeyTiming.REPEAT_WORD_MS else if (fast) KeyTiming.REPEAT_FAST_MS else KeyTiming.REPEAT_MS)
    }
}
