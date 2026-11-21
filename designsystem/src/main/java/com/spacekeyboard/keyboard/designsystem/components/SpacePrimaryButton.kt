package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.glass.spaceGlow
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private val BUTTON_HEIGHT = 48.dp
private val COMPACT_HEIGHT = 40.dp
private val BUTTON_RADIUS = 15.dp
private const val GLOW_ALPHA = 0.4f

@Composable
fun SpacePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: SpaceIcon? = null,
    compact: Boolean = false,
) {
    val look = SpaceTheme.look
    val shape: Shape = if (compact) CircleShape else RoundedCornerShape(if (look.look == SpaceLook.Day) 24.dp else BUTTON_RADIUS)
    val glowing = look.look == SpaceLook.Galaxy && !compact
    Row(
        modifier = modifier
            .then(if (compact) Modifier else Modifier.fillMaxWidth())
            .height(if (compact) COMPACT_HEIGHT else BUTTON_HEIGHT)
            .then(if (glowing) Modifier.spaceGlow(look.glow, { GLOW_ALPHA }, 10.dp, { 0.3f }) else Modifier)
            .clip(shape)
            .background(look.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 16.dp else 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) SpaceIconView(icon, look.onPrimary, size = 20.dp)
        val base = if (compact) SpaceTheme.typography.label.copy(fontSize = SpaceTheme.typography.bodySmall.fontSize) else SpaceTheme.typography.titleSmall
        SpaceText(text, base, color = look.onPrimary)
    }
}
