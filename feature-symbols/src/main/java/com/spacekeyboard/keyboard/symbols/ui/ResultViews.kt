package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.symbols.R
import com.spacekeyboard.keyboard.symbols.state.PanelState
import com.spacekeyboard.keyboard.symbols.state.PanelTab
import java.util.Locale

@Composable
internal fun ResultGrid(
    state: PanelState,
    palette: ThemePalette,
    modifier: Modifier,
    onInsert: (String) -> Unit,
) {
    val arabic = remember { Locale.getDefault().language == "ar" }
    val faces = state.tab == PanelTab.FACES
    val empty = if (faces) state.faces.isEmpty() else state.symbols.isEmpty()
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (empty) {
            Note(stringResource(R.string.panel_empty), palette, state.loaded)
        } else if (faces) {
            FaceGrid(state, palette, onInsert)
        } else {
            SymbolGrid(state, palette, arabic, onInsert)
        }
    }
}

@Composable
private fun SymbolGrid(state: PanelState, palette: ThemePalette, arabic: Boolean, onInsert: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(46.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(state.symbols, key = { it.codePoint }) { entry ->
            SymbolCell(entry, palette, arabic, onInsert = onInsert)
        }
    }
}

@Composable
private fun FaceGrid(state: PanelState, palette: ThemePalette, onInsert: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(104.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(state.faces, key = { it.text }) { entry ->
            FaceCell(entry, palette, onInsert = onInsert)
        }
    }
}

@Composable
internal fun ResultStrip(state: PanelState, palette: ThemePalette, onInsert: (String) -> Unit) {
    val arabic = remember { Locale.getDefault().language == "ar" }
    val faces = state.tab == PanelTab.FACES
    val empty = if (faces) state.faces.isEmpty() else state.symbols.isEmpty()
    Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
        if (empty) {
            val text = if (state.query.isEmpty()) R.string.panel_search_start else R.string.panel_empty
            Note(stringResource(text), palette, state.loaded || state.query.isEmpty())
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (faces) {
                    items(state.faces, key = { it.text }) { FaceCell(it, palette, Modifier.width(104.dp), onInsert) }
                } else {
                    items(state.symbols, key = { it.codePoint }) {
                        SymbolCell(it, palette, arabic, Modifier.width(48.dp), onInsert)
                    }
                }
            }
        }
    }
}

@Composable
private fun Note(text: String, palette: ThemePalette, visible: Boolean) {
    if (visible) SpaceText(text = text, style = SpaceTheme.typography.bodySmall, color = Color(palette.label))
}
