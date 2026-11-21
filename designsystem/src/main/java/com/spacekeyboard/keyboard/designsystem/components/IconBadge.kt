package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.BadgeShape
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun IconBadge(
    icon: SpaceIcon,
    tone: BadgeTone,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 24.dp,
) {
    val look = SpaceTheme.look
    val colors = look.badges.getValue(tone)
    if (look.badgeShape == BadgeShape.None) {
        SpaceIconView(icon, colors.tint, modifier, iconSize)
        return
    }
    val shape: Shape = if (look.badgeShape == BadgeShape.Circle) CircleShape else RoundedCornerShape(look.badgeRadius * size.value / 42f)
    Box(
        modifier = modifier.size(size).clip(shape).background(colors.fill),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, colors.tint, size = iconSize)
    }
}
