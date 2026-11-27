package com.spacekeyboard.keyboard.core.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipMathInitialTest {
    private fun peaks(size: Int, vararg loud: Pair<Int, Float>): FloatArray =
        FloatArray(size).also { levels -> loud.forEach { (index, value) -> levels[index] = value } }

    @Test
    fun limitsAreInOrder() {
        assertTrue(ClipMath.MIN_MS < ClipMath.DEFAULT_MS)
        assertTrue(ClipMath.DEFAULT_MS <= ClipMath.MAX_MS)
        assertEquals(10L, PeakBuilder.STEP_MS)
    }

    @Test
    fun onsetOfNoPeaksOrSilenceIsZero() {
        assertEquals(0L, ClipMath.onsetMs(FloatArray(0)))
        assertEquals(0L, ClipMath.onsetMs(FloatArray(50)))
    }

    @Test
    fun onsetIsTheFirstBucketReachingAQuarterOfTheLoudest() {
        assertEquals(20L, ClipMath.onsetMs(floatArrayOf(0.1f, 0.2f, 0.5f, 1f)))
        assertEquals(30L, ClipMath.onsetMs(floatArrayOf(0f, 0.1f, 0.2f, 0.26f, 1f)))
    }

    @Test
    fun onsetIncludesABucketExactlyAtTheThreshold() {
        assertEquals(10L, ClipMath.onsetMs(floatArrayOf(0f, 0.25f, 1f)))
    }

    @Test
    fun onsetFollowsTheLoudestNotFullScale() {
        assertEquals(20L, ClipMath.onsetMs(floatArrayOf(0.02f, 0.04f, 0.4f)))
        assertEquals(0L, ClipMath.onsetMs(floatArrayOf(1f, 0.5f)))
    }

    @Test
    fun initialStartsJustBeforeTheOnsetWithTheDefaultLength() {
        val range = ClipMath.initial(3_000L, peaks(300, 100 to 1f))
        assertEquals(ClipRange(980L, 1_480L), range)
        assertEquals(ClipMath.DEFAULT_MS, range.lengthMs)
    }

    @Test
    fun initialOfSilenceStartsAtZero() {
        assertEquals(ClipRange(0L, 500L), ClipMath.initial(3_000L, FloatArray(300)))
    }

    @Test
    fun initialNeverStartsBeforeZero() {
        assertEquals(ClipRange(0L, 500L), ClipMath.initial(3_000L, peaks(300, 1 to 1f)))
    }

    @Test
    fun initialIsPulledBackSoTheClipEndsInsideTheSound() {
        assertEquals(ClipRange(1_500L, 2_000L), ClipMath.initial(2_000L, peaks(200, 195 to 1f)))
    }

    @Test
    fun initialOfASoundShorterThanTheDefaultCoversAllOfIt() {
        assertEquals(ClipRange(0L, 300L), ClipMath.initial(300L, peaks(30, 5 to 1f)))
        assertEquals(ClipRange(0L, 25L), ClipMath.initial(25L, peaks(3, 1 to 1f)))
    }

    @Test
    fun initialOfAnEmptySoundIsEmpty() {
        val range = ClipMath.initial(0L, FloatArray(1))
        assertEquals(ClipRange(0L, 0L), range)
        assertEquals(0L, range.lengthMs)
    }

    @Test
    fun initialStaysInsideTheSoundForAnyDuration() {
        for (duration in 0L..5_000L step 37L) {
            val range = ClipMath.initial(duration, FloatArray(((duration + 9) / 10).toInt().coerceAtLeast(1)))
            assertTrue(range.startMs >= 0L)
            assertTrue(range.endMs <= duration)
            assertEquals(minOf(ClipMath.DEFAULT_MS, duration), range.lengthMs)
        }
    }
}
