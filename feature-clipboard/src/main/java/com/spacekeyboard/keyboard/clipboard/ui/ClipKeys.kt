package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
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
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
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
    spaceIcon: SpaceIcon? = null,
    onClick: () -> Unit,
) {
    PanelKey(palette, description, modifier.height(48.dp).widthIn(min = 48.dp), selected = selected, onClick = onClick) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (locked) ClipIcon(SpaceIcon.LOCK, Color(palette.keyText), 14.dp, Modifier.padding(end = 4.dp))
            if (spaceIcon != null) ClipIcon(spaceIcon, Color(palette.keyText), 18.dp, Modifier.padding(end = 4.dp))
            if (icon != null) PanelIconView(icon, Color(palette.keyText), Modifier.padding(end = 4.dp).size(16.dp))
            SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.keyText))
        }
    }
}

@Composable
fun ClipCapsuleKey(
    text: String,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    spaceIcon: SpaceIcon? = null,
    onClick: () -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val tint = if (primary) palette.glow else palette.label
    val fill = if (primary) 0.34f to 0.14f else 0.14f to 0.05f
    val edge = if (primary) 0.75f to 0.35f else 0.32f to 0.10f
    val ink = Color(if (primary) palette.glow else palette.keyText)
    val glow = if (primary) Modifier.shadow(10.dp, CircleShape, false, Color(palette.glow), Color(palette.glow)) else Modifier
    Row(
        modifier
            .height(48.dp)
            .widthIn(min = 48.dp)
            .then(glow)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(ColorMath.withAlpha(tint, fill.first)), Color(ColorMath.withAlpha(tint, fill.second)))))
            .border(1.dp, Brush.verticalGradient(listOf(Color(ColorMath.withAlpha(tint, edge.first)), Color(ColorMath.withAlpha(tint, edge.second)))), CircleShape)
            .semantics { contentDescription = text }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            }
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (spaceIcon != null) ClipIcon(spaceIcon, ink, 18.dp, Modifier.padding(end = 5.dp))
        SpaceText(text, SpaceTheme.typography.bodySmall, color = ink, maxLines = 1)
    }
}

@Composable
fun ClipIconKey(icon: PanelIcon, description: String, palette: ThemePalette, onClick: () -> Unit) {
    PanelKey(palette, description, Modifier.size(48.dp), onClick = onClick) {
        PanelIconView(icon, Color(palette.keyText))
    }
}

internal val ClipDanger = Color(0xFFFF9AA8)
