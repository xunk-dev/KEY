package com.spacekeyboard.keyboard.core.sound

import kotlin.math.nextDown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PeakBuilderTest {
    private val delta = 0.000001f

    @Test
    fun oneBucketPerTenMillisecondsRoundedUp() {
        assertEquals(100, PeakBuilder(1_000L).build().size)
        assertEquals(101, PeakBuilder(1_001L).build().size)
        assertEquals(1, PeakBuilder(1L).build().size)
    }

    @Test
    fun anEmptyOrNegativeDurationStillHasOneBucket() {
        assertEquals(1, PeakBuilder(0L).build().size)
        assertEquals(1, PeakBuilder(-5L).build().size)
    }

    @Test
    fun levelsKeepTheMaximumOfTheirBucket() {
        val builder = PeakBuilder(100L)
        builder.add(0L, 0.2f)
        builder.add(9_999L, 0.4f)
        builder.add(5_000L, 0.1f)
        builder.add(10_000L, 0.1f)
        builder.add(95_000L, 0.8f)
        val peaks = builder.build()
        assertEquals(0.5f, peaks[0], delta)
        assertEquals(0.125f, peaks[1], delta)
        assertEquals(1f, peaks[9], delta)
        assertEquals(0f, peaks[5], delta)
    }

    @Test
    fun buildNormalisesToTheLoudestLevel() {
        val builder = PeakBuilder(50L)
        builder.add(0L, 0.05f)
        builder.add(20_000L, 0.1f)
        assertEquals(0.1f, builder.loudest, 0f)
        assertEquals(1f, builder.build()[2], 0f)
        assertEquals(0.5f, builder.build()[0], delta)
    }

    @Test
    fun aQuieterLevelDoesNotLowerABucket() {
        val builder = PeakBuilder(20L)
        builder.add(0L, 0.6f)
        builder.add(1_000L, 0.2f)
        assertEquals(0.6f, builder.loudest, 0f)
        assertEquals(1f, builder.build()[0], 0f)
    }

    @Test
    fun timesPastTheEndAreIgnored() {
        val builder = PeakBuilder(1_000L)
        builder.add(1_000_000L, 1f)
        builder.add(5_000_000L, 1f)
        assertEquals(0f, builder.loudest, 0f)
        assertTrue(builder.silent)
        assertTrue(builder.build().all { it == 0f })
    }

    @Test
    fun timesBeforeTheStartAreIgnored() {
        val builder = PeakBuilder(1_000L)
        builder.add(-10_000L, 1f)
        builder.add(-1_000_000L, 1f)
        assertEquals(0f, builder.loudest, 0f)
        assertTrue(builder.silent)
    }

    @Test
    fun theLastBucketAcceptsLevels() {
        val builder = PeakBuilder(1_000L)
        builder.add(999_000L, 0.7f)
        assertEquals(1f, builder.build()[99], 0f)
    }

    @Test
    fun noLevelsIsSilentAndBuildsZeros() {
        val builder = PeakBuilder(500L)
        assertTrue(builder.silent)
        assertEquals(50, builder.build().size)
        assertTrue(builder.build().all { it == 0f })
    }

    @Test
    fun aZeroLevelKeepsTheBuilderSilent() {
        val builder = PeakBuilder(500L)
        builder.add(0L, 0f)
        assertEquals(0f, builder.loudest, 0f)
        assertTrue(builder.silent)
    }

    @Test
    fun silenceIsBelowTheThresholdOnly() {
        val quiet = PeakBuilder(100L)
        quiet.add(0L, PeakBuilder.SILENCE.nextDown())
        assertTrue(quiet.silent)
        val audible = PeakBuilder(100L)
        audible.add(0L, PeakBuilder.SILENCE)
        assertFalse(audible.silent)
    }

    @Test
    fun aSilentBuildIsACopy() {
        val builder = PeakBuilder(100L)
        val first = builder.build()
        first[0] = 9f
        assertEquals(0f, builder.build()[0], 0f)
    }
}
