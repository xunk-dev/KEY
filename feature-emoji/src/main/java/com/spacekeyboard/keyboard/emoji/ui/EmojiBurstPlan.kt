package com.spacekeyboard.keyboard.emoji.ui

object EmojiBurstPlan {
    const val HOLD_MS = 350L
    const val MAX_TOTAL = 40

    class Stage(val afterMs: Long, val count: Int, val intervalMs: Long)

    val stages = listOf(
        Stage(0L, 1, 140L),
        Stage(700L, 2, 120L),
        Stage(1_500L, 3, 110L),
        Stage(2_500L, 5, 100L),
    )

    fun stageIndex(elapsedMs: Long): Int = stages.indexOfLast { elapsedMs >= it.afterMs }.coerceAtLeast(0)

    fun batch(stage: Int, sent: Int): Int = minOf(stages[stage].count, MAX_TOTAL - sent).coerceAtLeast(0)

    fun totalAfter(holdMs: Long): Int {
        var sent = 1
        var at = 0L
        while (true) {
            at += stages[stageIndex(at)].intervalMs
            if (at > holdMs) return sent
            val more = batch(stageIndex(at), sent)
            if (more == 0) return sent
            sent += more
        }
    }
}
