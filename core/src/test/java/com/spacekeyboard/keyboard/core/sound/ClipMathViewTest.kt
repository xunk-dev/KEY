package com.spacekeyboard.keyboard.core.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipMathViewTest {
    private val duration = 20_000L

    @Test
    fun viewAroundCentresOnTheRange() {
        val view = ClipMath.viewAround(ClipRange(6_000L, 7_000L), 3_000L, duration)
        assertEquals(ClipRange(5_000L, 8_000L), view)
    }

    @Test
    fun viewAroundIsClampedAtBothEnds() {
        assertEquals(ClipRange(0L, 6_000L), ClipMath.viewAround(ClipRange(1_000L, 2_000L), 6_000L, duration))
        assertEquals(ClipRange(14_000L, 20_000L), ClipMath.viewAround(ClipRange(19_000L, 20_000L), 6_000L, duration))
    }

    @Test
    fun viewAroundNeverExceedsTheSound() {
        assertEquals(ClipRange(0L, 4_000L), ClipMath.viewAround(ClipRange(1_000L, 2_000L), 15_000L, 4_000L))
    }

    @Test
    fun viewAroundOfAZeroSpanIsOneMillisecond() {
        assertEquals(ClipRange(1_500L, 1_501L), ClipMath.viewAround(ClipRange(1_000L, 2_000L), 0L, 4_000L))
    }

    @Test
    fun viewAroundOfAnEmptySoundIsOneMillisecond() {
        assertEquals(ClipRange(0L, 1L), ClipMath.viewAround(ClipRange(0L, 0L), 3_000L, 0L))
    }

    @Test
    fun panMovesTheWindowAndKeepsItsLength() {
        val view = ClipRange(1_000L, 7_000L)
        assertEquals(ClipRange(1_500L, 7_500L), ClipMath.pan(view, 500L, duration))
        assertEquals(ClipRange(0L, 6_000L), ClipMath.pan(view, -5_000L, duration))
        assertEquals(ClipRange(14_000L, 20_000L), ClipMath.pan(view, 99_999L, duration))
    }

    @Test
    fun panOfAWindowLongerThanTheSoundStaysPut() {
        val view = ClipRange(0L, 6_000L)
        assertEquals(view, ClipMath.pan(view, 700L, 4_000L))
        assertEquals(view, ClipMath.pan(view, -700L, 4_000L))
    }

    @Test
    fun panKeepsTheLengthForAnyDelta() {
        val view = ClipRange(3_000L, 6_000L)
        for (delta in -5_000L..25_000L step 211L) {
            val moved = ClipMath.pan(view, delta, duration)
            assertEquals(view.lengthMs, moved.lengthMs)
            assertTrue(moved.startMs >= 0L && moved.endMs <= duration)
        }
    }

    @Test
    fun revealLeavesTheViewWhenTheRangeIsInside() {
        val view = ClipRange(0L, 6_000L)
        assertSame(view, ClipMath.reveal(view, ClipRange(1_000L, 2_000L), duration))
        assertSame(view, ClipMath.reveal(view, ClipRange(0L, 6_000L), duration))
    }

    @Test
    fun revealScrollsLeftToTheRangeStart() {
        val view = ClipRange(5_000L, 11_000L)
        assertEquals(ClipRange(4_000L, 10_000L), ClipMath.reveal(view, ClipRange(4_000L, 4_500L), duration))
    }

    @Test
    fun revealScrollsRightToTheRangeEnd() {
        val view = ClipRange(0L, 6_000L)
        assertEquals(ClipRange(1_000L, 7_000L), ClipMath.reveal(view, ClipRange(5_500L, 7_000L), duration))
    }

    @Test
    fun revealPrefersTheStartWhenTheRangeIsWiderThanTheView() {
        val view = ClipRange(2_000L, 5_000L)
        assertEquals(ClipRange(1_000L, 4_000L), ClipMath.reveal(view, ClipRange(1_000L, 9_000L), duration))
    }

    @Test
    fun revealCannotScrollPastTheEndOfTheSound() {
        val view = ClipRange(14_000L, 20_000L)
        assertEquals(view, ClipMath.reveal(view, ClipRange(19_800L, 20_300L), duration))
    }

    @Test
    fun secondsAreFormattedWithTwoDecimals() {
        assertEquals("0.00", ClipMath.seconds(0L))
        assertEquals("0.04", ClipMath.seconds(40L))
        assertEquals("1.00", ClipMath.seconds(999L))
        assertEquals("1.50", ClipMath.seconds(1_500L))
        assertEquals("3.00", ClipMath.seconds(3_000L))
    }

    @Test
    fun negativeSecondsReadAsZero() {
        assertEquals("0.00", ClipMath.seconds(-5L))
    }
}
