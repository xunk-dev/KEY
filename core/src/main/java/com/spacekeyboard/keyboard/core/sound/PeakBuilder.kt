package com.spacekeyboard.keyboard.core.sound

class PeakBuilder(durationMs: Long) {
    private val levels = FloatArray(((durationMs + STEP_MS - 1) / STEP_MS).toInt().coerceAtLeast(1))

    var loudest = 0f
        private set

    fun add(timeUs: Long, level: Float) {
        val index = (timeUs / 1000L / STEP_MS).toInt()
        if (index !in levels.indices) return
        if (level > levels[index]) levels[index] = level
        if (level > loudest) loudest = level
    }

    fun build(): FloatArray {
        if (loudest <= 0f) return levels.copyOf()
        return FloatArray(levels.size) { levels[it] / loudest }
    }

    val silent: Boolean get() = loudest < SILENCE

    companion object {
        const val STEP_MS = 10L
        const val SILENCE = 0.003f
    }
}
