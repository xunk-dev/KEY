package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import com.spacekeyboard.keyboard.emoji.ui.PanelIconView
import com.spacekeyboard.keyboard.emoji.ui.PanelKey

@Composable
fun ClipKey(
    text: String,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    description: String = text,
    locked: Boolean = false,
    icon: PanelIcon? = null,
    onClick: () -> Unit,
) {
    PanelKey(palette, description, modifier.height(48.dp).widthIn(min = 48.dp), selected = selected, onClick = onClick) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (locked) PanelIconView(PanelIcon.LOCK, Color(palette.keyText), Modifier.size(14.dp))
            if (icon != null) PanelIconView(icon, Color(palette.keyText), Modifier.padding(end = 4.dp).size(16.dp))
            SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.keyText))
        }
    }
}

@Composable
fun ClipIconKey(icon: PanelIcon, description: String, palette: ThemePalette, onClick: () -> Unit) {
    PanelKey(palette, description, Modifier.size(48.dp), onClick = onClick) {
        PanelIconView(icon, Color(palette.keyText))
    }
}
