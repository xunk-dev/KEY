package com.spacekeyboard.keyboard.dock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.spacekeyboard.keyboard.designsystem.a11y.spaceSelectable
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val IDLE_SATURATION = 0.2f
private const val IDLE_ALPHA = 0.62f

@Composable
internal fun RowScope.DockTabItem(tab: DockTab, selected: Boolean, palette: DockPalette, onClick: () -> Unit) {
    val label = stringResource(tab.label)
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(DockMetrics.SpringDamping, DockMetrics.SpringStiffness),
        label = "dockTab",
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .drawBehind { drawPill(palette, progress) }
            .clip(CircleShape)
            .spaceSelectable(selected, Role.Tab, onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TabIcon(tab, label, palette.glow) { progress }
        TabLabel(label, selected, palette.selectedLabel)
    }
}

private fun DrawScope.drawPill(palette: DockPalette, amount: Float) {
    val alpha = amount.coerceIn(0f, 1f)
    if (alpha <= 0f) return
    val grow = DockMetrics.PillFromScale + (1f - DockMetrics.PillFromScale) * amount
    scale(grow, grow, center) {
        val radius = size.height / 2f
        drawRoundRect(palette.pill.copy(alpha = palette.pill.alpha * alpha), cornerRadius = CornerRadius(radius))
        val inset = 0.5.dp.toPx()
        val rim = Brush.verticalGradient(
            0f to palette.pillRimTop.copy(alpha = palette.pillRimTop.alpha * alpha),
            0.35f to palette.pillRim.copy(alpha = palette.pillRim.alpha * alpha),
            1f to palette.pillRim.copy(alpha = palette.pillRim.alpha * alpha),
        )
        drawRoundRect(
            brush = rim,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(radius - inset),
            style = Stroke(1.dp.toPx()),
        )
    }
}

@Composable
private fun TabIcon(tab: DockTab, label: String, glow: Color, progress: () -> Float) {
    val vector = remember(tab) { dockIcon(tab) }
    val amount = progress().coerceIn(0f, 1f)
    val saturation = IDLE_SATURATION + (1f - IDLE_SATURATION) * amount
    Image(
        imageVector = vector,
        contentDescription = label,
        alpha = IDLE_ALPHA + (1f - IDLE_ALPHA) * amount,
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) }),
        modifier = Modifier
            .size(DockMetrics.IconWidth, DockMetrics.IconHeight)
            .drawBehind { drawGlow(glow, progress().coerceIn(0f, 1f)) }
            .graphicsLayer {
                val scale = 1f + (DockMetrics.SelectedIconScale - 1f) * progress()
                scaleX = scale
                scaleY = scale
                translationY = -1.dp.toPx() * progress()
            },
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlow(glow: Color, amount: Float) {
    if (amount <= 0f) return
    val radius = 24.dp.toPx()
    val tinted = glow.copy(alpha = glow.alpha * amount)
    drawCircle(Brush.radialGradient(listOf(tinted, Color.Transparent), center, radius), radius, center)
}

@Composable
private fun ColumnScope.TabLabel(label: String, visible: Boolean, color: Color) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        SpaceText(
            text = label,
            style = SpaceTheme.typography.label.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
            color = color,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp).clearAndSetSemantics {},
        )
    }
}
