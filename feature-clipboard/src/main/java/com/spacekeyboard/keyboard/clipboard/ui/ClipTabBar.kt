package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.ClipTabOrder
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptics
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val HELD_SCALE = 1.06f
private const val HELD_LIFT = 10f
private val CELL_SHAPE = RoundedCornerShape(10.dp)

@Stable
private class TabReorder {
    var tab by mutableStateOf<ClipTab?>(null)
        private set
    var preview by mutableStateOf(ClipTabOrder.DEFAULT)
        private set
    var left by mutableFloatStateOf(0f)
        private set
    private var settle: Job? = null

    val active: Boolean get() = tab != null

    fun start(order: List<ClipTab>, x: Float, width: Float, rtl: Boolean): Boolean {
        if (order.size < 2 || width <= 0f) return false
        settle?.cancel()
        val slot = ClipTabOrder.slotAt(x, width, order.size, rtl)
        tab = order[slot]
        preview = order
        left = ClipTabOrder.slotLeft(slot, width / order.size, order.size, rtl)
        return true
    }

    fun move(dx: Float, width: Float, rtl: Boolean): Boolean {
        val held = tab ?: return false
        val count = preview.size
        val cell = width / count
        left = (left + dx).coerceIn(0f, width - cell)
        val target = ClipTabOrder.slotAt(left + cell / 2f, width, count, rtl)
        if (target == preview.indexOf(held)) return false
        preview = ClipTabOrder.moveTo(preview, held, target)
        return true
    }

    fun finish(commit: Boolean, saved: List<ClipTab>, width: Float, rtl: Boolean, scope: CoroutineScope, save: (List<ClipTab>) -> Unit) {
        val held = tab ?: return
        val result = if (commit) preview else saved
        preview = result
        if (result != saved) save(result)
        val count = result.size
        val rest = ClipTabOrder.slotLeft(result.indexOf(held), width / count, count, rtl)
        val from = left
        settle = scope.launch {
            Animatable(from).animateTo(rest, tween(INDICATOR_MS)) { left = value }
            tab = null
        }
    }
}

@Composable
fun ClipTabs(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val reorder = remember { TabReorder() }
    val scope = rememberCoroutineScope()
    val haptics = rememberSpaceHaptics()
    val latest by rememberUpdatedState(state)
    val latestActions by rememberUpdatedState(actions)
    val gesture = Modifier.reorderGesture(reorder, rtl, scope, haptics, { latest.order }) { latestActions.onTabOrder(it) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(40.dp).underline(Color(palette.border).copy(alpha = LINE_ALPHA)).then(gesture)) {
            val shown = if (reorder.active) reorder.preview else state.order
            TabCells(state, palette, actions, reorder, shown, rtl)
            TabIndicator(state, palette, reorder, shown, rtl)
        }
    }
}

internal fun Modifier.underline(line: Color): Modifier = drawBehind {
    val thickness = 1.dp.toPx()
    drawRect(line, Offset(0f, size.height - thickness), Size(size.width, thickness))
}

private fun Modifier.reorderGesture(
    reorder: TabReorder,
    rtl: Boolean,
    scope: CoroutineScope,
    haptics: SpaceHaptics,
    order: () -> List<ClipTab>,
    save: (List<ClipTab>) -> Unit,
): Modifier = pointerInput(rtl) {
    detectDragGesturesAfterLongPress(
        onDragStart = { at ->
            if (reorder.start(order(), at.x, size.width.toFloat(), rtl)) haptics.perform(SpaceHaptic.LongPress)
        },
        onDrag = { change, amount ->
            if (reorder.active) {
                change.consume()
                if (reorder.move(amount.x, size.width.toFloat(), rtl)) haptics.perform(SpaceHaptic.Tick)
            }
        },
        onDragEnd = {
            if (reorder.active) haptics.perform(SpaceHaptic.DragEnd)
            reorder.finish(true, order(), size.width.toFloat(), rtl, scope, save)
        },
        onDragCancel = { reorder.finish(false, order(), size.width.toFloat(), rtl, scope, save) },
    )
}

