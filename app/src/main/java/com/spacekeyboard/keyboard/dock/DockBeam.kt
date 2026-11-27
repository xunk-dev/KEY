package com.spacekeyboard.keyboard.dock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.spacekeyboard.keyboard.designsystem.a11y.LocalSystemA11y

internal fun beamTarget(index: Int, widthPx: Float, paddingPx: Float, rtl: Boolean): Float {
    val slot = (widthPx - paddingPx * 2) / DockMetrics.TabCount
    val visual = if (rtl) DockMetrics.TabCount - 1 - index else index
    return paddingPx + slot * (visual + 0.5f)
}

@Composable
internal fun rememberBeamCenter(index: Int, widthPx: Float): State<Float> {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val padding = with(LocalDensity.current) { DockMetrics.DockPadding.toPx() }
    val calm = LocalSystemA11y.current.reduceMotion
    val center = remember { Animatable(0f) }
    var placed by remember { mutableStateOf(false) }
    val target = beamTarget(index, widthPx, padding, rtl)
    LaunchedEffect(target, widthPx > 0f) {
        if (widthPx <= 0f || !placed || calm) {
            center.snapTo(target)
            placed = widthPx > 0f
        } else {
            center.animateTo(target, spring(DockMetrics.SpringDamping, DockMetrics.SpringStiffness))
        }
    }
    return center.asState()
}

internal fun DrawScope.drawBeam(centerX: Float, palette: DockPalette) {
    val width = DockMetrics.BeamWidth.toPx()
    val height = DockMetrics.BeamHeight.toPx()
    val reach = DockMetrics.GlowRadius.toPx()
    val top = DockMetrics.DockBorder.toPx()
    val origin = Offset(centerX, top)
    scale(1f, DockMetrics.GlowSquash, origin) {
        drawCircle(Brush.radialGradient(listOf(palette.glow, Color.Transparent), origin, reach), reach, origin)
    }
    val bar = Brush.horizontalGradient(
        listOf(palette.gradientBottom, palette.gradientTop, palette.gradientBottom),
        startX = centerX - width / 2,
        endX = centerX + width / 2,
    )
    drawRoundRect(bar, Offset(centerX - width / 2, top), Size(width, height), CornerRadius(height / 2))
}
