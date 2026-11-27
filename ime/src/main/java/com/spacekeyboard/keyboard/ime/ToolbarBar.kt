package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import com.spacekeyboard.keyboard.core.toolbar.BarArrangement
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import kotlin.math.roundToInt

internal const val STRIP_ID = "strip"

private const val LIFT_SCALE = 1.14f
private const val LIFT_GLOW = 0.22f

internal class BarButton(
    val icon: SpaceIcon?,
    val label: String,
    val description: String,
    val selected: Boolean,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

internal class BarGeometry {
    val left = HashMap<String, Float>()
    val width = HashMap<String, Float>()
    var total = 0f

    fun record(items: Map<String, Placeable>, leftIds: List<String>, rightIds: List<String>, barWidth: Int) {
        left.clear()
        width.clear()
        total = barWidth.toFloat()
        var x = 0f
        leftIds.forEach { id -> x = put(id, x, items.getValue(id).width) }
        x = barWidth - rightIds.sumOf { items.getValue(it).width }.toFloat()
        rightIds.forEach { id -> x = put(id, x, items.getValue(id).width) }
    }

    private fun put(id: String, x: Float, w: Int): Float {
        left[id] = x
        width[id] = w.toFloat()
        return x + w
    }
}

@Stable
internal class BarDrag(
    private val geometry: BarGeometry,
    private val current: () -> BarArrangement,
    private val commit: (BarArrangement) -> Unit,
    private val feedback: (SpaceHaptic) -> Unit,
) {
    var picked by mutableStateOf<String?>(null)
        private set
    var centerX by mutableFloatStateOf(0f)
        private set
    var live by mutableStateOf<BarArrangement?>(null)
        private set

    fun start(id: String) {
        val x = geometry.left[id] ?: return
        centerX = x + (geometry.width[id] ?: 0f) / 2f
        live = current()
        picked = id
        feedback(SpaceHaptic.LongPress)
    }

    fun move(dx: Float) {
        val id = picked ?: return
        val half = (geometry.width[id] ?: 0f) / 2f
        centerX = (centerX + dx).coerceIn(half, maxOf(half, geometry.total - half))
        val next = (live ?: current()).dropAt(id, centerX, geometry.width, geometry.total)
        if (next == live) return
        live = next
        feedback(SpaceHaptic.Tick)
    }

    fun finish() {
        val result = live
        picked = null
        if (result == null || result == current()) {
            live = null
            return
        }
        feedback(SpaceHaptic.DragEnd)
        commit(result)
    }

    fun settle() {
        if (picked == null) live = null
    }
}

@Composable
internal fun ToolbarBar(
    bar: BarArrangement,
    buttons: Map<String, BarButton>,
    words: Boolean,
    look: ButtonLook,
    onArrange: (BarArrangement) -> Unit,
    modifier: Modifier,
    strip: @Composable () -> Unit,
) {
    val geometry = remember { BarGeometry() }
    val haptics = rememberSpaceHaptics()
    val latest by rememberUpdatedState(bar)
    val commit by rememberUpdatedState(onArrange)
    val vibrate by rememberUpdatedState(look.vibrate)
    val drag = remember { BarDrag(geometry, { latest }, { commit(it) }, { if (vibrate) haptics.perform(it) }) }
    LaunchedEffect(bar) { drag.settle() }
    BarLayout({ drag.live ?: bar }, { drag.picked }, { drag.centerX }, geometry, modifier) {
        buttons.forEach { (id, button) ->
            key(id) {
                BarSlot(id, button, drag, look, !words) { delta -> onArrange(latest.step(id, delta)) }
            }
        }
        if (words) Box(Modifier.layoutId(STRIP_ID)) { strip() }
    }
}

@Composable
private fun BarSlot(
    id: String,
    button: BarButton,
    drag: BarDrag,
    look: ButtonLook,
    canDrag: Boolean,
    onStep: (Int) -> Unit,
) {
    val lifted = drag.picked == id
    val scale = if (lifted) LIFT_SCALE else 1f
    val glow = if (lifted) Modifier.background(Color(look.palette.glow).copy(alpha = LIFT_GLOW), CircleShape) else Modifier
    val earlier = stringResource(R.string.toolbar_move_left)
    val later = stringResource(R.string.toolbar_move_right)
    Box(
        modifier = Modifier
            .layoutId(id)
            .semantics(mergeDescendants = true) {
                customActions = listOf(
                    CustomAccessibilityAction(earlier) { onStep(-1); true },
                    CustomAccessibilityAction(later) { onStep(1); true },
                )
            }
            .pointerInput(id, canDrag) {
                if (!canDrag) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { drag.start(id) },
                    onDrag = { change, amount ->
                        change.consume()
                        drag.move(amount.x)
                    },
                    onDragEnd = drag::finish,
                    onDragCancel = drag::finish,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }.then(glow)) {
            BarButtonView(button, look) { if (drag.picked == null) button.onClick() }
        }
    }
}

@Composable
private fun BarButtonView(button: BarButton, look: ButtonLook, onClick: () -> Unit) {
    val icon = button.icon
    if (icon != null) {
        ToolbarButton(icon, button.description, look, onClick, button.selected, button.enabled)
    } else {
        ToolbarTextButton(button.label, button.description, look, onClick, button.selected)
    }
}

@Composable
private fun BarLayout(
    arrangement: () -> BarArrangement,
    picked: () -> String?,
    pickedCenter: () -> Float,
    geometry: BarGeometry,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        arrange(measurables, constraints, arrangement(), picked(), pickedCenter(), geometry)
    }
}

private fun MeasureScope.arrange(
    measurables: List<Measurable>,
    constraints: Constraints,
    arrangement: BarArrangement,
    picked: String?,
    pickedCenter: Float,
    geometry: BarGeometry,
): MeasureResult {
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    val buttons = measurables.filter { it.layoutId != STRIP_ID }
    val cap = (width / buttons.size.coerceAtLeast(1)).coerceAtLeast(0)
    val items = LinkedHashMap<String, Placeable>()
    buttons.forEach { m -> (m.layoutId as? String)?.let { items[it] = m.measure(Constraints(maxWidth = cap, maxHeight = height)) } }
    val leftIds = arrangement.left.filter { it in items }
    val rightIds = arrangement.right.filter { it in items }
    val leftWidth = leftIds.sumOf { items.getValue(it).width }
    val rightWidth = rightIds.sumOf { items.getValue(it).width }
    val gap = (width - leftWidth - rightWidth).coerceAtLeast(0)
    val strip = measurables.firstOrNull { it.layoutId == STRIP_ID }?.measure(Constraints.fixed(gap, height))
    geometry.record(items, leftIds, rightIds, width)
    return layout(width, height) {
        var x = 0
        leftIds.forEach { id -> x = placeAt(items.getValue(id), x, height, id != picked) }
        x = width - rightWidth
        rightIds.forEach { id -> x = placeAt(items.getValue(id), x, height, id != picked) }
        strip?.place(leftWidth, 0)
        val lifted = picked?.let { items[it] } ?: return@layout
        val free = (pickedCenter - lifted.width / 2f).roundToInt().coerceIn(0, (width - lifted.width).coerceAtLeast(0))
        lifted.place(free, (height - lifted.height) / 2)
    }
}

private fun Placeable.PlacementScope.placeAt(item: Placeable, x: Int, height: Int, draw: Boolean): Int {
    if (draw) item.place(x, (height - item.height) / 2)
    return x + item.width
}
