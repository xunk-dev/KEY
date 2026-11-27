package com.spacekeyboard.keyboard.core.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipMathEditTest {
    private val duration = 10_000L

    @Test
    fun moveStartFollowsTheTargetInsideTheLimits() {
        val range = ClipRange(1_000L, 2_000L)
        assertEquals(1_500L, ClipMath.moveStart(range, 1_500L, duration).startMs)
        assertEquals(0L, ClipMath.moveStart(range, -50L, duration).startMs)
    }

    @Test
    fun moveStartKeepsTheEnd() {
        val moved = ClipMath.moveStart(ClipRange(1_000L, 2_000L), 1_200L, duration)
        assertEquals(2_000L, moved.endMs)
    }

    @Test
    fun moveStartStopsAtTheMinimumLength() {
        val range = ClipRange(1_000L, 2_000L)
        val past = ClipMath.moveStart(range, 1_990L, duration)
        assertEquals(2_000L - ClipMath.MIN_MS, past.startMs)
        assertEquals(ClipMath.MIN_MS, past.lengthMs)
        assertEquals(2_000L - ClipMath.MIN_MS, ClipMath.moveStart(range, 5_000L, duration).startMs)
    }

    @Test
    fun moveStartStopsAtTheMaximumLength() {
        val moved = ClipMath.moveStart(ClipRange(4_000L, 6_000L), 0L, duration)
        assertEquals(3_000L, moved.startMs)
        assertEquals(ClipMath.MAX_MS, moved.lengthMs)
    }

    @Test
    fun moveEndFollowsTheTargetInsideTheLimits() {
        val range = ClipRange(1_000L, 2_000L)
        assertEquals(2_500L, ClipMath.moveEnd(range, 2_500L, duration).endMs)
        assertEquals(1_000L, ClipMath.moveEnd(range, 2_500L, duration).startMs)
    }

    @Test
    fun moveEndStopsAtTheMinimumLength() {
        val range = ClipRange(1_000L, 2_000L)
        assertEquals(1_000L + ClipMath.MIN_MS, ClipMath.moveEnd(range, 1_005L, duration).endMs)
        assertEquals(1_000L + ClipMath.MIN_MS, ClipMath.moveEnd(range, -500L, duration).endMs)
    }

    @Test
    fun moveEndStopsAtTheMaximumLength() {
        val moved = ClipMath.moveEnd(ClipRange(1_000L, 2_000L), 9_000L, duration)
        assertEquals(4_000L, moved.endMs)
        assertEquals(ClipMath.MAX_MS, moved.lengthMs)
    }

    @Test
    fun moveEndStopsAtTheEndOfTheSound() {
        val range = ClipRange(9_900L, 9_950L)
        assertEquals(duration, ClipMath.moveEnd(range, 20_000L, duration).endMs)
    }

    @Test
    fun handlesNeverLeaveTheLimitsForAnyTarget() {
        val range = ClipRange(1_000L, 2_000L)
        for (target in -200L..11_000L step 13L) {
            val start = ClipMath.moveStart(range, target, duration)
            val end = ClipMath.moveEnd(range, target, duration)
            assertTrue(start.lengthMs in ClipMath.MIN_MS..ClipMath.MAX_MS)
            assertTrue(end.lengthMs in ClipMath.MIN_MS..ClipMath.MAX_MS)
            assertTrue(start.startMs >= 0L && end.endMs <= duration)
        }
    }

    @Test
    fun aSoundShorterThanTheMinimumCannotBeResized() {
        val range = ClipRange(0L, 30L)
        assertEquals(range, ClipMath.moveStart(range, 5L, 30L))
        assertEquals(range, ClipMath.moveEnd(range, 10L, 30L))
    }

    @Test
    fun aSoundExactlyTheMinimumCannotBeResized() {
        val range = ClipRange(0L, ClipMath.MIN_MS)
        assertEquals(range, ClipMath.moveStart(range, 10L, ClipMath.MIN_MS))
        assertEquals(range, ClipMath.moveEnd(range, 10L, ClipMath.MIN_MS))
    }

    @Test
    fun slideMovesBothEdgesAndKeepsTheLength() {
        val range = ClipRange(1_000L, 1_500L)
        assertEquals(ClipRange(1_200L, 1_700L), ClipMath.slide(range, 200L, 5_000L))
        assertEquals(ClipRange(0L, 500L), ClipMath.slide(range, -5_000L, 5_000L))
        assertEquals(ClipRange(4_500L, 5_000L), ClipMath.slide(range, 9_999L, 5_000L))
    }

    @Test
    fun slideOfAClipLongerThanTheSoundStaysPut() {
        val range = ClipRange(0L, 500L)
        assertEquals(range, ClipMath.slide(range, 100L, 300L))
        assertEquals(range, ClipMath.slide(range, -100L, 300L))
    }

    @Test
    fun slideKeepsTheLengthForAnyDelta() {
        val range = ClipRange(2_000L, 2_750L)
        for (delta in -3_000L..9_000L step 101L) {
            val moved = ClipMath.slide(range, delta, duration)
            assertEquals(range.lengthMs, moved.lengthMs)
            assertTrue(moved.startMs >= 0L && moved.endMs <= duration)
        }
    }
}
