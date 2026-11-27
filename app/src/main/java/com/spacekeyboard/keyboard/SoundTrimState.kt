package com.spacekeyboard.keyboard

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.sound.ClipMath
import com.spacekeyboard.keyboard.core.sound.ClipRange
import com.spacekeyboard.keyboard.core.sound.PeakBuilder
import com.spacekeyboard.keyboard.core.sound.StagedSound
import kotlin.math.abs
import kotlin.math.sqrt

internal enum class Zoom(val labelRes: Int, private val spanMs: Long?) {
    SHORT(R.string.sound_zoom_3, 3_000L),
    MEDIUM(R.string.sound_zoom_6, 6_000L),
    LONG(R.string.sound_zoom_15, 15_000L),
    ALL(R.string.sound_zoom_all, null);

    fun span(durationMs: Long): Long = spanMs ?: durationMs

    companion object {
        fun default(durationMs: Long): Zoom = if (durationMs <= MEDIUM.span(durationMs)) ALL else MEDIUM
    }
}

internal enum class DragMode { START, END, SLIDE, PAN }

@Stable
internal class TrimState(val sound: StagedSound) {
    val durationMs: Long = sound.durationMs

    var range by mutableStateOf(ClipMath.initial(sound.durationMs, sound.peaks))
        private set
    var zoom by mutableStateOf(Zoom.default(sound.durationMs))
        private set
    var view by mutableStateOf(ClipRange(0L, 1L))
        private set

    init {
        view = ClipMath.viewAround(range, zoom.span(durationMs), durationMs)
    }

    fun select(next: Zoom) {
        zoom = next
        view = ClipMath.viewAround(range, next.span(durationMs), durationMs)
    }

    fun restore(saved: ClipRange, next: Zoom) {
        val start = saved.startMs.coerceIn(0L, durationMs)
        range = ClipRange(start, saved.endMs.coerceIn(start, durationMs))
        select(next)
    }

    fun xOf(ms: Long, widthPx: Float): Float = (ms - view.startMs).toFloat() / view.lengthMs * widthPx

    fun hit(x: Float, widthPx: Float, grabPx: Float): DragMode {
        val startX = xOf(range.startMs, widthPx)
        val endX = xOf(range.endMs, widthPx)
        val nearStart = abs(x - startX) <= grabPx
        val nearEnd = abs(x - endX) <= grabPx
        return when {
            nearStart && (!nearEnd || abs(x - startX) <= abs(x - endX)) -> DragMode.START
            nearEnd -> DragMode.END
            x in startX..endX -> DragMode.SLIDE
            else -> DragMode.PAN
        }
    }

    fun drag(mode: DragMode, viewFraction: Float) {
        val delta = (viewFraction * view.lengthMs).toLong()
        when (mode) {
            DragMode.START -> move(ClipMath.moveStart(range, range.startMs + delta, durationMs))
            DragMode.END -> move(ClipMath.moveEnd(range, range.endMs + delta, durationMs))
            DragMode.SLIDE -> move(ClipMath.slide(range, delta, durationMs))
            DragMode.PAN -> view = ClipMath.pan(view, -delta, durationMs)
        }
    }

    fun level(fromMs: Long, toMs: Long): Float {
        val peaks = sound.peaks
        val first = (fromMs / PeakBuilder.STEP_MS).toInt().coerceIn(0, peaks.lastIndex)
        val last = (toMs / PeakBuilder.STEP_MS).toInt().coerceIn(first, peaks.lastIndex)
        var top = 0f
        for (i in first..last) top = maxOf(top, peaks[i])
        return sqrt(top)
    }

    private fun move(next: ClipRange) {
        range = next
        view = ClipMath.reveal(view, next, durationMs)
    }

    companion object {
        fun saver(sound: StagedSound): Saver<TrimState, Any> = listSaver<TrimState, Long>(
            save = { listOf(it.range.startMs, it.range.endMs, it.zoom.ordinal.toLong()) },
            restore = { saved ->
                val zoom = Zoom.entries[saved[2].toInt().coerceIn(0, Zoom.entries.lastIndex)]
                TrimState(sound).also { it.restore(ClipRange(saved[0], saved[1]), zoom) }
            },
        )
    }
}
