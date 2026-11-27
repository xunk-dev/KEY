package com.spacekeyboard.keyboard.core.sound

import java.util.Locale

data class ClipRange(val startMs: Long, val endMs: Long) {
    val lengthMs: Long get() = endMs - startMs
}

object ClipMath {
    const val MIN_MS = 40L
    const val MAX_MS = 3_000L
    const val DEFAULT_MS = 500L
    const val PRE_ROLL_MS = 20L
    private const val ONSET_RATIO = 0.25f

    fun initial(durationMs: Long, peaks: FloatArray): ClipRange {
        val length = minOf(DEFAULT_MS, durationMs).coerceAtLeast(0L)
        val onset = onsetMs(peaks)
        val start = (onset - PRE_ROLL_MS).coerceIn(0L, maxOf(0L, durationMs - length))
        return ClipRange(start, start + length)
    }

    fun onsetMs(peaks: FloatArray): Long {
        val loudest = peaks.maxOrNull() ?: return 0L
        if (loudest <= 0f) return 0L
        val index = peaks.indexOfFirst { it >= loudest * ONSET_RATIO }
        return maxOf(0, index) * PeakBuilder.STEP_MS
    }

    fun moveStart(range: ClipRange, target: Long, durationMs: Long): ClipRange {
        val floor = maxOf(0L, range.endMs - MAX_MS)
        val ceiling = maxOf(floor, minOf(range.endMs - minimum(durationMs), durationMs))
        return range.copy(startMs = target.coerceIn(floor, ceiling))
    }

    fun moveEnd(range: ClipRange, target: Long, durationMs: Long): ClipRange {
        val floor = minOf(durationMs, range.startMs + minimum(durationMs))
        val ceiling = maxOf(floor, minOf(durationMs, range.startMs + MAX_MS))
        return range.copy(endMs = target.coerceIn(floor, ceiling))
    }

    fun slide(range: ClipRange, delta: Long, durationMs: Long): ClipRange {
        val start = (range.startMs + delta).coerceIn(0L, maxOf(0L, durationMs - range.lengthMs))
        return ClipRange(start, start + range.lengthMs)
    }

    fun viewAround(range: ClipRange, spanMs: Long, durationMs: Long): ClipRange {
        val span = minOf(spanMs, durationMs).coerceAtLeast(1L)
        val centre = (range.startMs + range.endMs) / 2
        val start = (centre - span / 2).coerceIn(0L, maxOf(0L, durationMs - span))
        return ClipRange(start, start + span)
    }

    fun pan(view: ClipRange, delta: Long, durationMs: Long): ClipRange {
        val start = (view.startMs + delta).coerceIn(0L, maxOf(0L, durationMs - view.lengthMs))
        return ClipRange(start, start + view.lengthMs)
    }

    fun reveal(view: ClipRange, range: ClipRange, durationMs: Long): ClipRange = when {
        range.startMs < view.startMs -> pan(view, range.startMs - view.startMs, durationMs)
        range.endMs > view.endMs -> pan(view, range.endMs - view.endMs, durationMs)
        else -> view
    }

    fun seconds(ms: Long): String = String.format(Locale.ROOT, "%.2f", ms.coerceAtLeast(0L) / 1000.0)

    private fun minimum(durationMs: Long): Long = minOf(MIN_MS, durationMs)
}
