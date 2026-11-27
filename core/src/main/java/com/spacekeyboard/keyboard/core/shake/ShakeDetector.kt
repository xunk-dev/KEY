package com.spacekeyboard.keyboard.core.shake

import kotlin.math.sqrt

class ShakeDetector(var threshold: Float = ShakeSensitivity.MEDIUM.threshold) {
    private class Push(val time: Long, val x: Float, val y: Float, val z: Float) {
        fun along(other: Push): Float = x * other.x + y * other.y + z * other.z
    }

    private var gx = 0f
    private var gy = 0f
    private var gz = 0f
    private var ready = false
    private val pushes = ArrayList<Push>()
    private var lastFired = -COOLDOWN_MS

    fun reset() {
        ready = false
        pushes.clear()
    }

    fun feed(x: Float, y: Float, z: Float, timeMs: Long): Boolean {
        if (!ready) {
            gx = x
            gy = y
            gz = z
            ready = true
            return false
        }
        gx = GRAVITY_KEEP * gx + (1f - GRAVITY_KEEP) * x
        gy = GRAVITY_KEEP * gy + (1f - GRAVITY_KEEP) * y
        gz = GRAVITY_KEEP * gz + (1f - GRAVITY_KEEP) * z
        val push = Push(timeMs, x - gx, y - gy, z - gz)
        pushes.removeAll { timeMs - it.time > WINDOW_MS }
        if (sqrt(push.along(push)) < threshold) return false
        val last = pushes.lastOrNull()
        if (last != null && push.along(last) > 0f && timeMs - last.time < SWING_MS) return false
        pushes.add(push)
        if (pushes.size < MIN_PUSHES || turns() < MIN_TURNS) return false
        pushes.clear()
        if (timeMs - lastFired < COOLDOWN_MS) return false
        lastFired = timeMs
        return true
    }

    private fun turns(): Int = (1 until pushes.size).count { pushes[it].along(pushes[it - 1]) < 0f }

    private companion object {
        const val GRAVITY_KEEP = 0.9f
        const val WINDOW_MS = 800L
        const val SWING_MS = 60L
        const val COOLDOWN_MS = 1500L
        const val MIN_PUSHES = 4
        const val MIN_TURNS = 3
    }
}
