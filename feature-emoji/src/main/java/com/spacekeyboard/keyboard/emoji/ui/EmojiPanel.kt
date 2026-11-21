package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.LoadState
import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.RECENTS_GROUP

private const val SCRIM_ALPHA = 0.55f

@Composable
fun EmojiPanel(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark) {
            Column(modifier = Modifier.fillMaxSize()) {
                EmojiTopBar(state, palette, actions, onKeys = actions::onKeys)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(palette.background).copy(alpha = SCRIM_ALPHA)),
                ) {
                    PanelBody(state, palette, actions)
                }
            }
        }
    }
}

@Composable
private fun PanelBody(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    when (state.load) {
        LoadState.READY -> GroupContent(state, palette, actions)
        LoadState.FAILED -> CenterText(stringResource(R.string.emoji_failed), palette)
        else -> CenterText(stringResource(R.string.emoji_loading), palette)
    }
}

@Composable
private fun GroupContent(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    val active = state.activeGroup
    if (active == RECENTS_GROUP) {
        RecentsContent(state, palette, actions)
        return
    }
    val group = state.groups.firstOrNull { it.id == active } ?: return
    val texts = remember(group, state.tone) { group.emojis.map { it.forTone(state.tone) } }
    key(active) {
        EmojiGrid(texts, state.typeface, actions::onPick)
    }
}

@Composable
private fun RecentsContent(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    var confirming by remember(state.recents.isEmpty()) { mutableStateOf(false) }
    if (state.recents.isEmpty()) {
        CenterText(stringResource(R.string.emoji_recents_empty), palette)
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpaceText(
                stringResource(if (confirming) R.string.emoji_clear_confirm else R.string.emoji_recents),
                SpaceTheme.typography.bodySmall,
                color = Color(palette.label),
            )
            ClearButtons(confirming, palette, onAsk = { confirming = true }, onNo = { confirming = false }) {
                confirming = false
                actions.onClearRecents()
            }
        }
        Box(modifier = Modifier.weight(1f)) { EmojiGrid(state.recents, state.typeface, actions::onPick) }
    }
}

@Composable
private fun ClearButtons(confirming: Boolean, palette: ThemePalette, onAsk: () -> Unit, onNo: () -> Unit, onYes: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (confirming) {
            SmallKey(stringResource(R.string.emoji_clear_no), palette, onNo)
            SmallKey(stringResource(R.string.emoji_clear_yes), palette, onYes)
        } else {
            SmallKey(stringResource(R.string.emoji_clear), palette, onAsk, PanelIcon.CLEAR_ALL)
        }
    }
}

@Composable
private fun SmallKey(text: String, palette: ThemePalette, onClick: () -> Unit, icon: PanelIcon? = null) {
    PanelKey(palette, text, Modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) PanelIconView(icon, Color(palette.keyText), Modifier.padding(start = 10.dp).size(16.dp))
            SpaceText(
                text,
                SpaceTheme.typography.bodySmall,
                color = Color(palette.keyText),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun CenterText(text: String, palette: ThemePalette) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label))
    }
}
