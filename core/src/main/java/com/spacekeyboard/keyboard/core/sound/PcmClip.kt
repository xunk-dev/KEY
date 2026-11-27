package com.spacekeyboard.keyboard.core.sound

import kotlin.math.abs

object PcmClip {
    const val FADE_IN_MS = 2
    const val FADE_OUT_MS = 12
    const val TARGET_PEAK = 0.9f
    const val MAX_GAIN = 6f
    private const val FULL_SCALE = 32767f

    fun peak(samples: ShortArray): Int {
        var top = 0
        for (value in samples) top = maxOf(top, abs(value.toInt()))
        return top
    }

    fun level(samples: ShortArray): Float = peak(samples) / FULL_SCALE

    fun finish(samples: ShortArray, sampleRate: Int): ShortArray {
        val top = peak(samples)
        if (top == 0 || samples.isEmpty()) return samples.copyOf()
        val gain = minOf(MAX_GAIN, TARGET_PEAK * FULL_SCALE / top)
        val half = samples.size / 2
        val fadeIn = (sampleRate * FADE_IN_MS / 1000).coerceAtMost(half)
        val fadeOut = (sampleRate * FADE_OUT_MS / 1000).coerceAtMost(half)
        return ShortArray(samples.size) { i ->
            val edge = envelope(i, samples.size, fadeIn, fadeOut)
            (samples[i] * gain * edge).toInt().coerceIn(-32768, 32767).toShort()
        }
    }

    private fun envelope(index: Int, size: Int, fadeIn: Int, fadeOut: Int): Float {
        val tail = size - 1 - index
        val rise = if (index < fadeIn) (index + 1f) / fadeIn else 1f
        val fall = if (tail < fadeOut) (tail + 1f) / fadeOut else 1f
        return minOf(rise, fall)
    }
}
