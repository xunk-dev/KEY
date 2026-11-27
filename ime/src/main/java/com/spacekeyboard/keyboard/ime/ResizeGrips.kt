package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import kotlin.math.roundToInt

private const val FULL_WIDTH_SNAP = 94f
private const val STRIP_DP = 40

internal class Drag {
    var base = 0f
    var rows = 1
    var last = Int.MIN_VALUE
    var target: PanelMode? = null
    var centered = false
}

@Composable
internal fun Modifier.rawDrag(onStart: () -> Unit, onDrag: (dx: Float, dy: Float) -> Unit, onEnd: () -> Unit = {}): Modifier {
    val view = LocalView.current
    val start by rememberUpdatedState(onStart)
    val drag by rememberUpdatedState(onDrag)
    val end by rememberUpdatedState(onEnd)
    val holder = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val screen = remember { IntArray(2) }
    return this
        .onGloballyPositioned { holder[0] = it }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                fun raw(local: Offset): Offset {
                    val root = holder[0]?.takeIf { it.isAttached }?.localToRoot(local) ?: local
                    view.getLocationOnScreen(screen)
                    return Offset(root.x + screen[0], root.y + screen[1])
                }
                val origin = raw(down.position)
                start()
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    val now = raw(change.position)
                    drag(now.x - origin.x, now.y - origin.y)
                    change.consume()
                }
                end()
            }
        }
}

internal class ResizeCtx(
    val layouts: LayoutController,
    val latest: State<LayoutSnapshot>,
    val accent: Color,
    val ink: Color,
    val density: Float,
    val screenPx: Int,
    val width: Float,
    val leftEdge: Float,
    val rightEdge: Float,
)

@Composable
internal fun BoxScope.HeightGrip(c: ResizeCtx) {
    val drag = remember { Drag() }
    val label = stringResource(R.string.resize_height)
    Grip(
        horizontal = true,
        accent = c.accent,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(STRIP_DP.dp)
            .semantics { contentDescription = label }
            .rawDrag(
                onStart = {
                    val snap = c.latest.value
                    drag.rows = snap.active.rows.size.coerceAtLeast(1)
                    drag.base = snap.prefs.heightPercent.toFloat().coerceAtMost(maxPercent(c, drag.rows))
                    drag.last = Int.MIN_VALUE
                },
                onDrag = { _, dy ->
                    val perRow = Limits.ROW_DP * drag.rows
                    val ceiling = maxPercent(c, drag.rows).toInt()
                    val next = (drag.base + (-dy / c.density) / perRow * 100f).roundToInt()
                        .coerceIn(Limits.HEIGHT_MIN, maxOf(Limits.HEIGHT_MIN, ceiling))
                    if (next != drag.last) {
                        drag.last = next
                        c.layouts.setHeight(next)
                    }
                },
            ),
    )
}

private fun maxPercent(c: ResizeCtx, rows: Int): Float =
    c.latest.value.prefs.maxHeightPercent(rows, c.density, c.screenPx).toFloat()

@Composable
internal fun BoxScope.LeftGrip(c: ResizeCtx) {
    val drag = remember { Drag() }
    val label = stringResource(R.string.resize_width_left)
    Grip(
        horizontal = false,
        accent = c.accent,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(top = STRIP_DP.dp)
            .fillMaxHeight()
            .width(STRIP_DP.dp)
            .semantics { contentDescription = label }
            .rawDrag(
                onStart = {
                    val p = c.latest.value.prefs
                    drag.centered = p.mode == PanelMode.NORMAL
                    drag.base = edgesOf(p, c.width).first
                    drag.last = Int.MIN_VALUE
                },
                onDrag = { dx, _ ->
                    val edge = (drag.base + dx).coerceIn(0f, c.width)
                    if (drag.centered) {
                        applyCentered((c.width - 2f * edge) / c.width * 100f, drag, c.layouts)
                    } else {
                        applyWidth((c.width - edge) / c.width * 100f, PanelMode.ONE_HAND_RIGHT, drag, c.layouts)
                    }
                },
            ),
    )
}

@Composable
internal fun BoxScope.RightGrip(c: ResizeCtx) {
    val drag = remember { Drag() }
    val label = stringResource(R.string.resize_width_right)
    Grip(
        horizontal = false,
        accent = c.accent,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(top = STRIP_DP.dp)
            .fillMaxHeight()
            .width(STRIP_DP.dp)
            .semantics { contentDescription = label }
            .rawDrag(
                onStart = {
                    val p = c.latest.value.prefs
                    drag.centered = p.mode == PanelMode.NORMAL
                    drag.base = edgesOf(p, c.width).second
                    drag.last = Int.MIN_VALUE
                },
                onDrag = { dx, _ ->
                    val edge = (drag.base + dx).coerceIn(0f, c.width)
                    if (drag.centered) {
                        applyCentered((2f * edge - c.width) / c.width * 100f, drag, c.layouts)
                    } else {
                        applyWidth(edge / c.width * 100f, PanelMode.ONE_HAND_LEFT, drag, c.layouts)
                    }
                },
            ),
    )
}

internal fun applyWidth(percent: Float, side: PanelMode, drag: Drag, layouts: LayoutController) {
    val key = if (percent >= FULL_WIDTH_SNAP) -1 else percent.roundToInt().coerceIn(Limits.ONE_HAND_MIN, Limits.ONE_HAND_MAX)
    if (key == drag.last) return
    drag.last = key
    if (key < 0) {
        layouts.setMode(PanelMode.NORMAL)
    } else {
        layouts.setOneHand(key)
        layouts.setMode(side)
    }
}

internal fun applyCentered(percent: Float, drag: Drag, layouts: LayoutController) {
    val key = if (percent >= FULL_WIDTH_SNAP) Limits.WIDTH_MAX else percent.roundToInt().coerceIn(Limits.WIDTH_MIN, Limits.WIDTH_MAX)
    if (key == drag.last) return
    drag.last = key
    layouts.setWidth(key)
}

internal fun widthOf(prefs: KeyboardPrefs): Int = when (prefs.mode) {
    PanelMode.NORMAL -> prefs.widthPercent
    PanelMode.ONE_HAND_LEFT, PanelMode.ONE_HAND_RIGHT -> prefs.oneHandPercent
    else -> Limits.WIDTH_MAX
}

internal fun edgesOf(prefs: KeyboardPrefs, width: Float): Pair<Float, Float> {
    val fraction = prefs.oneHandPercent / 100f
    return when (prefs.mode) {
        PanelMode.ONE_HAND_RIGHT -> width * (1f - fraction) to width
        PanelMode.ONE_HAND_LEFT -> 0f to width * fraction
        PanelMode.NORMAL -> {
            val inset = width * (Limits.WIDTH_MAX - prefs.widthPercent) / (2f * Limits.WIDTH_MAX)
            inset to width - inset
        }
        else -> 0f to width
    }
}
