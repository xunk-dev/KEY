package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class TouchVibrator(context: Context) {
    private val vibrator: Vibrator? = systemVibrator(context)

    fun canScale(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasAmplitudeControl() == true

    fun pulse(millis: Long, strengthPercent: Int) {
        val motor = vibrator ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val amplitude = (strengthPercent * MAX_AMPLITUDE / PERCENT).coerceIn(1, MAX_AMPLITUDE)
        val effect = VibrationEffect.createOneShot(millis, amplitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            motor.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
        } else {
            @Suppress("DEPRECATION")
            motor.vibrate(effect, touchAttributes())
        }
    }

    private fun touchAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    companion object {
        private const val MAX_AMPLITUDE = 255
        private const val PERCENT = 100

        fun supportsStrength(context: Context): Boolean = TouchVibrator(context).canScale()

        private fun systemVibrator(context: Context): Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                context.getSystemService(Vibrator::class.java)
            }
    }
}
