package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.DimLimits
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import kotlin.math.roundToInt

private val BAR_RADIUS = 16.dp
private val SIDE_W = 36.dp
private val CLOSE_W = 40.dp
private val TRACK_H = 10.dp
private val THUMB = 28.dp
private const val MOON_INK = 0xFFB9A2FF.toInt()
private const val SUN_INK = 0xFFFFD88A.toInt()
private const val NIGHT = 0xFF1C1440.toInt()
private const val GOLD_EDGE = 0x8CC9A24B.toInt()
private const val THUMB_EDGE = 0xFF8B5CF6.toInt()
private const val THUMB_GLOW = 0x55B9A2FF
private const val TRACK_EDGE = 0x2EFFFFFF
private const val CLOSE_INK = 0xFFE9E4FF.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

@Composable
internal fun DimBarContent(theme: ThemeController, layouts: LayoutController, dim: DimController) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val palette = rememberThemeSnapshot(theme).palette
        val prefs = rememberLayoutSnapshot(layouts).prefs.dim
        val shape = RoundedCornerShape(BAR_RADIUS)
        Row(
            Modifier
                .fillMaxSize()
                .padding(vertical = 3.dp)
                .clip(shape)
                .background(Color(ColorMath.mix(palette.background, palette.keyFill, 0.35f)))
                .border(1.dp, Color(GOLD_EDGE), shape)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SideIcon(SpaceIcon.MOON, MOON_INK, stringResource(R.string.dim_night))
            LevelSlider(prefs.levelPercent, Modifier.weight(1f), layouts::setDimLevel)
            SideIcon(SpaceIcon.SUN, SUN_INK, stringResource(R.string.dim_day))
            CloseButton(dim::close)
        }
    }
}

@Composable
private fun SideIcon(icon: SpaceIcon, ink: Int, label: String) {
    Box(
        Modifier
            .size(SIDE_W, THUMB)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, Color(ink), Modifier, 22.dp)
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    val label = stringResource(R.string.dim_done)
    Box(
        Modifier
            .size(CLOSE_W, 36.dp)
            .clip(RoundedCornerShape(10.dp))
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(SpaceIcon.CLOSE, Color(CLOSE_INK), Modifier, 20.dp)
    }
}

internal fun levelAt(x: Float, width: Int, rtl: Boolean): Int {
    if (width <= 0) return DimLimits.LEVEL_MAX
    val raw = (x / width).coerceIn(0f, 1f)
    val fraction = if (rtl) 1f - raw else raw
    val span = DimLimits.LEVEL_MAX - DimLimits.LEVEL_MIN
    return (DimLimits.LEVEL_MIN + fraction * span).roundToInt().coerceIn(DimLimits.LEVEL_MIN, DimLimits.LEVEL_MAX)
}

private fun Modifier.levelGesture(rtl: Boolean, onChange: State<(Int) -> Unit>): Modifier = pointerInput(rtl) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        down.consume()
        onChange.value(levelAt(down.position.x, size.width, rtl))
        while (true) {
            val move = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
            if (!move.pressed) break
            onChange.value(levelAt(move.position.x, size.width, rtl))
            move.consume()
        }
    }
}

@Composable
private fun LevelSlider(level: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    val change = rememberUpdatedState(onChange)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val min = DimLimits.LEVEL_MIN
    val max = DimLimits.LEVEL_MAX
    val description = stringResource(R.string.dim_level_value, level)
    Canvas(
        modifier
            .height(THUMB)
            .semantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(level.toFloat(), min.toFloat()..max.toFloat())
                setProgress { target ->
                    change.value(target.roundToInt().coerceIn(min, max))
                    true
                }
            }
            .levelGesture(rtl, change),
    ) {
        val fraction = (level - min).toFloat() / (max - min)
        drawDimTrack(if (rtl) 1f - fraction else fraction, rtl)
    }
}

private fun DrawScope.drawDimTrack(shown: Float, rtl: Boolean) {
    val y = size.height / 2f
    val height = TRACK_H.toPx()
    val night = Color(NIGHT)
    val day = Color(SUN_INK)
    val brush = Brush.horizontalGradient(
        listOf(night, day),
        startX = if (rtl) size.width else 0f,
        endX = if (rtl) 0f else size.width,
    )
    val corner = CornerRadius(height / 2f)
    drawRoundRect(brush, Offset(0f, y - height / 2f), Size(size.width, height), corner)
    drawRoundRect(Color(TRACK_EDGE), Offset(0f, y - height / 2f), Size(size.width, height), corner, Stroke(1.dp.toPx()))
    val radius = THUMB.toPx() / 2f
    val center = Offset((size.width * shown).coerceIn(radius, size.width - radius), y)
    drawCircle(Color(THUMB_GLOW), radius + 5.dp.toPx(), center)
    drawCircle(Color(WHITE), radius, center)
    drawCircle(Color(THUMB_EDGE), radius - 1.dp.toPx(), center, style = Stroke(2.dp.toPx()))
}
