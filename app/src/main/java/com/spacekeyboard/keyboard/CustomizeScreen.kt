package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController

@Composable
fun CustomizeScreen(theme: ThemeController, layouts: LayoutController, onEditor: () -> Unit, onBack: () -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ScreenFrame(R.string.customize_title, onBack) {
        KeyboardPreview(theme, layouts)
        SpaceSectionTitle(stringResource(R.string.section_mode))
        ChipGrid(PanelMode.entries, prefs.mode, { it.labelRes }, layouts::setMode)
        SizeSliders(prefs, layouts)
        LabelStyleSection(prefs.label, layouts)
        TypingSection(prefs, layouts)
        VoiceSection()
        FeedbackSection(prefs.feedback, layouts)
        SpacePrimaryButton(stringResource(R.string.action_edit_layout), onEditor)
        SpaceButton(stringResource(R.string.reset_customization), layouts::resetAll)
    }
}

@Composable
private fun SizeSliders(prefs: KeyboardPrefs, layouts: LayoutController) {
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
}
