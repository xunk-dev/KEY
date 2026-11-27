package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import kotlinx.coroutines.delay

private val RAIL_BUTTON_W = 48.dp
private val RAIL_BUTTON_H = 36.dp
private val RAIL_ICON = 22.dp
private val CORNER_W = 44.dp
private val IDLE_PILL_W = 40.dp
private val IDLE_PILL_H = 4.dp
private val OPEN_PILL_W = 72.dp
private val OPEN_PILL_H = 5.dp
private const val CORNER_GLYPH_DP = 18f
private const val CORNER_STROKE_DP = 2f
private const val IDLE_PILL_ALPHA = 0.3f
private const val SELECTED_ALPHA = 0.18f
private const val RAIL_IDLE_MS = 3500L
private const val RAIL_POLL_MS = 250L

@Composable
internal fun FloatRailContent(
    theme: ThemeController,
    layouts: LayoutController,
    float: FloatController,
    mover: FloatMover,
    resizer: FloatResizer,
) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val palette = rememberThemeSnapshot(theme).palette
        val accent = Color(palette.glow)
        val open = float.railOpen
        val footer = Color(ColorMath.mix(palette.background, android.graphics.Color.BLACK, 0.5f))
        val line = Color(ColorMath.mix(palette.background, palette.glow, 0.18f))
        val quiet = Color(ColorMath.mix(palette.label, palette.background, 0.5f))
        LaunchedEffect(float.railStamp, open) {
            if (!open) return@LaunchedEffect
            delay(RAIL_IDLE_MS)
            while (float.holding) delay(RAIL_POLL_MS)
            float.closeRail()
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier
                    .fillMaxSize()
                    .background(if (open) footer else Color.Transparent)
                    .drawBehind { if (open) drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
                    .padding(horizontal = if (open) 4.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (open) {
                    CornerGrip(quiet, fromLeft = true, float, resizer)
                    RailButton(SpaceIcon.DOCK, stringResource(R.string.float_dock), accent, false, float) {
                        float.closePopover()
                        layouts.dockFloat()
                    }
                }
                RailHandle(accent, open, float, mover, Modifier.weight(1f))
                if (open) {
                    RailButton(SpaceIcon.OPACITY, stringResource(R.string.float_opacity), accent, float.popoverOpen, float) {
                        float.togglePopover()
                    }
                    CornerGrip(quiet, fromLeft = false, float, resizer)
                }
            }
        }
    }
}

@Composable
private fun RailHandle(accent: Color, open: Boolean, float: FloatController, mover: FloatMover, modifier: Modifier) {
    val label = stringResource(R.string.float_handle)
    Box(
        modifier
            .fillMaxHeight()
            .semantics { contentDescription = label }
            .rawDrag(
                onStart = {
                    float.closePopover()
                    float.touchRail()
                    float.holdRail(true)
                    mover.start()
                },
                onDrag = { dx, dy -> mover.drag(dx, dy) },
                onEnd = {
                    mover.end()
                    float.holdRail(false)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val width = if (open) OPEN_PILL_W else IDLE_PILL_W
        val height = if (open) OPEN_PILL_H else IDLE_PILL_H
        Box(
            Modifier
                .size(width, height)
                .clip(RoundedCornerShape(3.dp))
                .background(if (open) accent else accent.copy(alpha = IDLE_PILL_ALPHA)),
        )
    }
}

@Composable
private fun RailButton(
    icon: SpaceIcon,
    label: String,
    accent: Color,
    selected: Boolean,
    float: FloatController,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .size(RAIL_BUTTON_W, RAIL_BUTTON_H)
            .clip(shape)
            .background(if (selected) accent.copy(alpha = SELECTED_ALPHA) else Color.Transparent)
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .clickable {
                float.touchRail()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, accent, Modifier, RAIL_ICON)
    }
}

@Composable
private fun CornerGrip(tint: Color, fromLeft: Boolean, float: FloatController, resizer: FloatResizer) {
    val label = stringResource(if (fromLeft) R.string.float_resize_left else R.string.float_resize_right)
    Canvas(
        Modifier
            .width(CORNER_W)
            .fillMaxHeight()
            .semantics { contentDescription = label }
            .rawDrag(
                onStart = {
                    float.closePopover()
                    float.touchRail()
                    float.holdRail(true)
                    resizer.start()
                },
                onDrag = { dx, dy -> resizer.drag(dx, dy, fromLeft) },
                onEnd = { float.holdRail(false) },
            ),
    ) {
        val box = CORNER_GLYPH_DP.dp.toPx()
        val unit = box / CORNER_GLYPH_DP
        val x0 = (size.width - box) / 2f
        val y0 = (size.height - box) / 2f
        val width = CORNER_STROKE_DP.dp.toPx()
        fun line(ax: Float, ay: Float, bx: Float, by: Float) {
            val flip = !fromLeft
            val fromX = if (flip) CORNER_GLYPH_DP - ax else ax
            val toX = if (flip) CORNER_GLYPH_DP - bx else bx
            drawLine(tint, Offset(x0 + fromX * unit, y0 + ay * unit), Offset(x0 + toX * unit, y0 + by * unit), width, StrokeCap.Round)
        }
        line(4f, 4f, 14f, 14f)
        line(4f, 9f, 9f, 14f)
    }
}
