package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode

@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    val look = SpaceLook.Eclipse
    ApplySystemBars(look.dark)
    SpaceTheme(mode = SpaceThemeMode.Dark, look = look, content = content)
}
