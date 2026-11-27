package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.FeedbackPrefs
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.ime.TouchVibrator
import com.spacekeyboard.keyboard.ime.layout.LayoutController

@Composable
fun FeedbackSection(feedback: FeedbackPrefs, layouts: LayoutController, withSound: Boolean = true) {
    val context = LocalContext.current
    val scalable = remember(context) { TouchVibrator.supportsStrength(context) }
    SettingsCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SwitchRow(stringResource(R.string.feedback_vibrate), feedback.vibrate, layouts::setVibrate)
            if (feedback.vibrate && scalable) {
                ValueSlider(
                    stringResource(R.string.feedback_vibrate_strength), feedback.vibrateStrength,
                    Limits.STRENGTH_MIN, Limits.STRENGTH_MAX, layouts::setVibrateStrength,
                )
            }
            if (withSound) SoundBasics(feedback, layouts)
            SwitchRow(stringResource(R.string.feedback_preview), feedback.preview, layouts::setPreview)
        }
    }
}

@Composable
internal fun SoundBasics(feedback: FeedbackPrefs, layouts: LayoutController) {
    SwitchRow(stringResource(R.string.feedback_sound), feedback.sound, layouts::setSound)
    if (feedback.sound) {
        ValueSlider(
            stringResource(R.string.feedback_sound_volume), feedback.soundVolume,
            Limits.VOLUME_MIN, Limits.VOLUME_MAX, layouts::setSoundVolume,
        )
    }
}
