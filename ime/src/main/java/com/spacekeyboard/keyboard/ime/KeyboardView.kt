package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import com.spacekeyboard.keyboard.autofix.KeyNeighbors
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.layout.SlotRef
import com.spacekeyboard.keyboard.core.suggest.GlideKey
import com.spacekeyboard.keyboard.core.suggest.GlidePoint
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.ThemeSnapshot
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
    private val onNeighbors: (KeyNeighbors) -> Unit = {},
    internal val canSwapDigits: () -> Boolean = { false },
    internal val glideOn: () -> Boolean = { false },
    internal val onGlide: (List<GlidePoint>, List<GlideKey>) -> Unit = { _, _ -> },
    private val onSwapDigits: () -> Unit = {},
    private val onDragging: (Boolean) -> Unit = {},
    private val onKeyLongPress: (KeySpec) -> Boolean = { false },
    private val onResolve: (Placement, PlacedKey, Float, Float) -> PlacedKey = { _, hit, _, _ -> hit },
) : View(context) {
    internal val density = resources.displayMetrics.density
    internal val slop = ViewConfiguration.get(context).scaledTouchSlop
    private val painter = KeyPainter(context)
    internal val popups = KeyPopups(this)
    internal val feedback = KeyFeedback(this)
    internal val timer = Handler(Looper.getMainLooper())
    private val touches = SparseArray<KeyTouch>()
    private val pressedKeys = ArrayList<PlacedKey>()
    private var themeSnapshot = theme.snapshot
    internal var layoutSnapshot = layouts.snapshot
    private var cached: Placement? = null
    internal val rail = SymbolRail(this)
    internal val glide = GlideTrail(this)
    private val dragger = LayoutDrag(this, ::placement, slop, onSwap, onDragging)
    private val subscriptions = ArrayList<AutoCloseable>()
    internal val wordDelete = KeySpec(KeyTiming.WORD_ID, "", KeyAction.DELETE_WORD, 1f, false)
    internal val languageKey = KeySpec(KeyTiming.LANGUAGE_ID, "", KeyAction.LANGUAGE, 1f, false)
    var showPreviews = true

    var topRowClipPx: Int = 0
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }
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
        rail.stop()
        dragger.end(false)
        cancelAll()
        popups.dismissAll()
        subscriptions.forEach { it.close() }
        subscriptions.clear()
        feedback.release()
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
        if (!onlyDigitsChanged(previous, next, ::specOf)) requestLayout()
        invalidate()
        access.invalidateRoot()
    }

    private fun specOf(snapshot: LayoutSnapshot): LayoutSpec =
        snapshot.layouts.getValue(fixedLayoutId ?: snapshot.activeId)

    private fun spec(): LayoutSpec = specOf(layoutSnapshot)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val rows = layoutSnapshot.stableRows
        val height = rows * layoutSnapshot.rowPx(rows, density, resources.displayMetrics.heightPixels) + layoutSnapshot.prefs.bottomSafePx(density)
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

    private fun compute(): Placement =
        computePlacement(spec(), layoutSnapshot.prefs, density, width.toFloat(), height.toFloat(), editing)
            .also { onNeighbors(KeyNeighborsBuilder.from(it, layoutSnapshot::shown)); glide.keys = glideKeys(it) }

    override fun onDraw(canvas: Canvas) {
        pressedKeys.clear()
        for (i in 0 until touches.size()) {
            val touch = touches.valueAt(i)
            if (rail.owns(touch.id) && rail.scrolling) continue
            pressedKeys += touch.placed
        }
        val saved = canvas.save()
        if (topRowClipPx > 0) canvas.clipRect(0f, topRowClipPx.toFloat(), width.toFloat(), height.toFloat())
        rail.bind(placement(), spec().id)
        painter.draw(canvas, placement(), themeSnapshot.palette, KeyStyle.of(themeSnapshot.choice.theme), layoutSnapshot, pressedKeys, dragger.state, rail)
        glide.draw(canvas, themeSnapshot.palette)
        canvas.restoreToCount(saved)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        if (editing) dragger.touch(event) else typeTouch(event)

    override fun dispatchHoverEvent(event: MotionEvent): Boolean =
        (!editing && access.dispatchHoverEvent(event)) || super.dispatchHoverEvent(event)

    override fun performClick(): Boolean = super.performClick()

    internal fun down(id: Int, x: Float, y: Float) {
        if (topRowClipPx > 0 && y < topRowClipPx) return
        release(id, false)
        rail.bind(placement(), spec().id)
        val hit = placement().hit(x, y) ?: return
        val railItem = if (hit.key.action == KeyAction.RAIL) (rail.itemAt(x, y) ?: return) else null
        val placed = railItem ?: onResolve(placement(), hit, x, y)
        if (railItem != null) rail.begin(id, y)
        val touch = KeyTouch(id, placed, x, y, ::startLongPress, ::repeatDelete)
        touches.put(id, touch)
        val key = placed.key
        feedback.down(key.action, layoutSnapshot.prefs.feedback)
        if (showPreviews && layoutSnapshot.prefs.feedback.preview && key.action == KeyAction.COMMIT) popups.showPreview(layoutSnapshot.shown(touch.picked()), placed.box, themeSnapshot.palette)
        touch.deferred = key.isNumberRowKey() && canSwapDigits()
        touch.glideable = glide.arms(key, layoutSnapshot.shown(touch.picked()))
        val holdsNewline = key.action == KeyAction.ENTER && layoutSnapshot.enterKind != EnterKind.ENTER
        if (key.firesOnDown() && !touch.deferred && !touch.glideable && !holdsNewline && railItem == null) {
            touch.handled = true
            fire(touch)
        }
        when {
            key.action == KeyAction.DELETE -> timer.postDelayed(touch.repeat, KeyTiming.REPEAT_DELAY_MS)
            key.hasLongPress() || holdsNewline -> timer.postDelayed(touch.longPress, holdDelayFor(key) { painter.micAt(x, y) })
        }
        invalidate()
    }

    internal fun move(id: Int, x: Float, y: Float) {
        val touch = touches.get(id) ?: return
        touch.lastX = x
        touch.lastY = y
        if (glide.track(touch, x, y)) return
        if (rail.owns(id) && touch.alts.isEmpty() && railMove(touch, x, y)) return
        if (touch.deferred && touch.alts.isEmpty() && trackDigitSwipe(touch, x, y)) return
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

    internal fun release(id: Int, commit: Boolean) {
        val touch = touches.get(id) ?: return
        touches.remove(id)
        rail.finish(id)
        timer.removeCallbacks(touch.longPress)
        timer.removeCallbacks(touch.repeat)
        popups.hidePreview()
        glide.end(touch, commit)
        if (touch.alts.isNotEmpty()) popups.hideAlts()
        if (commit) {
            when {
                touch.armed && digitSwipeReady(touch, touch.lastX, touch.lastY) -> {
                    feedback.tick(layoutSnapshot.prefs.feedback)
                    onSwapDigits()
                }
                touch.alts.isNotEmpty() -> {
                    val pick = touch.altPick()
                    if (pick != null) fireAlt(touch, pick) else if (!touch.handled) fire(touch)
                }
                !touch.handled && !touch.swiping && !touch.slidOffSpace(KeyTiming.SPACE_SLIDE_SLOP_DP * density) -> fire(touch)
            }
        }
        invalidate()
    }

    internal fun cancelAll() {
        for (i in touches.size() - 1 downTo 0) release(touches.keyAt(i), false)
        popups.dismissKeys()
    }

    private fun fire(touch: KeyTouch) {
        val key = touch.picked()
        if (key.action == KeyAction.COMMIT) touch.typed = KeyLabels.output(layoutSnapshot.shown(key))
        onKeyTouch(touch.dx, touch.dy)
        onKey(key)
    }

    private fun fireAlt(touch: KeyTouch, text: String) {
        if (touch.typed.isNotEmpty()) fireUntouched(KeySpec(KeyTiming.REPLACE_ID, touch.typed, KeyAction.DELETE_WORD, 1f, false))
        fireUntouched(KeySpec(KeyTiming.ALT_ID, text, KeyAction.COMMIT, 1f, false))
    }

    private fun pressForAccessibility(placed: PlacedKey) = fireUntouched(placed.key)

    internal fun fireUntouched(key: KeySpec) {
        onKeyTouch(Float.NaN, Float.NaN)
        onKey(key)
    }

    private fun startLongPress(touch: KeyTouch) {
        if (touches.get(touch.id) !== touch || touch.swiping) return
        val key = touch.placed.key
        when {
            key.action == KeyAction.LANGUAGE || key.action == KeyAction.SPACE || key.action == KeyAction.ENTER -> if (!touch.handled && onKeyLongPress(key)) {
                touch.handled = true
                feedback.longPress(layoutSnapshot.prefs.feedback)
                touches.remove(touch.id)
                invalidate()
            }
            key.action == KeyAction.COMMIT && key.alts.isNotEmpty() -> {
                val list = layoutSnapshot.alts(key)
                if (list.isEmpty()) return
                popups.hidePreview()
                touch.alts = list
                touch.altIndex = popups.showAlts(list, touch.placed.box, touch.lastX, themeSnapshot.palette)
                touch.altStart = touch.altIndex
                feedback.longPress(layoutSnapshot.prefs.feedback)
            }
        }
    }

    private fun repeatDelete(touch: KeyTouch) {
        if (touches.get(touch.id) !== touch) return
        touch.repeats++
        if (touch.repeats > KeyTiming.WORD_AFTER) fireUntouched(wordDelete) else fire(touch)
        timer.postDelayed(touch.repeat, touch.repeatDelayMs())
    }
}
