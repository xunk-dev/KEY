package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.ui.DecorActions
import com.spacekeyboard.keyboard.decorate.ui.DecorPanelHost
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun DecorPanelContent(theme: ThemeController, decor: DecorController, actions: DecorActions) {
    DecorPanelHost(decor, rememberThemeSnapshot(theme).palette, actions)
}