@Composable
private fun BoxWithConstraintsScope.TabCells(
    state: ClipSnapshot,
    palette: ThemePalette,
    actions: ClipActions,
    reorder: TabReorder,
    shown: List<ClipTab>,
    rtl: Boolean,
) {
    val count = shown.size
    val cellPx = with(LocalDensity.current) { maxWidth.toPx() } / count
    val cellDp = maxWidth / count
    for (tab in ClipTab.entries) {
        key(tab) {
            val slotX = ClipTabOrder.slotLeft(shown.indexOf(tab), cellPx, count, rtl)
            val settled by animateFloatAsState(slotX, tween(INDICATOR_MS), label = "clipTabX")
            val held = reorder.tab == tab
            ClipTabCell(
                tab, state, palette, held,
                Modifier
                    .width(cellDp)
                    .fillMaxHeight()
                    .zIndex(if (held) 1f else 0f)
                    .offset { IntOffset((if (held) reorder.left else settled).roundToInt(), 0) },
                onMove = { delta -> actions.onTabOrder(ClipTabOrder.shift(state.order, tab, delta)) },
            ) { if (!reorder.active) actions.onTab(tab) }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.TabIndicator(state: ClipSnapshot, palette: ThemePalette, reorder: TabReorder, shown: List<ClipTab>, rtl: Boolean) {
    val count = shown.size
    val cellPx = with(LocalDensity.current) { maxWidth.toPx() } / count
    val selectedX = ClipTabOrder.slotLeft(shown.indexOf(state.tab), cellPx, count, rtl)
    val indicatorX by animateFloatAsState(selectedX, tween(INDICATOR_MS), label = "clipTabIndicator")
    Box(
        Modifier
            .align(Alignment.BottomStart)
            .offset { IntOffset(((if (reorder.tab == state.tab) reorder.left else indicatorX) + cellPx * INDICATOR_INSET).roundToInt(), 0) }
            .width(maxWidth / count * (1f - 2 * INDICATOR_INSET))
            .height(3.dp)
            .background(Color(palette.glow), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
    )
}

@Composable
private fun ClipTabCell(
    tab: ClipTab,
    state: ClipSnapshot,
    palette: ThemePalette,
    held: Boolean,
    modifier: Modifier,
    onMove: (Int) -> Unit,
    onClick: () -> Unit,
) {
    val isSelected = tab == state.tab
    val haptics = rememberSpaceHaptics()
    val label = stringResource(ClipLabels.tab(tab))
    val earlier = stringResource(R.string.clip_tab_move_earlier)
    val later = stringResource(R.string.clip_tab_move_later)
    val index = state.order.indexOf(tab)
    val last = state.order.lastIndex
    val count = state.counts[tab] ?: 0
    val base = Color(palette.label)
    val color = if (isSelected) base else base.copy(alpha = DIM_ALPHA)
    val weight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
    Box(
        modifier = modifier
            .graphicsLayer {
                if (held) {
                    scaleX = HELD_SCALE
                    scaleY = HELD_SCALE
                    shadowElevation = HELD_LIFT
                    shape = CELL_SHAPE
                    clip = false
                }
            }
            .background(if (held) Color(palette.keyFill) else Color.Transparent, CELL_SHAPE)
            .semantics {
                contentDescription = label
                selected = isSelected
                customActions = buildList {
                    if (index > 0) add(CustomAccessibilityAction(earlier) { onMove(-1); true })
                    if (index < last) add(CustomAccessibilityAction(later) { onMove(1); true })
                }
            }
            .clickable(role = Role.Tab) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            SpaceText(label, SpaceTheme.typography.bodySmall.copy(fontWeight = weight), color = color)
            if (count > 0) CountBadge(count, palette, isSelected)
        }
    }
}
