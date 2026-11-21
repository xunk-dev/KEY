package com.spacekeyboard.keyboard

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode

@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    val saved by AppLookStore.current(LocalContext.current)
    val look = saved ?: SpaceLook.forSystem(isSystemInDarkTheme())
    ApplySystemBars(look.dark)
    SpaceTheme(mode = SpaceThemeMode.System, look = look, content = content)
}
