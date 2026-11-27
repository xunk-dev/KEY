package com.spacekeyboard.keyboard.core.sound

import kotlin.math.abs
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PcmClipTest {
    private val rate = 1_000

    private fun constant(value: Int, size: Int): ShortArray = ShortArray(size) { value.toShort() }

    private fun assertNear(expected: Int, actual: Short, tolerance: Int) {
        assertTrue("expected $expected but was $actual", abs(expected - actual.toInt()) <= tolerance)
    }

    @Test
    fun peakIsTheLargestMagnitude() {
        assertEquals(5, PcmClip.peak(shortArrayOf(0, -5, 3)))
        assertEquals(0, PcmClip.peak(ShortArray(0)))
        assertEquals(32_768, PcmClip.peak(shortArrayOf(-32_768)))
    }

    @Test
    fun levelIsThePeakOverFullScale() {
        assertEquals(1f, PcmClip.level(shortArrayOf(32_767)), 0f)
        assertEquals(0.5f, PcmClip.level(shortArrayOf(16_383)), 0.0001f)
        assertEquals(0f, PcmClip.level(ShortArray(0)), 0f)
    }

    @Test
    fun aSilentClipStaysSilent() {
        val input = ShortArray(100)
        val output = PcmClip.finish(input, rate)
        assertEquals(100, output.size)
        assertTrue(output.all { it.toInt() == 0 })
        assertNotSame(input, output)
    }

    @Test
    fun anEmptyClipStaysEmpty() {
        assertEquals(0, PcmClip.finish(ShortArray(0), rate).size)
    }

    @Test
    fun aQuietClipGainsAtMostMaxGain() {
        val output = PcmClip.finish(constant(1_000, 100), rate)
        assertEquals(1_000 * PcmClip.MAX_GAIN.toInt(), output[50].toInt())
        assertEquals(6_000, PcmClip.peak(output))
    }

    @Test
    fun aMediumClipIsNormalisedToTheTargetPeak() {
        val output = PcmClip.finish(constant(16_383, 100), rate)
        assertNear(29_490, output[50], 1)
    }

    @Test
    fun aLoudClipIsLoweredToTheTargetPeak() {
        val output = PcmClip.finish(constant(32_767, 100), rate)
        assertNear(29_490, output[50], 1)
    }

    @Test
    fun gainFollowsThePeakNotTheFirstSample() {
        val input = constant(100, 100).also { it[50] = 20_000 }
        val output = PcmClip.finish(input, rate)
        assertNear(29_490, output[50], 1)
    }

    @Test
    fun theLowestSampleStaysInRange() {
        val output = PcmClip.finish(constant(-32_768, 100), rate)
        assertNear(-29_490, output[50], 2)
    }

    @Test
    fun negativeSamplesKeepTheirSign() {
        val input = ShortArray(100) { if (it % 2 == 0) 1_000 else -1_000 }
        val output = PcmClip.finish(input, rate)
        assertEquals(6_000, output[50].toInt())
        assertEquals(-6_000, output[51].toInt())
    }

    @Test
    fun theClipFadesInOverTwoMilliseconds() {
        val output = PcmClip.finish(constant(1_000, 100), rate)
        assertEquals(3_000, output[0].toInt())
        assertEquals(6_000, output[1].toInt())
    }

    @Test
    fun theClipFadesOutOverTwelveMilliseconds() {
        val output = PcmClip.finish(constant(1_000, 100), rate)
        assertEquals(6_000, output[87].toInt())
        assertEquals(6_000, output[88].toInt())
        assertTrue(output[99] < output[98])
        assertTrue(output[98] < output[90])
        assertTrue(output[90] < output[88])
        assertNear(500, output[99], 1)
        assertNear(1_000, output[98], 1)
    }

    @Test
    fun fadesAreCappedToHalfOfAShortClip() {
        val output = PcmClip.finish(constant(1_000, 10), 48_000)
        assertNear(1_200, output[0], 1)
        assertNear(1_200, output[9], 1)
        assertEquals(6_000, output[4].toInt())
        assertEquals(6_000, output[5].toInt())
        assertTrue(output.all { it in 1..6_000 })
    }

    @Test
    fun aSingleSampleIsNotFaded() {
        assertArrayEquals(shortArrayOf(6_000), PcmClip.finish(shortArrayOf(1_000), rate))
    }

    @Test
    fun finishDoesNotChangeItsInput() {
        val input = constant(1_000, 100)
        PcmClip.finish(input, rate)
        assertArrayEquals(constant(1_000, 100), input)
    }

    @Test
    fun theLoudestSampleOfALongClipReachesTheTarget() {
        val input = ShortArray(5_000) { ((it % 400 - 200) * 50).toShort() }
        val output = PcmClip.finish(input, 44_100)
        assertTrue(PcmClip.peak(output) in 29_480..29_500)
    }
}
