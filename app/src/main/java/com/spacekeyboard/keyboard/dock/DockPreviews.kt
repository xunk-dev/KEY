package com.spacekeyboard.keyboard.dock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.AppTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
private fun DockPreviewFrame(selected: DockTab) {
    AppTheme {
        val palette = rememberDockPalette()
        val layer = rememberGraphicsLayer()
        val backdrop = remember(layer) { Backdrop(layer) }
        Box(Modifier.fillMaxWidth().background(SpaceTheme.colors.background).padding(16.dp), Alignment.BottomStart) {
            Row(horizontalArrangement = Arrangement.spacedBy(DockMetrics.BarGap)) {
                KeyboardFab(palette, {})
                DockBar(selected, {}, palette, backdrop, { Offset.Zero })
            }
        }
    }
}

@Preview(widthDp = 400, heightDp = 120, showBackground = true)
@Composable
private fun DockHomePreview() = DockPreviewFrame(DockTab.HOME)

@Preview(widthDp = 400, heightDp = 120, showBackground = true)
@Composable
private fun DockCustomizePreview() = DockPreviewFrame(DockTab.CUSTOMIZE)

@Preview(widthDp = 400, heightDp = 120, showBackground = true)
@Composable
private fun DockSettingsPreview() = DockPreviewFrame(DockTab.SETTINGS)
