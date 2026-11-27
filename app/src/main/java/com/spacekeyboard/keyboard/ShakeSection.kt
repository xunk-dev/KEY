package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.shake.ShakeAction
import com.spacekeyboard.keyboard.core.shake.ShakePrefsStore
import com.spacekeyboard.keyboard.core.shake.ShakeSensitivity
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun ShakeSection() {
    val context = LocalContext.current
    val store = remember { ShakePrefsStore(context) }
    var prefs by remember { mutableStateOf(store.read()) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceText(stringResource(R.string.section_shake), SpaceTheme.typography.title)
        SwitchRow(stringResource(R.string.shake_enable), prefs.enabled) {
            store.setEnabled(it)
            prefs = store.read()
        }
        BodyText(R.string.shake_hint)
        if (prefs.enabled) {
            SpaceText(stringResource(R.string.shake_action_title), SpaceTheme.typography.bodySmall)
            ChipGrid(ShakeAction.entries, prefs.action, ::actionLabel) {
                store.setAction(it)
                prefs = store.read()
            }
            SpaceText(stringResource(R.string.shake_sensitivity_title), SpaceTheme.typography.bodySmall)
            ChipGrid(ShakeSensitivity.entries, prefs.sensitivity, ::sensitivityLabel) {
                store.setSensitivity(it)
                prefs = store.read()
            }
        }
    }
}

private fun actionLabel(action: ShakeAction): Int = when (action) {
    ShakeAction.CALCULATOR -> R.string.shake_action_calculator
    ShakeAction.EMOJI -> R.string.shake_action_emoji
    ShakeAction.CLIPBOARD -> R.string.shake_action_clipboard
    ShakeAction.TRANSLATE -> R.string.shake_action_translate
    ShakeAction.SHORTCUTS -> R.string.shake_action_shortcuts
    ShakeAction.VOICE -> R.string.shake_action_voice
    ShakeAction.SYMBOLS -> R.string.shake_action_symbols
    ShakeAction.IMAGES -> R.string.shake_action_images
    ShakeAction.DECOR -> R.string.shake_action_decor
    ShakeAction.FLOAT -> R.string.shake_action_float
}

private fun sensitivityLabel(level: ShakeSensitivity): Int = when (level) {
    ShakeSensitivity.LOW -> R.string.shake_sens_low
    ShakeSensitivity.MEDIUM -> R.string.shake_sens_medium
    ShakeSensitivity.HIGH -> R.string.shake_sens_high
}
