package com.spacekeyboard.keyboard.emoji.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiBurstPlanTest {
    @Test
    fun stagesGetFasterAndBiggerTheLongerYouHold() {
        val stages = EmojiBurstPlan.stages
        assertEquals(stages.sortedBy { it.afterMs }.map { it.afterMs }, stages.map { it.afterMs })
        assertTrue(stages.zipWithNext().all { (a, b) -> b.count > a.count && b.intervalMs <= a.intervalMs })
    }

    @Test
    fun stageIndexFollowsElapsedTime() {
        assertEquals(0, EmojiBurstPlan.stageIndex(0))
        assertEquals(0, EmojiBurstPlan.stageIndex(699))
        assertEquals(1, EmojiBurstPlan.stageIndex(700))
        assertEquals(3, EmojiBurstPlan.stageIndex(60_000))
    }

    @Test
    fun batchNeverPassesTheCap() {
        assertEquals(5, EmojiBurstPlan.batch(3, 10))
        assertEquals(2, EmojiBurstPlan.batch(3, EmojiBurstPlan.MAX_TOTAL - 2))
        assertEquals(0, EmojiBurstPlan.batch(3, EmojiBurstPlan.MAX_TOTAL))
    }

    @Test
    fun totalGrowsSmoothlyAndStopsAtTheCap() {
        assertEquals(1, EmojiBurstPlan.totalAfter(0))
        val totals = (0..10_000 step 250).map { EmojiBurstPlan.totalAfter(it.toLong()) }
        assertEquals(totals.sorted(), totals)
        assertTrue(EmojiBurstPlan.totalAfter(1_000) in 6..14)
        assertEquals(EmojiBurstPlan.MAX_TOTAL, EmojiBurstPlan.totalAfter(60_000))
    }
}
