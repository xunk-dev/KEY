package com.spacekeyboard.keyboard.ime

import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.spacekeyboard.keyboard.core.layout.FeedbackPrefs
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.Limits

internal class KeyFeedback(private val view: View) {
    private val audio = view.context.getSystemService(AudioManager::class.java)
    private val motor = TouchVibrator(view.context)

    fun down(action: KeyAction, prefs: FeedbackPrefs) {
        if (prefs.vibrate) buzz(prefs, HapticFeedbackConstants.KEYBOARD_TAP, TAP_MS)
        if (prefs.sound) audio?.playSoundEffect(soundOf(action), volumeOf(prefs))
    }

    fun tick(prefs: FeedbackPrefs) {
        if (prefs.vibrate) buzz(prefs, HapticFeedbackConstants.CLOCK_TICK, TICK_MS)
    }

    fun longPress(prefs: FeedbackPrefs) {
        if (prefs.vibrate) buzz(prefs, HapticFeedbackConstants.LONG_PRESS, LONG_MS)
    }

    private fun buzz(prefs: FeedbackPrefs, system: Int, millis: Long) {
        if (prefs.vibrateStrength == Limits.STRENGTH_DEFAULT || !motor.canScale()) {
            view.performHapticFeedback(system)
        } else {
            motor.pulse(millis, prefs.vibrateStrength)
        }
    }

    private fun volumeOf(prefs: FeedbackPrefs): Float {
        if (prefs.soundVolume >= Limits.VOLUME_DEFAULT) return SYSTEM_VOLUME
        val level = prefs.soundVolume / Limits.VOLUME_MAX.toFloat()
        return level * level
    }

    private fun soundOf(action: KeyAction): Int = when (action) {
        KeyAction.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
        KeyAction.DELETE -> AudioManager.FX_KEYPRESS_DELETE
        KeyAction.ENTER -> AudioManager.FX_KEYPRESS_RETURN
        else -> AudioManager.FX_KEYPRESS_STANDARD
    }

    private companion object {
        const val TAP_MS = 14L
        const val TICK_MS = 8L
        const val LONG_MS = 24L
        const val SYSTEM_VOLUME = -1f
    }
}
