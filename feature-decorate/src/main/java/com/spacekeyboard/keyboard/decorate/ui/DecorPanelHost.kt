package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.rememberDecorSnapshot

@Composable
fun DecorPanelHost(controller: DecorController, palette: ThemePalette, actions: DecorActions) {
    val state = rememberDecorSnapshot(controller)
    if (state.open) DecorPanel(state, palette, actions)
}
