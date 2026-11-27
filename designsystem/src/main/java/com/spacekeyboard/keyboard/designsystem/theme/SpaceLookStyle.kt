package com.spacekeyboard.keyboard.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BadgeShape { Squircle, Circle, None }

enum class BadgeTone { Look, Tool, Help }

enum class HeroKind { Card, Banner, Plain }

@Immutable
class BadgeColors(val fill: Color, val tint: Color)

@Immutable
class SpaceLookStyle(
    val look: SpaceLook,
    val canvas: Brush,
    val card: Color,
    val cardEdge: Color,
    val cardEdgeWidth: Dp,
    val cardRadius: Dp,
    val glow: Color,
    val field: Color,
    val fieldEdge: Color,
    val fieldEdgeWidth: Dp,
    val fieldRadius: Dp,
    val searchFill: Color,
    val searchEdge: Color,
    val searchRadius: Dp,
    val searchHeight: Dp,
    val badgeShape: BadgeShape,
    val badgeRadius: Dp,
    val badges: Map<BadgeTone, BadgeColors>,
    val primary: Brush,
    val onPrimary: Color,
    val chevron: Color,
    val heading: Color,
    val headingSize: TextUnit,
    val headingWeight: FontWeight,
    val screenPadding: Dp,
    val sectionGap: Dp,
    val rowHeight: Dp,
    val tiles: Boolean,
    val hero: HeroKind,
    val heroFill: Color,
    val heroRadius: Dp,
)

private object GalaxyCanvas : ShaderBrush() {
    private val colors = listOf(Color(0xFF2B2048), Color(0xFF150E28), Color(0xFF0B0716))
    private val stops = listOf(0f, 0.55f, 1f)
    private const val ORIGIN_X = 0.9f
    private const val REACH = 1.3f

    override fun createShader(size: Size): Shader =
        RadialGradientShader(Offset(size.width * ORIGIN_X, 0f), size.width * REACH, colors, stops)
}

private val galaxyTone = BadgeColors(Color(0x2EB26BFF), Color(0xFFD8C2FF))

private val galaxy = SpaceLookStyle(
    look = SpaceLook.Galaxy,
    canvas = GalaxyCanvas,
    card = Color.White.copy(alpha = 0.06f),
    cardEdge = Color(0x42B26BFF),
    cardEdgeWidth = 1.dp,
    cardRadius = 20.dp,
    glow = Color(0xFFB26BFF),
    field = Color.White.copy(alpha = 0.05f),
    fieldEdge = Color(0x3DB26BFF),
    fieldEdgeWidth = 1.dp,
    fieldRadius = 18.dp,
    searchFill = Color.White.copy(alpha = 0.05f),
    searchEdge = Color(0x3DB26BFF),
    searchRadius = 16.dp,
    searchHeight = 48.dp,
    badgeShape = BadgeShape.Squircle,
    badgeRadius = 13.dp,
    badges = BadgeTone.entries.associateWith { galaxyTone },
    primary = Brush.linearGradient(listOf(SpaceColors.GalaxyPrimary, SpaceColors.GalaxyPrimaryEnd)),
    onPrimary = SpaceColors.GalaxyOnPrimary,
    chevron = Color(0xFFA99DC6),
    heading = Color(0xFFE9E0FF),
    headingSize = 18.sp,
    headingWeight = FontWeight.SemiBold,
    screenPadding = 20.dp,
    sectionGap = 18.dp,
    rowHeight = 64.dp,
    tiles = true,
    hero = HeroKind.Card,
    heroFill = Color.White.copy(alpha = 0.06f),
    heroRadius = 22.dp,
)

private val day = SpaceLookStyle(
    look = SpaceLook.Day,
    canvas = SolidColor(SpaceColors.DayBackground),
    card = SpaceColors.DaySurface,
    cardEdge = Color.Transparent,
    cardEdgeWidth = 0.dp,
    cardRadius = 26.dp,
    glow = Color.Transparent,
    field = SpaceColors.DaySurface,
    fieldEdge = SpaceColors.DayOutline,
    fieldEdgeWidth = 1.5.dp,
    fieldRadius = 24.dp,
    searchFill = SpaceColors.DayContainer,
    searchEdge = Color.Transparent,
    searchRadius = 26.dp,
    searchHeight = 52.dp,
    badgeShape = BadgeShape.Circle,
    badgeRadius = 22.dp,
    badges = mapOf(
        BadgeTone.Look to BadgeColors(Color(0xFFD6E4FF), SpaceColors.DayPrimary),
        BadgeTone.Tool to BadgeColors(Color(0xFFD3F0E9), SpaceColors.DaySuccess),
        BadgeTone.Help to BadgeColors(Color(0xFFFBE6C8), SpaceColors.DayWarning),
    ),
    primary = SolidColor(SpaceColors.DayPrimary),
    onPrimary = SpaceColors.DayOnPrimary,
    chevron = Color(0xFF5A6580),
    heading = Color(0xFF3C4866),
    headingSize = 14.sp,
    headingWeight = FontWeight.Medium,
    screenPadding = 16.dp,
    sectionGap = 16.dp,
    rowHeight = 70.dp,
    tiles = false,
    hero = HeroKind.Banner,
    heroFill = SpaceColors.DayPrimaryContainer,
    heroRadius = 28.dp,
)

private val eclipse = SpaceLookStyle(
    look = SpaceLook.Eclipse,
    canvas = SolidColor(SpaceColors.EclipseBackground),
    card = SpaceColors.EclipseSurface,
    cardEdge = Color.Transparent,
    cardEdgeWidth = 0.dp,
    cardRadius = 16.dp,
    glow = Color.Transparent,
    field = SpaceColors.EclipseSurface,
    fieldEdge = SpaceColors.EclipseOutline,
    fieldEdgeWidth = 1.dp,
    fieldRadius = 16.dp,
    searchFill = SpaceColors.EclipseSurface,
    searchEdge = Color.Transparent,
    searchRadius = 12.dp,
    searchHeight = 46.dp,
    badgeShape = BadgeShape.None,
    badgeRadius = 0.dp,
    badges = BadgeTone.entries.associateWith { BadgeColors(Color.Transparent, SpaceColors.EclipsePrimary) },
    primary = SolidColor(SpaceColors.EclipsePrimary),
    onPrimary = SpaceColors.EclipseOnPrimary,
    chevron = Color(0xFF8F897B),
    heading = SpaceColors.EclipseOnSurfaceVariant,
    headingSize = 13.sp,
    headingWeight = FontWeight.Medium,
    screenPadding = 18.dp,
    sectionGap = 22.dp,
    rowHeight = 64.dp,
    tiles = false,
    hero = HeroKind.Plain,
    heroFill = Color.Transparent,
    heroRadius = 16.dp,
)

fun lookStyleFor(look: SpaceLook): SpaceLookStyle = when (look) {
    SpaceLook.Galaxy -> galaxy
    SpaceLook.Day -> day
    SpaceLook.Eclipse -> eclipse
}
