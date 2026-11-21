package com.spacekeyboard.keyboard.designsystem.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private const val GRID = 24f
private const val PEN = 1.75f
private const val DASH = 3.2f

private val line = Stroke(width = PEN, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val dashed = Stroke(
    width = PEN,
    cap = StrokeCap.Round,
    join = StrokeJoin.Round,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH, DASH)),
)

@Composable
fun SpaceIconView(icon: SpaceIcon, tint: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    val flip = icon.mirrors && LocalLayoutDirection.current == LayoutDirection.Ltr
    val parts = icon.parts()
    Canvas(modifier.size(size)) {
        val extent = this.size
        val unit = extent.minDimension / GRID
        withTransform({
            if (flip) translate(extent.width, 0f)
            scale(if (flip) -unit else unit, unit, Offset.Zero)
        }) {
            parts.forEach { drawPart(it, tint) }
        }
    }
}

private fun DrawScope.drawPart(part: IconPart, color: Color) {
    when (part) {
        is StrokePath -> drawPath(part.path, color, style = line)
        is Dot -> {
            drawCircle(color, part.r, Offset(part.x, part.y))
            drawCircle(color, part.r, Offset(part.x, part.y), style = line)
        }
        is Ring -> drawCircle(color, part.r, Offset(part.x, part.y), style = line)
        is Box -> drawRoundRect(
            color = color,
            topLeft = Offset(part.x, part.y),
            size = Size(part.w, part.h),
            cornerRadius = CornerRadius(part.radius),
            style = if (part.dashed) dashed else line,
        )
    }
}
