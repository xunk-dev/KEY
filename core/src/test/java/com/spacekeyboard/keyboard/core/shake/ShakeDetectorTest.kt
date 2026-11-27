package com.spacekeyboard.keyboard.core.shake

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class ShakeDetectorTest {
    private fun run(seconds: Float, amplitude: Float, hz: Float, bumps: Boolean = false, detector: ShakeDetector = ShakeDetector()): Int {
        var fired = 0
        var t = 0L
        while (t <= (seconds * 1000).toLong()) {
            val wave = if (bumps) 0f else amplitude * sin(2.0 * PI * hz * t / 1000.0).toFloat()
            if (detector.feed(wave, 0f, 9.81f, t)) fired++
            t += 20
        }
        return fired
    }

    @Test
    fun aRealShakeFiresOnce() = assertEquals(1, run(1f, 20f, 3f))

    @Test
    fun aPhoneAtRestNeverFires() = assertEquals(0, run(5f, 0f, 0f))

    @Test
    fun walkingNeverFires() = assertEquals(0, run(5f, 3f, 2f))

    @Test
    fun oneBumpNeverFires() {
        val detector = ShakeDetector()
        var fired = false
        var t = 0L
        while (t < 2000) {
            val spike = if (t in 500L..560L) 25f else 0f
            fired = fired or detector.feed(spike, 0f, 9.81f, t)
            t += 20
        }
        assertFalse(fired)
    }

    @Test
    fun keepingOnShakingDoesNotFireAgainInsideTheCooldown() = assertEquals(1, run(1.4f, 20f, 3f))

    @Test
    fun aLongShakeFiresAgainAfterTheCooldown() = assertTrue(run(4f, 20f, 3f) >= 2)

    @Test
    fun highSensitivityCatchesAGentlerShake() {
        assertEquals(0, run(1f, 9f, 3f, detector = ShakeDetector(ShakeSensitivity.MEDIUM.threshold + 4f)))
        assertEquals(1, run(1f, 12f, 3f, detector = ShakeDetector(ShakeSensitivity.HIGH.threshold)))
    }

    @Test
    fun unknownSavedValuesFallBackToDefaults() {
        assertEquals(ShakeAction.CALCULATOR, ShakeAction.byId("nope"))
        assertEquals(ShakeAction.CALCULATOR, ShakeAction.byId(null))
        assertEquals(ShakeSensitivity.MEDIUM, ShakeSensitivity.byId("nope"))
        assertEquals(ShakeAction.VOICE, ShakeAction.byId("voice"))
    }
}
