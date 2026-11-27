package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.a11y.spaceHoldRepeat
import com.spacekeyboard.keyboard.designsystem.glass.spaceGlow
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.theme.SpaceFontFamily

internal class CalcLook(val palette: ThemePalette, val style: KeyStyle, val vibrate: Boolean)

internal enum class CalcKind { DIGIT, FUNCTION, OPERATOR, EQUALS, DELETE }

private class CalcFace(val brush: Brush, val border: Color, val ink: Color, val glow: Color?)

private val KEY_SHAPE = RoundedCornerShape(16.dp)
private val GLOW_SPREAD = 10.dp
private const val CORNER_FRACTION = 0.36f
private const val GLOW_ALPHA = 0.5f
private const val DISABLED_ALPHA = 0.38f
private const val DARK_INK = 0xFF12082A.toInt()
private val ROSE_NIGHT = Color(0xFFFF7896)
private val ROSE_DAY = Color(0xFFD64560)
private val ROSE_INK_NIGHT = Color(0xFFFFB3C0)
private val ROSE_INK_DAY = Color(0xFFA3213A)
private const val LIGHT_BACKGROUND = 0.4

private fun onLightTheme(look: CalcLook): Boolean = ColorMath.luminance(look.palette.background) > LIGHT_BACKGROUND

internal fun roseInk(look: CalcLook): Color = if (onLightTheme(look)) ROSE_INK_DAY else ROSE_INK_NIGHT

private fun vertical(top: Int, bottom: Int): Brush = Brush.verticalGradient(listOf(Color(top), Color(bottom)))

private fun plainFace(p: ThemePalette, ink: Color, selected: Boolean): CalcFace {
    val top = if (selected) ColorMath.withAlpha(p.glow, 0.34f) else ColorMath.mix(p.background, p.label, 0.17f)
    val bottom = if (selected) ColorMath.withAlpha(p.glow, 0.16f) else ColorMath.mix(p.background, p.label, 0.06f)
    val border = if (selected) Color(p.glow) else Color(ColorMath.withAlpha(p.label, 0.16f))
    return CalcFace(vertical(top, bottom), border, if (selected) Color(p.label) else ink, null)
}

private fun operatorFace(p: ThemePalette): CalcFace {
    val top = ColorMath.mix(p.background, p.glow, 0.5f)
    val bottom = ColorMath.mix(p.background, p.glow, 0.28f)
    val border = ColorMath.withAlpha(ColorMath.mix(p.glow, p.label, 0.35f), 0.6f)
    val ink = ColorMath.readableOn(top, listOf(p.label, ColorMath.WHITE))
    return CalcFace(vertical(top, bottom), Color(border), Color(ink), null)
}

private fun equalsFace(p: ThemePalette): CalcFace {
    val top = ColorMath.mix(p.glow, ColorMath.WHITE, 0.38f)
    val border = ColorMath.mix(p.glow, ColorMath.WHITE, 0.6f)
    val ink = ColorMath.readableOn(p.glow, listOf(DARK_INK, ColorMath.WHITE))
    return CalcFace(vertical(top, p.glow), Color(border), Color(ink), Color(p.glow))
}

private fun deleteFace(look: CalcLook): CalcFace {
    val rose = if (onLightTheme(look)) ROSE_DAY else ROSE_NIGHT
    val brush = Brush.verticalGradient(listOf(rose.copy(alpha = 0.3f), rose.copy(alpha = 0.1f)))
    return CalcFace(brush, rose.copy(alpha = 0.45f), roseInk(look), null)
}

private fun calcFace(kind: CalcKind, look: CalcLook, selected: Boolean): CalcFace {
    val p = look.palette
    return when (kind) {
        CalcKind.DIGIT -> plainFace(p, Color(p.label), selected)
        CalcKind.FUNCTION -> plainFace(p, Color(p.glow), selected)
        CalcKind.OPERATOR -> operatorFace(p)
        CalcKind.EQUALS -> equalsFace(p)
        CalcKind.DELETE -> deleteFace(look)
    }
}

private fun textSize(kind: CalcKind): Int = when (kind) {
    CalcKind.DIGIT, CalcKind.DELETE -> 22
    CalcKind.FUNCTION -> 17
    CalcKind.OPERATOR -> 25
    CalcKind.EQUALS -> 28
}

@Composable
internal fun CalcKeyText(text: String, kind: CalcKind, ink: Color) {
    BasicText(
        text = text,
        style = TextStyle(
            color = ink,
            fontSize = textSize(kind).sp,
            fontWeight = if (kind == CalcKind.EQUALS) FontWeight.SemiBold else FontWeight.Medium,
            fontFamily = SpaceFontFamily,
        ),
        maxLines = 1,
    )
}

@Composable
internal fun CalcKeyView(
    kind: CalcKind,
    look: CalcLook,
    modifier: Modifier,
    description: String?,
    shape: Shape = KEY_SHAPE,
    selected: Boolean = false,
    enabled: Boolean = true,
    repeat: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val face = calcFace(kind, look, selected)
    val press = if (repeat) {
        Modifier.spaceHoldRepeat(onClick)
    } else {
        Modifier.clickable(enabled = enabled, role = Role.Button) {
            if (look.vibrate) haptics.perform(SpaceHaptic.Tap)
            onClick()
        }
    }
    val glow = if (face.glow == null) Modifier else Modifier.spaceGlow(face.glow, { GLOW_ALPHA }, GLOW_SPREAD, { CORNER_FRACTION })
    Box(
        modifier = modifier
            .then(glow)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(shape)
            .background(face.brush)
            .border(1.dp, face.border, shape)
            .semantics(mergeDescendants = true) { if (description != null) contentDescription = description }
            .then(press),
        contentAlignment = Alignment.Center,
    ) {
        content(face.ink)
    }
}
