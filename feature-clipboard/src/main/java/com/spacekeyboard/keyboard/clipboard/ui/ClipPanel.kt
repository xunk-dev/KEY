package com.spacekeyboard.keyboard.clipboard.ui

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.BusyKind
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.rememberClipSnapshot
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode

private const val SCRIM_ALPHA = 0.55f
private const val SWIPE_DP = 56
private const val SLIDE_DP = 36
private const val SLIDE_MS = 200
private const val SLIDE_FADE = 0.6f

@Composable
fun ClipPanelHost(controller: ClipboardController, palette: ThemePalette, actions: ClipActions) {
    val state = rememberClipSnapshot(controller)
    val rtl = LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL
    CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
            when (state.mode) {
                ClipMode.BROWSE -> state.viewing?.let { ClipFullView(it, palette, actions) } ?: ClipPanel(state, palette, actions)
                ClipMode.NAMING -> ClipNameBar(state, palette, actions)
                ClipMode.CLOSED -> Unit
            }
        }
    }
}

@Composable
private fun ClipPanel(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { ClipTabs(state, palette, actions) }
            ClipSmartSlot(state, palette, actions)
            ClipClearSlot(state, palette, actions)
        }
        if (!state.enabled) ClipEnableStrip(palette, actions)
        if (state.tab == ClipTab.SAVED) ClipSectionBar(state, palette, actions)
        if (state.showKinds && state.confirm == null && state.assigning == null) ClipKindBar(state, palette, actions)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            SwipeTabs(state.tab, state.order, rtl, actions::onTab, Modifier.fillMaxSize().background(Color(palette.background).copy(alpha = SCRIM_ALPHA))) {
                ClipBody(state, palette, actions)
            }
            ClipFloatingBar(state, palette, actions, Modifier.align(Alignment.BottomStart))
        }
        state.busy?.takeIf { it.kind == BusyKind.PASTING }?.let { PasteStrip(it, palette, actions) }
    }
}

@Composable
private fun SwipeTabs(tab: ClipTab, order: List<ClipTab>, rtl: Boolean, onTab: (ClipTab) -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val threshold = with(density) { SWIPE_DP.dp.toPx() }
    val slide = with(density) { SLIDE_DP.dp.toPx() }
    val offset = remember { Animatable(0f) }
    var previous by remember { mutableStateOf(tab) }
    val latestOrder by rememberUpdatedState(order)
    LaunchedEffect(tab) {
        val step = latestOrder.indexOf(tab) - latestOrder.indexOf(previous)
        previous = tab
        if (step != 0) {
            val from = if (rtl) -step * slide else step * slide
            offset.snapTo(from)
            offset.animateTo(0f, tween(SLIDE_MS))
        }
    }
    Box(
        modifier
            .pointerInput(tab, order, rtl) {
                var total = 0f
                detectHorizontalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = {
                        if (abs(total) >= threshold) {
                            val forward = if (rtl) total > 0f else total < 0f
                            val next = order.indexOf(tab) + if (forward) 1 else -1
                            order.getOrNull(next)?.let(onTab)
                        }
                    },
                    onDragCancel = { total = 0f },
                    onHorizontalDrag = { _, delta -> total += delta },
                )
            }
            .graphicsLayer {
                translationX = offset.value
                alpha = 1f - (abs(offset.value) / slide).coerceIn(0f, 1f) * SLIDE_FADE
            },
    ) { content() }
}
