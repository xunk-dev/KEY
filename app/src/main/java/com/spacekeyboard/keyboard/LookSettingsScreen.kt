package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.DigitsMode
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController

@Composable
fun LookSettingsScreen(theme: ThemeController, layouts: LayoutController, onBack: () -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ScreenFrame(R.string.settings_look_title, onBack) {
        KeyboardPreview(theme, layouts)
        ChipGrid(PanelMode.selectable, prefs.mode, { it.labelRes }, layouts::setMode)
        SizeSliders(prefs, layouts)
        ArabicStyleSection(prefs.arabicStyle, layouts)
        AdvancedGroup {
            DigitsChoice(prefs, layouts)
            LabelStyleSection(prefs.label, layouts)
            SpaceButton(stringResource(R.string.reset_customization), layouts::resetAll)
        }
    }
}

@Composable
private fun DigitsChoice(prefs: KeyboardPrefs, layouts: LayoutController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceText(stringResource(R.string.typing_number_row), SpaceTheme.typography.bodySmall)
        ChipGrid(DigitsMode.entries, prefs.typing.digits, DigitsMode::labelRes, layouts::setDigitsMode)
    }
}

@Composable
internal fun SizeSliders(prefs: KeyboardPrefs, layouts: LayoutController) {
    SettingsCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ValueSlider(
                stringResource(R.string.key_size), prefs.keySizePercent, Limits.KEY_MIN, Limits.KEY_MAX, layouts::setKeySize,
            )
            ValueSlider(
                stringResource(R.string.panel_height), prefs.heightPercent, Limits.HEIGHT_MIN, Limits.HEIGHT_MAX, layouts::setHeight,
            )
            ModeSliders(prefs, layouts)
        }
    }
}

@Composable
private fun ModeSliders(prefs: KeyboardPrefs, layouts: LayoutController) {
    if (prefs.mode == PanelMode.ONE_HAND_LEFT || prefs.mode == PanelMode.ONE_HAND_RIGHT) {
        ValueSlider(
            stringResource(R.string.one_hand_width), prefs.oneHandPercent, Limits.ONE_HAND_MIN, Limits.ONE_HAND_MAX,
            layouts::setOneHand,
        )
    }
    if (prefs.mode == PanelMode.SPLIT) {
        ValueSlider(
            stringResource(R.string.split_gap), prefs.splitGapPercent, Limits.SPLIT_MIN, Limits.SPLIT_MAX,
            layouts::setSplitGap,
        )
    }
    if (prefs.mode == PanelMode.FLOAT) {
        ValueSlider(
            stringResource(R.string.float_width), prefs.floatWidthPercent, Limits.FLOAT_WIDTH_MIN, Limits.FLOAT_WIDTH_MAX,
            layouts::setFloatWidth,
        )
    }
}
