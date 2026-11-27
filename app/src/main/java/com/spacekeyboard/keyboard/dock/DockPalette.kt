package com.spacekeyboard.keyboard.dock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.spacekeyboard.keyboard.designsystem.theme.SpaceColorScheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Immutable
internal class DockPalette(
    val container: Color,
    val border: Color,
    val idle: Color,
    val selectedIcon: Color,
    val selectedLabel: Color,
    val gradientTop: Color,
    val gradientBottom: Color,
    val glow: Color,
    val ring: Color,
    val onAccent: Color,
    val sheenTop: Color,
    val sheenBottom: Color,
    val rimTop: Color,
    val rimBottom: Color,
    val pill: Color,
    val pillRimTop: Color,
    val pillRim: Color,
)

@Composable
internal fun rememberDockPalette(): DockPalette {
    val colors = SpaceTheme.colors
    return remember(colors) { dockPalette(colors) }
}

internal fun dockPalette(c: SpaceColorScheme): DockPalette {
    val top = lerp(c.accentB, Color.White, 0.12f)
    val bottom = lerp(c.accentA, Color.Black, 0.38f)
    val dark = c.isDark
    return DockPalette(
        container = if (dark) lerp(c.surface, c.accentC, 0.2f).copy(alpha = 0.36f) else c.surface.copy(alpha = 0.62f),
        border = if (dark) Color.White.copy(alpha = 0.16f) else c.accentA.copy(alpha = 0.25f),
        idle = c.onSurfaceVariant,
        selectedIcon = if (dark) top else bottom,
        selectedLabel = c.onSurface,
        gradientTop = top,
        gradientBottom = bottom,
        glow = c.accentA.copy(alpha = if (dark) 0.5f else 0.42f),
        ring = Color.White.copy(alpha = 0.4f),
        onAccent = Color.White,
        sheenTop = Color.White.copy(alpha = if (dark) 0.13f else 0.55f),
        sheenBottom = Color.White.copy(alpha = if (dark) 0.02f else 0.1f),
        rimTop = Color.White.copy(alpha = if (dark) 0.30f else 0.9f),
        rimBottom = if (dark) Color.White.copy(alpha = 0.05f) else c.accentA.copy(alpha = 0.05f),
        pill = if (dark) Color.White.copy(alpha = 0.09f) else c.accentA.copy(alpha = 0.12f),
        pillRimTop = Color.White.copy(alpha = if (dark) 0.22f else 0.7f),
        pillRim = if (dark) Color.White.copy(alpha = 0.08f) else c.accentA.copy(alpha = 0.1f),
    )
}
