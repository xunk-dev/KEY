package com.spacekeyboard.keyboard.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.R
import com.spacekeyboard.keyboard.designsystem.a11y.LocalSystemA11y
import com.spacekeyboard.keyboard.designsystem.theme.GlassIntensity
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val FALLBACK_ALPHA = 0.7f
private const val SHADOW_STEPS = 10
private const val SHADOW_LAYER_ALPHA = 0.045f
private val ShadowSpread = 30.dp
private val ShadowOffsetY = 12.dp
private val RimInset = 1.5.dp

@Composable
private fun rememberBlurEnabled(): Boolean {
    val quiet = LocalSystemA11y.current.reduceTransparency
    return blurSupported && !quiet && SpaceTheme.glassIntensity != GlassIntensity.Off
}

@Composable
internal fun DockBar(
    selected: DockTab,
    onSelect: (DockTab) -> Unit,
    palette: DockPalette,
    backdrop: Backdrop,
    hostOrigin: () -> Offset,
    modifier: Modifier = Modifier,
) {
    var width by remember { mutableFloatStateOf(0f) }
    var position by remember { mutableStateOf(Offset.Zero) }
    val center by rememberBeamCenter(selected.ordinal, width)
    val blur = rememberBlurEnabled()
    val navLabel = stringResource(R.string.dock_navigation)
    val blurPx = with(LocalDensity.current) { DockMetrics.BlurRadius.toPx() }
    val container = if (blur) palette.container else palette.container.copy(alpha = FALLBACK_ALPHA)
    val sheen = Brush.verticalGradient(0f to palette.sheenTop, 0.5f to palette.sheenBottom, 1f to palette.sheenBottom)
    Box(
        modifier = modifier
            .width(DockMetrics.DockWidth)
            .height(DockMetrics.DockHeight)
            .onGloballyPositioned { width = it.size.width.toFloat(); position = it.positionInRoot() }
            .drawBehind { drawSoftShadow() }
            .clip(CircleShape)
            .semantics { contentDescription = navLabel },
    ) {
        if (blur) Box(Modifier.matchParentSize().backdropBlur(backdrop, { position - hostOrigin() }, blurPx))
        Box(Modifier.matchParentSize().background(container).background(sheen).drawBehind { drawBeam(center, palette) })
        DockTabs(selected, onSelect, palette)
        Box(Modifier.matchParentSize().border(DockMetrics.DockBorder, palette.border, CircleShape).drawBehind { drawRim(palette) })
    }
}

@Composable
private fun DockTabs(selected: DockTab, onSelect: (DockTab) -> Unit, palette: DockPalette) {
    Row(Modifier.fillMaxSize().padding(DockMetrics.DockPadding).selectableGroup()) {
        DockTab.entries.forEach { tab ->
            DockTabItem(tab, tab == selected, palette) { onSelect(tab) }
        }
    }
}

private fun DrawScope.drawSoftShadow() {
    val radius = size.height / 2f
    val hole = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius))) }
    val dy = ShadowOffsetY.toPx()
    clipPath(hole, ClipOp.Difference) {
        for (i in 1..SHADOW_STEPS) {
            val spread = ShadowSpread.toPx() * i / SHADOW_STEPS
            drawRoundRect(
                color = Color.Black.copy(alpha = SHADOW_LAYER_ALPHA),
                topLeft = Offset(-spread, dy - spread),
                size = Size(size.width + spread * 2, size.height + spread * 2),
                cornerRadius = CornerRadius(radius + spread),
            )
        }
    }
}

private fun DrawScope.drawRim(palette: DockPalette) {
    val inset = RimInset.toPx()
    val brush = Brush.verticalGradient(
        0f to palette.rimTop,
        0.3f to Color.Transparent,
        0.8f to Color.Transparent,
        1f to palette.rimBottom,
    )
    drawRoundRect(
        brush = brush,
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = CornerRadius(size.height / 2f - inset),
        style = Stroke(1.dp.toPx()),
    )
}
