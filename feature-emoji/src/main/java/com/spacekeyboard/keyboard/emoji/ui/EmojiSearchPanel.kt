package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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
import com.spacekeyboard.keyboard.emoji.R

private const val CARET = "|"

@Composable
fun EmojiSearchPanel(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PanelKey(palette, stringResource(R.string.emoji_search_close), Modifier.size(48.dp), onClick = actions::onCloseSearch) {
                        PanelIconView(PanelIcon.CLOSE, Color(palette.keyText))
                    }
                    QueryBox(state.query, palette, Modifier.weight(1f).fillMaxHeight())
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) { Results(state, palette, actions) }
            }
        }
    }
}

@Composable
private fun QueryBox(query: String, palette: ThemePalette, modifier: Modifier) {
    Box(
        modifier = modifier.panelKeyLook(palette, false).padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        SpaceText(query + CARET, SpaceTheme.typography.body, color = Color(palette.keyText))
    }
}

@Composable
private fun Results(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    when {
        state.query.isBlank() -> CenterText(stringResource(R.string.emoji_search_hint), palette)
        !state.searchReady -> CenterText(stringResource(R.string.emoji_search_loading), palette)
        state.results.isEmpty() -> CenterText(stringResource(R.string.emoji_search_none), palette)
        else -> {
            val texts = remember(state.results, state.tone) { state.results.map { it.forTone(state.tone) } }
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                items(texts.size) { index ->
                    EmojiCellView(texts[index], state.typeface) { actions.onPick(texts[index]) }
                }
            }
        }
    }
}
