package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.clipboard.rememberClipSnapshot
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode

private const val SCRIM_ALPHA = 0.55f

@Composable
fun ClipPanelHost(controller: ClipboardController, palette: ThemePalette, actions: ClipActions) {
    val state = rememberClipSnapshot(controller)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark) {
            when (state.mode) {
                ClipMode.BROWSE -> ClipPanel(state, palette, actions)
                ClipMode.NAMING -> ClipNamePanel(state, palette, actions)
                ClipMode.CLOSED -> Unit
            }
        }
    }
}

@Composable
private fun ClipPanel(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Column(modifier = Modifier.fillMaxSize()) {
        ClipBar(state, palette, actions)
        if (state.tab == ClipTab.SAVED) ClipSectionBar(state, palette, actions)
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(palette.background).copy(alpha = SCRIM_ALPHA))) {
            ClipBody(state, palette, actions)
        }
    }
}

@Composable
private fun ClipNamePanel(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Column(Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
            val text = state.naming.ifEmpty { stringResource(R.string.clip_section_name) }
            val color = if (state.naming.isEmpty()) palette.label and HINT_MASK else palette.label
            SpaceText(text, SpaceTheme.typography.body, color = Color(color), modifier = Modifier.padding(horizontal = 8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClipKey(stringResource(R.string.clip_ok), palette, selected = true, icon = PanelIcon.DONE, onClick = actions::onNameOk)
            ClipKey(stringResource(R.string.clip_cancel), palette, onClick = actions::onNameCancel)
        }
    }
}

private const val HINT_MASK = 0x99FFFFFF.toInt()
