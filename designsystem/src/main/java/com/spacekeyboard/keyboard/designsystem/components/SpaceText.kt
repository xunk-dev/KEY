package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun SpaceText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = SpaceTheme.colors.onSurface,
) {
    BasicText(text = text, modifier = modifier, style = style.copy(color = color))
}
