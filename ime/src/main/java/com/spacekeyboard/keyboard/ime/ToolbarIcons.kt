package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView

internal fun ToolbarItem.glyph(): SpaceIcon = when (this) {
    ToolbarItem.THEME -> SpaceIcon.THEME
    ToolbarItem.MODE -> SpaceIcon.MODE
    ToolbarItem.SYMBOLS -> SpaceIcon.SYMBOLS
    ToolbarItem.EMOJI -> SpaceIcon.EMOJI
    ToolbarItem.DECORATE -> SpaceIcon.DECORATE
    ToolbarItem.CLIPBOARD -> SpaceIcon.CLIPBOARD
    ToolbarItem.TRANSLATE -> SpaceIcon.TRANSLATE
    ToolbarItem.IMAGES -> SpaceIcon.IMAGES
    ToolbarItem.VOICE -> SpaceIcon.VOICE
    ToolbarItem.EDIT -> SpaceIcon.EDIT
    ToolbarItem.INCOGNITO -> SpaceIcon.INCOGNITO
    ToolbarItem.SETTINGS -> SpaceIcon.SETTINGS
}

@Composable
internal fun ToolbarIcon(icon: SpaceIcon, color: Color, modifier: Modifier = Modifier) {
    SpaceIconView(icon, color, modifier, ICON_DP.dp)
}

private const val ICON_DP = 22
