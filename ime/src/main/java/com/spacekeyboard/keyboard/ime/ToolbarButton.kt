package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon

internal class ButtonLook(val palette: ThemePalette, val style: KeyStyle, val vibrate: Boolean)

internal class ButtonFace(
    val width: Dp,
    val height: Dp,
    val shape: Shape,
    val fill: Color,
    val border: Color?,
    val glow: Color?,
    val ink: Color,
    val underline: Color?,
    val labelled: Boolean,
    val accent: Color = ink,
)

internal fun buttonFace(look: ButtonLook, on: Boolean): ButtonFace = when (look.style) {
    KeyStyle.GLASS -> glassFace(look.palette, on)
    KeyStyle.GALAXY -> galaxyFace(look.palette, on)
    KeyStyle.DAY -> dayFace(look.palette, on)
    KeyStyle.ECLIPSE -> eclipseFace(look.palette, on)
}

private fun galaxyFace(p: ThemePalette, on: Boolean) = ButtonFace(
    width = 44.dp,
    height = 36.dp,
    shape = CircleShape,
    fill = if (on) Color(ColorMath.withAlpha(p.glow, 0.26f)) else Color.Transparent,
    border = null,
    glow = null,
    ink = Color(if (on) p.label else ColorMath.withAlpha(p.label, 0.82f)),
    underline = null,
    labelled = false,
    accent = Color(p.glow),
)

private fun glassFace(p: ThemePalette, on: Boolean) = ButtonFace(
    width = 44.dp,
    height = 36.dp,
    shape = CircleShape,
    fill = if (on) Color(ColorMath.withAlpha(p.glow, GLASS_CHIP_ALPHA)) else Color.Transparent,
    border = null,
    glow = null,
    ink = Color(if (on) p.glow else GlassSkins.dim(p)),
    underline = null,
    labelled = false,
    accent = Color(p.glow),
)

private fun dayFace(p: ThemePalette, on: Boolean): ButtonFace {
    val fill = if (on) p.glow else ColorMath.mix(p.keyFill, p.border, 0.3f)
    return ButtonFace(
        width = 36.dp,
        height = 36.dp,
        shape = CircleShape,
        fill = Color(fill),
        border = null,
        glow = null,
        ink = Color(ColorMath.readableOn(fill, listOf(p.keyText, ColorMath.WHITE))),
        underline = null,
        labelled = false,
        accent = if (on) Color(ColorMath.readableOn(fill, listOf(p.keyText, ColorMath.WHITE))) else Color(p.glow),
    )
}

private fun eclipseFace(p: ThemePalette, on: Boolean) = ButtonFace(
    width = 44.dp,
    height = 36.dp,
    shape = RectangleShape,
    fill = Color.Transparent,
    border = null,
    glow = null,
    ink = Color(if (on) p.glow else ColorMath.withAlpha(p.label, 0.7f)),
    underline = if (on) Color(p.glow) else null,
    labelled = false,
    accent = Color(p.glow),
)

private fun Modifier.halo(color: Color?): Modifier = if (color == null) this else drawBehind {
    for (layer in HALO_LAYERS downTo 1) {
        val grow = layer * HALO_STEP.dp.toPx()
        drawRoundRect(
            color = color.copy(alpha = HALO_ALPHA * (HALO_LAYERS + 1 - layer)),
            topLeft = Offset(-grow, -grow),
            size = Size(size.width + 2 * grow, size.height + 2 * grow),
            cornerRadius = CornerRadius(CORNER.dp.toPx() + grow),
        )
    }
}

private fun Modifier.underline(color: Color?): Modifier = if (color == null) this else drawBehind {
    val thickness = UNDERLINE.dp.toPx()
    drawRect(color, Offset(0f, size.height - thickness), Size(size.width, thickness))
}

@Composable
internal fun ToolbarButton(
    icon: SpaceIcon,
    description: String,
    look: ButtonLook,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true,
) {
    val haptics = rememberSpaceHaptics()
    val face = buttonFace(look, selected)
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 44.dp, minHeight = 40.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled, role = Role.Button) {
                if (look.vibrate) haptics.perform(SpaceHaptic.Tap)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        ButtonBody(face, icon, description, selected)
    }
}

@Composable
private fun ButtonBody(face: ButtonFace, icon: SpaceIcon, label: String, selected: Boolean) {
    var modifier = Modifier.size(width = face.width, height = face.height).halo(face.glow).clip(face.shape).background(face.fill)
    if (face.border != null) modifier = modifier.border(1.dp, face.border, face.shape)
    Column(
        modifier = modifier.underline(face.underline),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ToolbarIcon(icon, face.ink, accent = face.accent)
        if (face.labelled) {
            val weight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics { },
                style = TextStyle(color = face.ink, fontSize = LABEL_SP.sp, fontWeight = weight),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val HALO_LAYERS = 3
private const val HALO_STEP = 2f
private const val HALO_ALPHA = 0.07f
private const val CORNER = 12f
private const val UNDERLINE = 2f
private const val LABEL_SP = 10f
private const val DISABLED_ALPHA = 0.38f
private const val GLASS_CHIP_ALPHA = 0.17f

@Composable
internal fun ToolbarTextButton(
    text: String,
    description: String,
    look: ButtonLook,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    val haptics = rememberSpaceHaptics()
    val face = buttonFace(look, selected)
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 44.dp, minHeight = 40.dp)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button) {
                if (look.vibrate) haptics.perform(SpaceHaptic.Tap)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        var modifier = Modifier.size(width = maxOf(face.width, 44.dp), height = face.height).halo(face.glow).clip(CircleShape).background(face.fill)
        if (face.border != null) modifier = modifier.border(1.dp, face.border, CircleShape)
        Box(modifier = modifier.underline(face.underline), contentAlignment = Alignment.Center) {
            BasicText(
                text = text,
                modifier = Modifier.clearAndSetSemantics { },
                style = TextStyle(color = face.ink, fontSize = 18.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                maxLines = 1,
            )
        }
    }
}
