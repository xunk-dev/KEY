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
internal fun KeyboardColorsScreen(controller: ThemeController, layouts: LayoutController, onBack: () -> Unit) {
    val snapshot = rememberThemeSnapshot(controller)
    var slot by remember { mutableStateOf(ThemeSlot.GLOW) }
    val requested = snapshot.choice.requested()
    ScreenFrame(R.string.colors_title, onBack) {
        KeyboardPreview(controller, layouts)
        SpaceSectionTitle(stringResource(R.string.section_colors))
        SlotTabs(slot, requested, { slot = it })
        ColorEditor(requested[slot]) { controller.setColor(slot, it) }
        if (snapshot.palette.adjusted) Notice(R.string.colors_adjusted)
        SpaceButton(stringResource(R.string.reset_colors), controller::resetColors)
    }
}

@Composable
internal fun Notice(message: Int) {
    SpaceText(
        text = stringResource(message),
        style = SpaceTheme.typography.bodySmall,
        color = SpaceTheme.colors.warning,
    )
}
