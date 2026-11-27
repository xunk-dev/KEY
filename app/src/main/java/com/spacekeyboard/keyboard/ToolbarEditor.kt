package com.spacekeyboard.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import kotlin.math.roundToInt

private val RowHeight = 48.dp
private val RowGap = 6.dp
private val RowShape = RoundedCornerShape(14.dp)

@Composable
fun ToolbarEditor(items: List<ToolbarItem>, onMove: (Int, Int) -> Unit, onDragging: (Boolean) -> Unit = {}) {
    val pitch = with(LocalDensity.current) { (RowHeight + RowGap).toPx() }
    val haptics = LocalHapticFeedback.current
    val latest by rememberUpdatedState(items)
    var held by remember { mutableStateOf<ToolbarItem?>(null) }
    var drag by remember { mutableFloatStateOf(0f) }

    fun targetFor(item: ToolbarItem?, list: List<ToolbarItem>, offset: Float): Int {
        val from = list.indexOf(item)
        return if (from < 0) -1 else (from + (offset / pitch).roundToInt()).coerceIn(0, list.lastIndex)
    }

    val from = items.indexOf(held)
    val target = targetFor(held, items, drag)
    var lastTarget by remember { mutableStateOf(-1) }
    if (target != lastTarget) {
        if (lastTarget >= 0 && target >= 0) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        lastTarget = target
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(RowGap)) {
        items.forEachIndexed { index, item ->
            key(item) {
                val isHeld = item == held
                val slide = when {
                    isHeld || from < 0 -> 0f
                    from < target && index in (from + 1)..target -> -pitch
                    from > target && index in target until from -> pitch
                    else -> 0f
                }
                val settled by animateFloatAsState(slide, spring(dampingRatio = 0.8f, stiffness = 600f), label = "slide")
                ToolbarRow(
                    item = item,
                    position = if (isHeld) target + 1 else index + 1,
                    lifted = isHeld,
                    offsetY = if (isHeld) drag else settled,
                    onStart = {
                        held = item
                        drag = 0f
                        onDragging(true)
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { drag += it },
                    onEnd = {
                        val list = latest
                        val start = list.indexOf(held)
                        val end = targetFor(held, list, drag)
                        held = null
                        drag = 0f
                        onDragging(false)
                        if (start >= 0 && end >= 0 && start != end) onMove(start, end)
                    },
                )
            }
        }
    }
}

@Composable
private fun ToolbarRow(
    item: ToolbarItem,
    position: Int,
    lifted: Boolean,
    offsetY: Float,
    onStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onEnd: () -> Unit,
) {
    val colors = SpaceTheme.colors
    val label = stringResource(item.labelRes)
    val hint = stringResource(R.string.editor_toolbar_handle, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .zIndex(if (lifted) 1f else 0f)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .then(if (lifted) Modifier.shadow(10.dp, RowShape) else Modifier)
            .background(if (lifted) colors.primaryContainer else colors.containerHigh, RowShape)
            .border(if (lifted) 2.dp else 1.dp, if (lifted) colors.primary else colors.outline, RowShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp)
                .semantics { contentDescription = hint }
                .pointerInput(item) {
                    detectVerticalDragGestures(
                        onDragStart = { onStart() },
                        onDragEnd = onEnd,
                        onDragCancel = onEnd,
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            onDrag(amount)
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            GripDots(colors.onSurfaceVariant)
        }
        SpaceText(label, SpaceTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        SpaceText(position.toString(), SpaceTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun GripDots(color: Color) {
    Canvas(modifier = Modifier.size(width = 14.dp, height = 20.dp)) {
        val r = 1.8.dp.toPx()
        for (col in 0..1) for (row in 0..2) {
            drawCircle(color, r, Offset(size.width * (0.25f + 0.5f * col), size.height * (0.15f + 0.35f * row)))
        }
    }
}
