package com.spacekeyboard.keyboard.ime

import android.os.SystemClock
import android.view.MotionEvent
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.suggest.GlideKey
import kotlin.math.abs

internal fun KeyboardView.typeTouch(event: MotionEvent): Boolean {
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

internal fun KeyTouch.picked(): KeySpec {
    val key = placed.key
    if (!key.pair || key.letters.size != 2) return key
    return key.copy(label = key.letters[if (dx < 0f) 0 else 1], pair = false)
}

internal fun KeyTouch.altPick(): String? {
    val pick = alts.getOrNull(altIndex) ?: return null
    val untouchedDigit = altIndex == altStart && pick.isNotEmpty() && pick.all { it.isDigit() }
    return if (untouchedDigit) null else pick
}

internal fun KeyTouch.repeatDelayMs(): Long = when {
    repeats > KeyTiming.WORD_AFTER -> KeyTiming.REPEAT_WORD_MS
    repeats > KeyTiming.FAST_AFTER -> KeyTiming.REPEAT_FAST_MS
    else -> KeyTiming.REPEAT_MS
}

private val cursorLeft = KeySpec("cursor_left", "", KeyAction.LEFT, 1f, false)
private val cursorRight = KeySpec("cursor_right", "", KeyAction.RIGHT, 1f, false)

private fun KeyboardView.startCursor(touch: KeyTouch, x: Float) {
    touch.cursor = true
    touch.swiping = true
    touch.anchorX = x
    timer.removeCallbacks(touch.longPress)
    popups.dismissKeys()
    feedback.tick(layoutSnapshot.prefs.feedback)
}

private fun KeyboardView.dragCursor(touch: KeyTouch, x: Float) {
    val step = KeyTiming.CURSOR_STEP_DP * density
    while (abs(x - touch.anchorX) >= step) {
        val right = x > touch.anchorX
        touch.anchorX += if (right) step else -step
        feedback.tick(layoutSnapshot.prefs.feedback)
        fireUntouched(if (right) cursorRight else cursorLeft)
    }
}

internal fun KeyboardView.swipeSpace(touch: KeyTouch, x: Float) {
    if (touch.cursor) return dragCursor(touch, x)
    if (touch.swiping) return
    if (touch.movedAt == 0L && abs(x - touch.downX) > slop) {
        touch.movedAt = SystemClock.uptimeMillis()
        if (touch.movedAt - touch.downAt >= KeyTiming.CURSOR_HOLD_MS) return startCursor(touch, x)
    }
    if (abs(x - touch.downX) < KeyTiming.SWIPE_LANGUAGE_DP * density) return
    touch.swiping = true
    popups.dismissKeys()
    feedback.tick(layoutSnapshot.prefs.feedback)
    fireUntouched(languageKey)
}

internal fun KeyboardView.swipeDelete(touch: KeyTouch, x: Float) {
    val step = KeyTiming.SWIPE_DELETE_DP * density
    val dir = if (LayoutCatalog.isRtl(layoutSnapshot.activeId)) -1f else 1f
    while (dir * (touch.anchorX - x) >= step) {
        if (!touch.swiping) {
            touch.swiping = true
            timer.removeCallbacks(touch.repeat)
        }
        touch.anchorX -= dir * step
        feedback.tick(layoutSnapshot.prefs.feedback)
        fireUntouched(wordDelete)
    }
}

internal fun KeyboardView.railMove(touch: KeyTouch, x: Float, y: Float): Boolean {
    if (!rail.scrolling) {
        val dy = y - touch.downY
        if (abs(dy) <= slop || abs(dy) < abs(x - touch.downX)) return false
        rail.startScroll(y)
        touch.handled = true
        touch.swiping = true
        timer.removeCallbacks(touch.longPress)
        popups.hidePreview()
    }
    rail.scrollTo(y)
    return true
}

internal fun KeyboardView.digitSwipeReady(touch: KeyTouch, x: Float, y: Float): Boolean {
    val across = abs(x - touch.downX)
    val reach = maxOf(touch.placed.cell.width * KeyTiming.SWIPE_DIGITS_RATIO, KeyTiming.SWIPE_DIGITS_MIN_DP * density)
    val flat = abs(y - touch.downY) <= across * KeyTiming.SWIPE_DIGITS_FLATNESS
    val held = SystemClock.uptimeMillis() - touch.downAt >= KeyTiming.SWIPE_DIGITS_MIN_MS
    return across >= reach && flat && held && canSwapDigits()
}

internal fun KeyboardView.trackDigitSwipe(touch: KeyTouch, x: Float, y: Float): Boolean {
    val ready = digitSwipeReady(touch, x, y)
    if (ready && !touch.armed) {
        touch.armed = true
        touch.swiping = true
        timer.removeCallbacks(touch.longPress)
        popups.dismissKeys()
        feedback.tick(layoutSnapshot.prefs.feedback)
    } else if (!ready && touch.armed) {
        touch.armed = false
    }
    return touch.swiping
}

internal fun KeyboardView.glideKeys(placement: Placement): List<GlideKey> =
    KeyNeighborsBuilder.points(placement, layoutSnapshot::shown).map { GlideKey(it.ch, it.x, it.y, it.width, it.height) }
