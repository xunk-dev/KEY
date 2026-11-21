package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.core.theme.ThemeSlot
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun ThemesScreen(controller: ThemeController, layouts: LayoutController, onBack: () -> Unit) {
    val snapshot = rememberThemeSnapshot(controller)
    ScreenFrame(R.string.themes_title, onBack) {
        KeyboardPreview(controller, layouts)
        SpaceSectionTitle(stringResource(R.string.app_look_title))
        AppLookPicker()
        BodyText(R.string.app_look_note)
        SpaceSectionTitle(stringResource(R.string.section_keyboard_theme))
        ThemeGrid(snapshot.choice.themeId, controller::select)
        SettingsCard {
            SwitchRow(stringResource(R.string.themes_follow_system), snapshot.choice.followSystem, controller::setFollowSystem)
        }
        BodyText(R.string.themes_follow_note)
        ColorsSection(controller)
    }
}

@Composable
private fun ColorsSection(controller: ThemeController) {
    val snapshot = rememberThemeSnapshot(controller)
    var slot by remember { mutableStateOf(ThemeSlot.GLOW) }
    val requested = snapshot.choice.requested()
    SpaceSectionTitle(stringResource(R.string.section_colors))
    SlotTabs(slot, requested, { slot = it })
    ColorEditor(requested[slot]) { controller.setColor(slot, it) }
    Notices(adjusted = snapshot.palette.adjusted, paused = snapshot.choice.animated && !snapshot.motion)
    SettingsCard {
        SwitchRow(stringResource(R.string.animated_backgrounds), snapshot.choice.animated, controller::setAnimated)
    }
    SpaceButton(stringResource(R.string.reset_colors), controller::resetColors)
}

@Composable
private fun Notices(adjusted: Boolean, paused: Boolean) {
    if (adjusted) Notice(R.string.colors_adjusted)
    if (paused) Notice(R.string.animation_paused)
}

@Composable
private fun Notice(message: Int) {
    SpaceText(
        text = stringResource(message),
        style = SpaceTheme.typography.bodySmall,
        color = SpaceTheme.colors.warning,
    )
}
