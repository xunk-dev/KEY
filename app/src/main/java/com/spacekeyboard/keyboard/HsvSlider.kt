package com.spacekeyboard.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun HsvSlider(
    fraction: Float,
    stops: List<Color>,
    thumb: Color,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    valueText: String? = null,
    steps: Int = DEFAULT_STEPS,
) {
    val current = rememberUpdatedState(onChange)
    val shown = fraction.coerceIn(0f, 1f)
    val spoken = valueText ?: "${(shown * 100).roundToInt()}%"
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics {
                    contentDescription = label
                    stateDescription = spoken
                    progressBarRangeInfo = ProgressBarRangeInfo(shown, 0f..1f, steps)
                    setProgress { target ->
                        current.value(target.coerceIn(0f, 1f))
                        true
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { current.value((it.x / size.width).coerceIn(0f, 1f)) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        current.value((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                },
        ) {
            val track = 12.dp.toPx()
            val top = (size.height - track) / 2f
            drawRoundRect(
                brush = Brush.horizontalGradient(stops),
                topLeft = Offset(0f, top),
                size = Size(size.width, track),
                cornerRadius = CornerRadius(track / 2f),
            )
            val x = fraction.coerceIn(0f, 1f) * size.width
            drawCircle(Color(0x33000000), radius = 16.dp.toPx(), center = Offset(x, size.height / 2f))
            drawCircle(Color.White, radius = 15.dp.toPx(), center = Offset(x, size.height / 2f))
            drawCircle(thumb, radius = 11.dp.toPx(), center = Offset(x, size.height / 2f))
        }
    }
}

private const val DEFAULT_STEPS = 19
