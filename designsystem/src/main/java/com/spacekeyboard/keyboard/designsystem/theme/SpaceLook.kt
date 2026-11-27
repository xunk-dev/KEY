package com.spacekeyboard.keyboard.designsystem.theme

import androidx.compose.ui.graphics.Color

enum class SpaceLook(val key: String, val dark: Boolean) {
    Galaxy("galaxy", true),
    Day("day", false),
    Eclipse("eclipse", true);

    companion object {
        fun fromKey(key: String?): SpaceLook? = entries.firstOrNull { it.key == key }

        fun forSystem(systemDark: Boolean): SpaceLook = if (systemDark) Galaxy else Day
    }
}

fun lookColorScheme(look: SpaceLook): SpaceColorScheme = when (look) {
    SpaceLook.Galaxy -> galaxyScheme()
    SpaceLook.Day -> dayScheme()
    SpaceLook.Eclipse -> eclipseScheme()
}

private fun galaxyScheme() = SpaceColorScheme(
    background = SpaceColors.GalaxyBackground,
    surface = SpaceColors.GalaxySurface,
    container = SpaceColors.GalaxyContainer,
    containerHigh = SpaceColors.GalaxyContainerHigh,
    outline = SpaceColors.GalaxyOutline,
    divider = Color.White.copy(alpha = 0.09f),
    onSurface = SpaceColors.GalaxyOnSurface,
    onSurfaceVariant = SpaceColors.GalaxyOnSurfaceVariant,
    primary = SpaceColors.GalaxyPrimary,
    onPrimary = SpaceColors.GalaxyOnPrimary,
    primaryContainer = SpaceColors.GalaxyPrimaryContainer,
    success = SpaceColors.GalaxySuccess,
    warning = SpaceColors.DarkWarning,
    error = SpaceColors.DarkError,
    accentA = Color(0xFFB26BFF),
    accentB = SpaceColors.GalaxyPrimary,
    accentC = SpaceColors.GalaxyPrimaryEnd,
    onDarkAccent = SpaceColors.GalaxyPrimary,
    isDark = true,
)

private fun dayScheme() = SpaceColorScheme(
    background = SpaceColors.DayBackground,
    surface = SpaceColors.DaySurface,
    container = SpaceColors.DayContainer,
    containerHigh = SpaceColors.DayContainerHigh,
    outline = SpaceColors.DayOutline,
    divider = Color(0xFFE6EAF3),
    onSurface = SpaceColors.DayOnSurface,
    onSurfaceVariant = SpaceColors.DayOnSurfaceVariant,
    primary = SpaceColors.DayPrimary,
    onPrimary = SpaceColors.DayOnPrimary,
    primaryContainer = SpaceColors.DayPrimaryContainer,
    success = SpaceColors.DaySuccess,
    warning = SpaceColors.DayWarning,
    error = SpaceColors.LightError,
    accentA = SpaceColors.DayPrimary,
    accentB = Color(0xFF5B8DEF),
    accentC = SpaceColors.DaySuccess,
    onDarkAccent = SpaceColors.GalaxyPrimary,
    isDark = false,
)

private fun eclipseScheme() = SpaceColorScheme(
    background = SpaceColors.EclipseBackground,
    surface = SpaceColors.EclipseSurface,
    container = SpaceColors.EclipseContainer,
    containerHigh = SpaceColors.EclipseContainerHigh,
    outline = SpaceColors.EclipseOutline,
    divider = SpaceColors.EclipseOutline,
    onSurface = SpaceColors.EclipseOnSurface,
    onSurfaceVariant = SpaceColors.EclipseOnSurfaceVariant,
    primary = SpaceColors.EclipsePrimary,
    onPrimary = SpaceColors.EclipseOnPrimary,
    primaryContainer = SpaceColors.EclipsePrimaryContainer,
    success = SpaceColors.DarkSuccess,
    warning = SpaceColors.DarkWarning,
    error = SpaceColors.DarkError,
    accentA = SpaceColors.EclipsePrimary,
    accentB = Color(0xFFE8B24C),
    accentC = SpaceColors.EclipseOnSurfaceVariant,
    onDarkAccent = SpaceColors.EclipsePrimary,
    isDark = true,
)
