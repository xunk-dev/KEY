package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.glass.spaceGlow
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val GLOW_ALPHA = 0.18f
private val GLOW_SPREAD = 12.dp

@Composable
fun LookCard(
    modifier: Modifier = Modifier,
    radius: Dp = SpaceTheme.look.cardRadius,
    fill: Color = SpaceTheme.look.card,
    edge: Color = SpaceTheme.look.cardEdge,
    edgeWidth: Dp = SpaceTheme.look.cardEdgeWidth,
    glow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val look = SpaceTheme.look
    val shape = RoundedCornerShape(radius)
    val glowing = glow && look.glow != Color.Transparent
    Column(
        modifier = modifier
            .then(if (glowing) Modifier.spaceGlow(look.glow, { GLOW_ALPHA }, GLOW_SPREAD) else Modifier)
            .clip(shape)
            .background(fill)
            .then(if (edgeWidth > 0.dp) Modifier.border(edgeWidth, edge, shape) else Modifier),
        content = content,
    )
}
