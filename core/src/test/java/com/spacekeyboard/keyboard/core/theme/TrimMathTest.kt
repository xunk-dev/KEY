package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrimMathTest {
    private val duration = 60_000L

    @Test
    fun clipsUpToTheLimitNeedTrimmingAndShortOnesDoNot() {
        assertFalse(TrimMath.needsTrim(TrimMath.MAX_CLIP_MS))
        assertTrue(TrimMath.needsTrim(TrimMath.MAX_CLIP_MS + 1))
    }

    @Test
    fun initialRangeStartsAtZeroAndIsCapped() {
        assertEquals(TrimRange(0L, TrimMath.MAX_CLIP_MS), TrimMath.initial(duration))
        assertEquals(TrimRange(0L, 4_000L), TrimMath.initial(4_000L))
    }

    @Test
    fun startCannotPassTheEndMinusTheMinimumClip() {
        val range = TrimRange(0L, 10_000L)
        assertEquals(10_000L - TrimMath.MIN_CLIP_MS, TrimMath.moveStart(range, 50_000L, duration).startMs)
    }

    @Test
    fun startCannotMakeTheClipLongerThanTheLimit() {
        val range = TrimRange(20_000L, 30_000L)
        assertEquals(30_000L - TrimMath.MAX_CLIP_MS, TrimMath.moveStart(range, 0L, duration).startMs)
    }

    @Test
    fun endIsBoundByDurationMinimumAndLimit() {
        val range = TrimRange(10_000L, 20_000L)
        assertEquals(10_000L + TrimMath.MAX_CLIP_MS, TrimMath.moveEnd(range, 59_000L, duration).endMs)
        assertEquals(10_000L + TrimMath.MIN_CLIP_MS, TrimMath.moveEnd(range, 0L, duration).endMs)
        val tail = TrimRange(50_000L, 58_000L)
        assertEquals(duration, TrimMath.moveEnd(tail, 90_000L, duration).endMs)
    }

    @Test
    fun shiftKeepsTheLengthAndStaysInsideTheVideo() {
        val range = TrimRange(10_000L, 20_000L)
        assertEquals(TrimRange(15_000L, 25_000L), TrimMath.shift(range, 5_000L, duration))
        assertEquals(TrimRange(0L, 10_000L), TrimMath.shift(range, -50_000L, duration))
        assertEquals(TrimRange(50_000L, 60_000L), TrimMath.shift(range, 90_000L, duration))
    }

    @Test
    fun fractionsAndPositionsRoundTrip() {
        assertEquals(0.5f, TrimMath.fraction(30_000L, duration), 0.0001f)
        assertEquals(0f, TrimMath.fraction(5L, 0L), 0f)
        assertEquals(15_000L, TrimMath.position(0.25f, duration))
        assertEquals(duration, TrimMath.position(2f, duration))
    }

    @Test
    fun labelsReadAsMinutesAndSeconds() {
        assertEquals("0:00", TrimMath.label(0L))
        assertEquals("0:15", TrimMath.label(14_600L))
        assertEquals("1:05", TrimMath.label(65_000L))
    }
}
