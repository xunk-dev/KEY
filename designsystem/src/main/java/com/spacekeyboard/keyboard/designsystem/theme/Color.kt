package com.spacekeyboard.keyboard.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private const val LightSurfaceTint = 0.05f

object SpaceColors {
    val DarkBackground = Color(0xFF0A0E0D)
    val DarkSurface = Color(0xFF101514)
    val DarkContainer = Color(0xFF161C1B)
    val DarkContainerHigh = Color(0xFF1E2625)
    val DarkOutline = Color(0xFF33403E)
    val DarkOnSurface = Color(0xFFE9EFED)
    val DarkOnSurfaceVariant = Color(0xFFA7B4B0)
    val DarkPrimary = Color(0xFF9A9DFF)
    val DarkOnPrimary = Color(0xFF0B0A5C)
    val DarkPrimaryContainer = Color(0xFF2A2C7A)
    val DarkSuccess = Color(0xFF3DDC97)
    val DarkWarning = Color(0xFFFFB84D)
    val DarkError = Color(0xFFFF7078)
    val ToastError = Color(0xFFFF453A)

    val LightBackground = Color(0xFFE6E8F1)
    val LightSurface = Color(0xFFF2F3F9)
    val LightContainer = Color(0xFFDADDEA)
    val LightContainerHigh = Color(0xFFCED2E3)
    val LightOutline = Color(0xFF9096B0)
    val LightOnSurface = Color(0xFF191B28)
    val LightOnSurfaceVariant = Color(0xFF4E5268)
    val LightPrimary = Color(0xFF4347C9)
    val LightOnPrimary = Color(0xFFF7F7FF)
    val LightPrimaryContainer = Color(0xFFD5D6F8)
    val LightSuccess = Color(0xFF0B6B4D)
    val LightWarning = Color(0xFF9A4A06)
    val LightError = Color(0xFFB3261E)

    val GraphiteBackground = Color(0xFF080808)
    val GraphiteSurface = Color(0xFF0E0E0E)
    val GraphiteContainer = Color(0xFF141414)
    val GraphiteContainerHigh = Color(0xFF1E1E1E)
    val GraphiteOutline = Color(0xFF6E6E6E)
    val GraphiteDivider = Color(0xFF2C2C2C)

    val AmoledBackground = Color(0xFF000000)
    val NeutralBackground = Color(0xFF000000)
    val NeutralSurface = Color(0xFF0A0A0A)
    val NeutralContainer = Color(0xFF141414)
    val NeutralContainerHigh = Color(0xFF1F1F1F)
    val NeutralOutline = Color(0xFF6E6E6E)
    val NeutralDivider = Color(0xFF333333)

    val NeutralOnSurface = Color(0xFFECECEC)
    val NeutralOnSurfaceVariant = Color(0xFFABABAB)

    val NeutralLightBackground = Color(0xFFFFFFFF)
    val NeutralLightSurface = Color(0xFFFFFFFF)
    val NeutralLightContainer = Color(0xFFF2F2F2)
    val NeutralLightContainerHigh = Color(0xFFE6E6E6)
    val NeutralLightOutline = Color(0xFF000000)
    val NeutralLightOnSurface = Color(0xFF000000)
    val NeutralLightOnSurfaceVariant = Color(0xFF555555)
    val ViewerBlack = Color(0xFF000000)

    val GalaxyBackground = Color(0xFF0B0716)
    val GalaxySurface = Color(0xFF150E28)
    val GalaxyContainer = Color(0xFF1B1433)
    val GalaxyContainerHigh = Color(0xFF2B2048)
    val GalaxyOutline = Color(0xFF4A3B73)
    val GalaxyOnSurface = Color(0xFFF3ECFF)
    val GalaxyOnSurfaceVariant = Color(0xFFBDB2D6)
    val GalaxyPrimary = Color(0xFFC48BFF)
    val GalaxyPrimaryEnd = Color(0xFF8E64E6)
    val GalaxyOnPrimary = Color(0xFF12082A)
    val GalaxyPrimaryContainer = Color(0xFF3A2766)
    val GalaxySuccess = Color(0xFF6EEBB4)

    val DayBackground = Color(0xFFEEF1F8)
    val DaySurface = Color(0xFFFFFFFF)
    val DayContainer = Color(0xFFDDE3F0)
    val DayContainerHigh = Color(0xFFCFDFFF)
    val DayOutline = Color(0xFFC6CFE2)
    val DayOnSurface = Color(0xFF151B2B)
    val DayOnSurfaceVariant = Color(0xFF4A5672)
    val DayPrimary = Color(0xFF1F5FE0)
    val DayOnPrimary = Color(0xFFFFFFFF)
    val DayPrimaryContainer = Color(0xFFCFDFFF)
    val DaySuccess = Color(0xFF0B6B5C)
    val DayWarning = Color(0xFF8A4B00)

    val EclipseBackground = Color(0xFF000000)
    val EclipseSurface = Color(0xFF111113)
    val EclipseContainer = Color(0xFF18181B)
    val EclipseContainerHigh = Color(0xFF222226)
    val EclipseOutline = Color(0xFF26231C)
    val EclipseOnSurface = Color(0xFFF2EEE4)
    val EclipseOnSurfaceVariant = Color(0xFFB4AEA0)
    val EclipsePrimary = Color(0xFFFFD27A)
    val EclipseOnPrimary = Color(0xFF1A1405)
    val EclipsePrimaryContainer = Color(0xFF4A3D1C)
}

data class SpaceColorScheme(
    val background: Color,
    val surface: Color,
    val container: Color,
    val containerHigh: Color,
    val outline: Color,
    val divider: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val accentA: Color,
    val accentB: Color,
    val accentC: Color,
    val onDarkAccent: Color,
    val isDark: Boolean,
    val neutral: Boolean = false
)

