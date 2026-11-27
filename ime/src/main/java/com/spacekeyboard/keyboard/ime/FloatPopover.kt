package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import kotlin.math.roundToInt

private val BAR_RADIUS = 16.dp
private const val TRACK_ALPHA = 0.25f

private fun Modifier.swallowTouches(): Modifier =
    pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }

@Composable
internal fun FloatPopoverContent(theme: ThemeController, layouts: LayoutController) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val palette = rememberThemeSnapshot(theme).palette
        val accent = Color(palette.glow)
        val fill = Color(ColorMath.mix(palette.background, palette.keyFill, 0.35f))
        val ink = Color(palette.label)
        val value = rememberLayoutSnapshot(layouts).prefs.floatOpacityPercent
        val shape = RoundedCornerShape(BAR_RADIUS)
        Column(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .background(fill)
                .border(1.dp, accent, shape)
                .swallowTouches()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpaceText(stringResource(R.string.float_opacity), SpaceTheme.typography.bodySmall, color = ink)
                SpaceText("$value%", SpaceTheme.typography.bodySmall, color = accent)
            }
            OpacitySlider(value, accent, stringResource(R.string.float_opacity_value, value), layouts::setFloatOpacity)
        }
    }
}

internal fun opacityAt(x: Float, width: Int, rtl: Boolean): Int {
    if (width <= 0) return Limits.FLOAT_OPACITY_MAX
    val raw = (x / width).coerceIn(0f, 1f)
    val fraction = if (rtl) 1f - raw else raw
    val span = Limits.FLOAT_OPACITY_MAX - Limits.FLOAT_OPACITY_MIN
    return (Limits.FLOAT_OPACITY_MIN + fraction * span).roundToInt().coerceIn(Limits.FLOAT_OPACITY_MIN, Limits.FLOAT_OPACITY_MAX)
}

private fun Modifier.opacityGesture(rtl: Boolean, onChange: State<(Int) -> Unit>): Modifier = pointerInput(rtl) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        down.consume()
        onChange.value(opacityAt(down.position.x, size.width, rtl))
        while (true) {
            val event = awaitPointerEvent()
            val move = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!move.pressed) break
            onChange.value(opacityAt(move.position.x, size.width, rtl))
            move.consume()
        }
    }
}

@Composable
private fun OpacitySlider(value: Int, accent: Color, description: String, onChange: (Int) -> Unit) {
    val change = rememberUpdatedState(onChange)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val min = Limits.FLOAT_OPACITY_MIN
    val max = Limits.FLOAT_OPACITY_MAX
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(28.dp)
            .semantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(value.toFloat(), min.toFloat()..max.toFloat())
                setProgress { target ->
                    change.value(target.roundToInt().coerceIn(min, max))
                    true
                }
            }
            .opacityGesture(rtl, change),
    ) {
        val fraction = (value - min).toFloat() / (max - min)
        drawOpacityTrack(accent, if (rtl) 1f - fraction else fraction, rtl)
    }
}

private fun DrawScope.drawOpacityTrack(accent: Color, shown: Float, rtl: Boolean) {
    val y = size.height / 2f
    val thumbX = size.width * shown
    val track = 4.dp.toPx()
    val radius = 10.dp.toPx()
    drawLine(accent.copy(alpha = TRACK_ALPHA), Offset(0f, y), Offset(size.width, y), track, StrokeCap.Round)
    drawLine(accent, Offset(if (rtl) size.width else 0f, y), Offset(thumbX, y), track, StrokeCap.Round)
    drawCircle(accent, radius, Offset(thumbX.coerceIn(radius, size.width - radius), y))
}
