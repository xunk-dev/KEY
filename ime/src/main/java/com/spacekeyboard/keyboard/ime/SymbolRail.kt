package com.spacekeyboard.keyboard.ime

import android.animation.ValueAnimator
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.layout.RailSymbols
import kotlin.math.roundToInt

internal class SymbolRail(private val view: View) {
    private val specs: List<KeySpec> = RailSymbols.items.mapIndexed { i, item ->
        KeySpec("rail_sym_$i", item.label, KeyAction.COMMIT, 1f, false, item.alts)
    }
    private var slots: List<PlacedKey> = emptyList()
    private var offset = 0f
    private var animator: ValueAnimator? = null
    private var owner = NONE
    private var lastY = 0f
    private var lastTime = 0L
    private var velocity = 0f

    var scrolling = false
        private set

    val active: Boolean get() = slots.size >= 2

    fun bind(placement: Placement, pageId: String) {
        slots = if (pageId == LayoutCatalog.numbers.id) {
            placement.keys.filter { it.key.action == KeyAction.RAIL }.sortedBy { it.cell.top }
        } else {
            emptyList()
        }
        offset = offset.coerceIn(0f, maxOffset())
    }

    fun clip(): RectF? {
        if (!active) return null
        val first = slots.first().cell
        val last = slots.last().cell
        return RectF(first.left, first.top, first.right, last.bottom)
    }

    fun entries(): List<PlacedKey> {
        if (!active) return emptyList()
        val first = slots.first()
        val pitch = step()
        val area = clip() ?: return emptyList()
        return specs.mapIndexedNotNull { i, spec ->
            val shift = i * pitch - offset
            val box = Box(first.box.left, first.box.top + shift, first.box.right, first.box.bottom + shift)
            if (box.bottom <= area.top || box.top >= area.bottom) return@mapIndexedNotNull null
            val cell = Box(first.cell.left, first.cell.top + shift, first.cell.right, first.cell.bottom + shift)
            PlacedKey(spec, first.slot, cell, box)
        }
    }

    fun itemAt(x: Float, y: Float): PlacedKey? {
        val area = clip() ?: return null
        if (!area.contains(x, y)) return null
        return entries().firstOrNull { it.cell.contains(x, y) }
    }

    fun thumb(width: Float, inset: Float): RectF? {
        val area = clip() ?: return null
        val max = maxOffset()
        if (max <= 0f) return null
        val track = area.height() - 2f * inset
        val share = slots.size.toFloat() / specs.size
        val length = track * share
        val top = area.top + inset + (track - length) * (offset / max)
        return RectF(area.left + inset, top, area.left + inset + width, top + length)
    }

    fun begin(id: Int, y: Float) {
        animator?.cancel()
        owner = id
        scrolling = false
        lastY = y
        lastTime = SystemClock.uptimeMillis()
        velocity = 0f
    }

    fun owns(id: Int): Boolean = owner == id

    fun startScroll(y: Float) {
        scrolling = true
        lastY = y
        lastTime = SystemClock.uptimeMillis()
    }

    fun scrollTo(y: Float) {
        val now = SystemClock.uptimeMillis()
        val dt = (now - lastTime).coerceAtLeast(1L)
        val moved = lastY - y
        velocity = velocity * 0.5f + (moved / dt * 1000f) * 0.5f
        offset = (offset + moved).coerceIn(0f, maxOffset())
        lastY = y
        lastTime = now
        view.invalidate()
    }

    fun finish(id: Int) {
        if (owner != id) return
        owner = NONE
        val pitch = step()
        if (pitch <= 0f) return
        if (!scrolling) {
            // A tap that stopped a running settle must not leave the rows cut in the middle.
            snap(pitch)
            return
        }
        scrolling = false
        // A finger that rested before lifting must not fling.
        if (SystemClock.uptimeMillis() - lastTime > REST_MS) velocity = 0f
        val projected = offset + velocity * FLING_SECONDS
        val target = ((projected / pitch).roundToInt() * pitch).coerceIn(0f, maxOffset())
        settle(target)
    }

    private fun snap(pitch: Float) {
        val target = ((offset / pitch).roundToInt() * pitch).coerceIn(0f, maxOffset())
        if (target != offset) settle(target)
    }

    fun stop() {
        animator?.cancel()
        owner = NONE
        scrolling = false
    }

    private fun settle(target: Float) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(offset, target).apply {
            duration = SETTLE_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                offset = it.animatedValue as Float
                view.invalidate()
            }
            start()
        }
    }

    private fun step(): Float = if (active) slots[1].cell.top - slots[0].cell.top else 0f

    private fun maxOffset(): Float = if (active) maxOf(0f, (specs.size - slots.size) * step()) else 0f

    private companion object {
        const val NONE = -1
        const val FLING_SECONDS = 0.2f
        const val SETTLE_MS = 240L
        const val REST_MS = 90L
    }
}
