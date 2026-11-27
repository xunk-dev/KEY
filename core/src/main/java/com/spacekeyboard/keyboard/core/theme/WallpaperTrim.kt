package com.spacekeyboard.keyboard.core.theme

import java.util.Locale

data class TrimRange(val startMs: Long, val endMs: Long) {
    val lengthMs: Long get() = endMs - startMs
}

object TrimMath {
    const val MIN_CLIP_MS = 1_000L
    const val MAX_CLIP_MS = 15_000L

    fun needsTrim(durationMs: Long): Boolean = durationMs > MAX_CLIP_MS

    fun initial(durationMs: Long): TrimRange = TrimRange(0L, minOf(durationMs, MAX_CLIP_MS).coerceAtLeast(0L))

    fun moveStart(range: TrimRange, target: Long, durationMs: Long): TrimRange {
        val floor = maxOf(0L, range.endMs - MAX_CLIP_MS)
        val ceiling = maxOf(floor, minOf(range.endMs - minimum(durationMs), durationMs))
        return range.copy(startMs = target.coerceIn(floor, ceiling))
    }

    fun moveEnd(range: TrimRange, target: Long, durationMs: Long): TrimRange {
        val floor = minOf(durationMs, range.startMs + minimum(durationMs))
        val ceiling = maxOf(floor, minOf(durationMs, range.startMs + MAX_CLIP_MS))
        return range.copy(endMs = target.coerceIn(floor, ceiling))
    }

    fun shift(range: TrimRange, delta: Long, durationMs: Long): TrimRange {
        val length = range.lengthMs
        val start = (range.startMs + delta).coerceIn(0L, maxOf(0L, durationMs - length))
        return TrimRange(start, start + length)
    }

    fun fraction(positionMs: Long, durationMs: Long): Float =
        if (durationMs <= 0L) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)

    fun position(fraction: Float, durationMs: Long): Long = (fraction.coerceIn(0f, 1f) * durationMs).toLong()

    fun label(ms: Long): String {
        val total = (ms.coerceAtLeast(0L) + 500L) / 1000L
        return String.format(Locale.ROOT, "%d:%02d", total / 60L, total % 60L)
    }

    private fun minimum(durationMs: Long): Long = minOf(MIN_CLIP_MS, durationMs)
}
