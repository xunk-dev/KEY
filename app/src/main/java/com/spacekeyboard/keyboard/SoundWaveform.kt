package com.spacekeyboard.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.sound.ClipMath
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private val WAVE_HEIGHT = 132.dp
private val GRAB_RADIUS = 30.dp
private val BAR_WIDTH = 3.dp
private val BAR_GAP = 1.5.dp
private val HANDLE_WIDTH = 14.dp
private val HANDLE_HEIGHT = 40.dp

@Composable
internal fun Waveform(state: TrimState, modifier: Modifier = Modifier) {
    var widthPx by remember { mutableFloatStateOf(0f) }
    val grabPx = with(LocalDensity.current) { GRAB_RADIUS.toPx() }
    val colors = SpaceTheme.colors
    val description = stringResource(
        R.string.sound_wave_desc,
        ClipMath.seconds(state.range.startMs),
        ClipMath.seconds(state.range.endMs),
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(
            modifier
                .fillMaxWidth()
                .height(WAVE_HEIGHT)
                .clip(RoundedCornerShape(14.dp))
                .background(SpaceTheme.look.field)
                .onSizeChanged { widthPx = it.width.toFloat() }
                .pointerInput(state, widthPx) { waveformGestures(state, widthPx, grabPx) }
                .semantics { contentDescription = description },
        ) {
            drawWave(state, colors.primary, colors.onSurfaceVariant.copy(alpha = 0.45f))
            drawSelection(state, colors.primary, colors.onPrimary)
        }
    }
}

private suspend fun PointerInputScope.waveformGestures(state: TrimState, widthPx: Float, grabPx: Float) {
    if (widthPx <= 0f) return
    var mode = DragMode.PAN
    detectHorizontalDragGestures(onDragStart = { mode = state.hit(it.x, widthPx, grabPx) }) { change, dx ->
        change.consume()
        state.drag(mode, dx / widthPx)
    }
}

private fun DrawScope.drawWave(state: TrimState, active: Color, idle: Color) {
    val step = (BAR_WIDTH + BAR_GAP).toPx()
    val columns = (size.width / step).toInt().coerceAtLeast(1)
    val view = state.view
    val usable = size.height - 24.dp.toPx()
    for (column in 0 until columns) {
        val from = view.startMs + view.lengthMs * column / columns
        val to = view.startMs + view.lengthMs * (column + 1) / columns
        val height = maxOf(2.dp.toPx(), state.level(from, to) * usable)
        val inside = (from + to) / 2 in state.range.startMs..state.range.endMs
        drawRoundRect(
            color = if (inside) active else idle,
            topLeft = Offset(column * step, (size.height - height) / 2f),
            size = Size(BAR_WIDTH.toPx(), height),
            cornerRadius = CornerRadius(BAR_WIDTH.toPx() / 2f),
        )
    }
}

private fun DrawScope.drawSelection(state: TrimState, edge: Color, ink: Color) {
    val startX = state.xOf(state.range.startMs, size.width)
    val endX = state.xOf(state.range.endMs, size.width)
    drawRect(edge.copy(alpha = 0.12f), Offset(startX, 0f), Size(endX - startX, size.height))
    listOf(startX, endX).forEach { x ->
        drawRect(edge, Offset(x - 1.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
        val w = HANDLE_WIDTH.toPx()
        val h = HANDLE_HEIGHT.toPx()
        drawRoundRect(edge, Offset(x - w / 2f, (size.height - h) / 2f), Size(w, h), CornerRadius(w / 2f))
        drawRect(ink, Offset(x - 1.dp.toPx(), (size.height - 16.dp.toPx()) / 2f), Size(2.dp.toPx(), 16.dp.toPx()))
    }
}
