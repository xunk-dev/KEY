package com.spacekeyboard.keyboard.dock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.AppTheme

@Composable
internal fun DockHost(
    selected: DockTab,
    dockVisible: Boolean,
    onSelect: (DockTab) -> Unit,
    content: @Composable () -> Unit,
) {
    val layer = rememberGraphicsLayer()
    val backdrop = remember(layer) { Backdrop(layer) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val reserve by animateDpAsState(
        targetValue = if (dockVisible && !keyboardUp) DockMetrics.ContentReserve else 0.dp,
        label = "dockReserve",
    )
    Box(Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInRoot() }) {
        Box(Modifier.fillMaxSize().recordBackdrop(backdrop)) {
            CompositionLocalProvider(LocalDockInset provides reserve) { content() }
        }
        AppTheme {
            DockOverlay(selected, dockVisible && !keyboardUp && !sheetOpen, onSelect, backdrop, { origin }) { sheetOpen = true }
            TrySheet(sheetOpen) { sheetOpen = false }
        }
    }
}

@Composable
private fun BoxScope.DockOverlay(
    selected: DockTab,
    visible: Boolean,
    onSelect: (DockTab) -> Unit,
    backdrop: Backdrop,
    origin: () -> Offset,
    onTryKeyboard: () -> Unit,
) {
    val palette = rememberDockPalette()
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.BottomStart),
        enter = slideInVertically(spring(0.8f, 400f)) { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = DockMetrics.BarSideMargin, bottom = DockMetrics.BarBottomMargin),
            horizontalArrangement = Arrangement.spacedBy(DockMetrics.BarGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyboardFab(palette, onTryKeyboard)
            DockBar(selected, onSelect, palette, backdrop, origin)
        }
    }
}
