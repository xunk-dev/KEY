package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelKey
import com.spacekeyboard.keyboard.symbols.R
import com.spacekeyboard.keyboard.symbols.state.PanelState
import com.spacekeyboard.keyboard.symbols.state.PanelTab
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel

private val RemoveBadge = Color(0xFFD92D3A)

@Composable
internal fun RecentsPage(state: PanelState, palette: ThemePalette, model: SymbolPanelModel, onInsert: (String) -> Unit) {
    var confirming by remember(state.recents.isEmpty()) { mutableStateOf(false) }
    if (state.recents.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SpaceText(stringResource(R.string.recent_empty), SpaceTheme.typography.bodySmall, color = Color(palette.label))
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpaceText(
                stringResource(if (confirming) R.string.recent_clear_confirm else R.string.cat_recent),
                SpaceTheme.typography.bodySmall,
                color = Color(palette.label),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                when {
                    confirming -> {
                        HeaderKey(stringResource(R.string.recent_keep), palette) { confirming = false }
                        HeaderKey(stringResource(R.string.recent_clear_all), palette) {
                            confirming = false
                            model.clearRecents()
                        }
                    }
                    state.editing -> {
                        HeaderKey(stringResource(R.string.recent_clear_all), palette) { confirming = true }
                        HeaderKey(stringResource(R.string.recent_done), palette) { model.setEditing(false) }
                    }
                    else -> HeaderKey(stringResource(R.string.recent_edit), palette) { model.setEditing(true) }
                }
            }
        }
        RecentGrid(state, palette, model, onInsert)
    }
}

@Composable
private fun HeaderKey(text: String, palette: ThemePalette, onClick: () -> Unit) {
    PanelKey(palette, text, Modifier, onClick = onClick) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.keyText), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
private fun RecentGrid(state: PanelState, palette: ThemePalette, model: SymbolPanelModel, onInsert: (String) -> Unit) {
    val art = state.tab == PanelTab.ART
    val faces = state.tab == PanelTab.FACES
    val min = when { art -> 150.dp; faces -> 104.dp; else -> 46.dp }
    val size = when { art -> 13.sp; faces -> 16.sp; else -> 22.sp }
    val tap = rememberTap()
    val remove = stringResource(R.string.recent_remove)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(min),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(state.recents, key = { it }) { text ->
            RecentCell(text, palette, art, size, state.editing, remove) {
                tap()
                if (state.editing) model.removeRecent(text) else onInsert(text)
            }
        }
    }
}

@Composable
private fun RecentCell(text: String, palette: ThemePalette, art: Boolean, size: TextUnit, editing: Boolean, removeLabel: String, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(palette.keyFill))
                .semantics { if (editing) contentDescription = removeLabel }
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = text,
                maxLines = if (art) 4 else 1,
                style = TextStyle(color = Color(palette.keyText), fontSize = size, fontFamily = FontFamily.Default, textAlign = TextAlign.Center),
            )
        }
        if (editing) RemoveMark(Modifier.align(Alignment.TopStart))
    }
}

@Composable
private fun RemoveMark(modifier: Modifier) {
    Box(
        modifier = modifier.offset(x = (-3).dp, y = (-3).dp).size(18.dp).background(RemoveBadge, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(width = 9.dp, height = 2.dp).background(Color.White, CircleShape))
    }
}