fun darkColorScheme(amoled: Boolean = false, accent: SpaceAccent = SpaceAccents.Indigo): SpaceColorScheme {
    val neutral = accent.neutral
    fun grey(c: Color): Color { val g = c.red * 0.2126f + c.green * 0.7152f + c.blue * 0.0722f; return Color(g, g, g, c.alpha) }
    fun tint(base: Color) = lerp(grey(base), accent.darkContainer, accent.surfaceTint)
    fun ramp(base: Color, graphite: Color, black: Color) = when {
        !neutral -> tint(base)
        amoled -> black
        else -> graphite
    }
    return SpaceColorScheme(
        background = when {
            amoled -> SpaceColors.AmoledBackground
            neutral -> SpaceColors.GraphiteBackground
            else -> tint(SpaceColors.DarkBackground)
        },
        surface = ramp(SpaceColors.DarkSurface, SpaceColors.GraphiteSurface, SpaceColors.NeutralSurface),
        container = ramp(SpaceColors.DarkContainer, SpaceColors.GraphiteContainer, SpaceColors.NeutralContainer),
        containerHigh = ramp(SpaceColors.DarkContainerHigh, SpaceColors.GraphiteContainerHigh, SpaceColors.NeutralContainerHigh),
        outline = ramp(SpaceColors.DarkOutline, SpaceColors.GraphiteOutline, SpaceColors.NeutralOutline),
        divider = ramp(SpaceColors.DarkOutline, SpaceColors.GraphiteDivider, SpaceColors.NeutralDivider),
        onSurface = if (neutral) SpaceColors.NeutralOnSurface else SpaceColors.DarkOnSurface,
        onSurfaceVariant = if (neutral) SpaceColors.NeutralOnSurfaceVariant else SpaceColors.DarkOnSurfaceVariant,
        primary = accent.darkPrimary,
        onPrimary = accent.darkOnPrimary,
        primaryContainer = accent.darkContainer,
        success = SpaceColors.DarkSuccess,
        warning = SpaceColors.DarkWarning,
        error = SpaceColors.DarkError,
        accentA = accent.a,
        accentB = accent.b,
        accentC = accent.c,
        onDarkAccent = accent.darkPrimary,
        isDark = true,
        neutral = neutral
    )
}

fun lightColorScheme(accent: SpaceAccent = SpaceAccents.Indigo): SpaceColorScheme {
    val neutral = accent.neutral
    fun tint(base: Color) = lerp(base, accent.a, LightSurfaceTint)
    return SpaceColorScheme(
        background = if (neutral) SpaceColors.NeutralLightBackground else tint(SpaceColors.LightBackground),
        surface = if (neutral) SpaceColors.NeutralLightSurface else tint(SpaceColors.LightSurface),
        container = if (neutral) SpaceColors.NeutralLightContainer else tint(SpaceColors.LightContainer),
        containerHigh = if (neutral) SpaceColors.NeutralLightContainerHigh else tint(SpaceColors.LightContainerHigh),
        outline = if (neutral) SpaceColors.NeutralLightOutline.copy(alpha = 0.22f) else SpaceColors.LightOutline.copy(alpha = 0.32f),
        divider = if (neutral) SpaceColors.NeutralLightOutline.copy(alpha = 0.14f) else SpaceColors.LightOutline.copy(alpha = 0.32f),
        onSurface = if (neutral) SpaceColors.NeutralLightOnSurface else SpaceColors.LightOnSurface,
        onSurfaceVariant = if (neutral) SpaceColors.NeutralLightOnSurfaceVariant else SpaceColors.LightOnSurfaceVariant,
        primary = accent.lightPrimary,
        onPrimary = accent.lightOnPrimary,
        primaryContainer = accent.lightContainer,
        success = SpaceColors.LightSuccess,
        warning = SpaceColors.LightWarning,
        error = SpaceColors.LightError,
        accentA = accent.lightA,
        accentB = accent.lightB,
        accentC = accent.lightC,
        onDarkAccent = accent.darkPrimary,
        isDark = false,
        neutral = accent.neutral
    )
}

fun lerpScheme(a: SpaceColorScheme, b: SpaceColorScheme, t: Float): SpaceColorScheme = when {
    t <= 0f -> a
    t >= 1f -> b
    else -> SpaceColorScheme(
        background = lerp(a.background, b.background, t),
        surface = lerp(a.surface, b.surface, t),
        container = lerp(a.container, b.container, t),
        containerHigh = lerp(a.containerHigh, b.containerHigh, t),
        outline = lerp(a.outline, b.outline, t),
        divider = lerp(a.divider, b.divider, t),
        onSurface = lerp(a.onSurface, b.onSurface, t),
        onSurfaceVariant = lerp(a.onSurfaceVariant, b.onSurfaceVariant, t),
        primary = lerp(a.primary, b.primary, t),
        onPrimary = lerp(a.onPrimary, b.onPrimary, t),
        primaryContainer = lerp(a.primaryContainer, b.primaryContainer, t),
        success = lerp(a.success, b.success, t),
        warning = lerp(a.warning, b.warning, t),
        error = lerp(a.error, b.error, t),
        accentA = lerp(a.accentA, b.accentA, t),
        accentB = lerp(a.accentB, b.accentB, t),
        accentC = lerp(a.accentC, b.accentC, t),
        onDarkAccent = lerp(a.onDarkAccent, b.onDarkAccent, t),
        isDark = if (t < 0.5f) a.isDark else b.isDark,
        neutral = if (t < 0.5f) a.neutral else b.neutral
    )
}

val SpaceColorScheme.fieldFill: Color
    get() = if (isDark) Color.White.copy(alpha = 0.06f) else container
