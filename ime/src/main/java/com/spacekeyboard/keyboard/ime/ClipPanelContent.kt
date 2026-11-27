package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.ui.ClipActions
import com.spacekeyboard.keyboard.clipboard.ui.ClipPanelHost
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun ClipPanelContent(theme: ThemeController, clip: ClipboardController, actions: ClipActions) {
    ClipPanelHost(clip, rememberThemeSnapshot(theme).palette, actions)
}
