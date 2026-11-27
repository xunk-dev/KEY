package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.PanelMode
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
    ToolbarItem.EDIT -> SpaceIcon.EDIT
    ToolbarItem.RESIZE -> SpaceIcon.RESIZE
    ToolbarItem.INCOGNITO -> SpaceIcon.INCOGNITO
    ToolbarItem.SETTINGS -> SpaceIcon.SETTINGS
}

internal fun PanelMode.icon(): SpaceIcon = when (this) {
    PanelMode.NORMAL -> SpaceIcon.MODE_NORMAL
    PanelMode.ONE_HAND_RIGHT -> SpaceIcon.MODE_ONE_RIGHT
    PanelMode.ONE_HAND_LEFT -> SpaceIcon.MODE_ONE_LEFT
    PanelMode.SPLIT -> SpaceIcon.MODE_SPLIT
    PanelMode.FLOAT -> SpaceIcon.FLOAT
}

@Composable
internal fun ToolbarIcon(icon: SpaceIcon, color: Color, modifier: Modifier = Modifier, accent: Color = color) {
    SpaceIconView(icon, color, modifier, ICON_DP.dp, accent)
}

private const val ICON_DP = 24
