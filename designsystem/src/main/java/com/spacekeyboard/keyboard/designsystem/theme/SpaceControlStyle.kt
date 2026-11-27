package com.spacekeyboard.keyboard.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.glass.spaceGlow

@Composable
fun Modifier.spaceControl(
    shape: Shape,
    selected: Boolean = false,
    tint: Color = SpaceTheme.colors.primary,
    glowAlpha: Float = if (selected) 1f else 0f,
    glowSpread: Dp = 10.dp,
    cornerFraction: Float = 0.5f,
    solid: Boolean = false
): Modifier {
    val c = SpaceTheme.colors
    val dark = c.isDark
    val fill: Brush = when {
        solid && c.neutral -> Brush.linearGradient(if (dark) GraphiteFabDark else GraphiteFab)
        solid -> Brush.linearGradient(listOf(lerp(tint, Color.White, 0.18f), tint, lerp(tint, c.accentB, 0.35f)))
        selected && c.neutral && dark -> Brush.verticalGradient(listOf(tint.copy(alpha = 0.18f), tint.copy(alpha = 0.05f)))
        selected -> Brush.verticalGradient(listOf(tint.copy(alpha = if (dark) 0.30f else 0.20f), tint.copy(alpha = if (dark) 0.14f else 0.08f)))
        else -> spaceCardBrush()
    }
    val edge: Brush = when {
        solid && c.neutral -> Brush.verticalGradient(if (dark) listOf(Color.White.copy(alpha = 0.90f), Color.White.copy(alpha = 0.22f)) else listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.04f)))
        solid -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.10f)))
        selected && c.neutral -> Brush.verticalGradient(listOf(tint.copy(alpha = 0.95f), tint.copy(alpha = 0.60f)))
        selected -> Brush.verticalGradient(listOf(tint.copy(alpha = 0.70f), tint.copy(alpha = 0.26f)))
        else -> spaceCardEdge()
    }
    val glowBase = if (dark) 0.26f else 0.16f
    return this
        .then(if (glowAlpha > 0f) Modifier.spaceGlow(tint, { glowBase * glowAlpha }, glowSpread, { cornerFraction }) else Modifier)
        .background(fill, shape)
        .border(1.dp, edge, shape)
}
