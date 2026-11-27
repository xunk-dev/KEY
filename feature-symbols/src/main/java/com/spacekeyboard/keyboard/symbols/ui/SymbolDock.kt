package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.emoji.ui.DockAction
import com.spacekeyboard.keyboard.emoji.ui.DockBar
import com.spacekeyboard.keyboard.emoji.ui.DockCategory
import com.spacekeyboard.keyboard.emoji.ui.DockDelete
import com.spacekeyboard.keyboard.symbols.R
import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.state.PanelState
import com.spacekeyboard.keyboard.symbols.state.PanelTab
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel

private val dockCategories = SymbolCategory.entries

@Composable
internal fun SymbolDock(state: PanelState, palette: ThemePalette, model: SymbolPanelModel, onDelete: () -> Unit) {
    val listState = rememberLazyListState()
    val active = activeIndex(state)
    LaunchedEffect(state.tab, active) { listState.animateScrollToItem((active - 1).coerceAtLeast(0)) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        DockBar(palette) {
            DockAction(SpaceIcon.KEYS, stringResource(R.string.panel_back), palette, model::close)
            LazyRow(
                modifier = Modifier.weight(1f),
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dockItems(state, palette, model)
            }
            DockDelete(palette, stringResource(R.string.panel_delete), onDelete)
        }
    }
}

private fun activeIndex(state: PanelState): Int {
    if (state.recentsOpen) return 0
    return 1 + when (state.tab) {
        PanelTab.FACES -> FaceCategory.entries.indexOf(state.faceCategory)
        PanelTab.ART -> ArtCategory.entries.indexOf(state.artCategory)
        PanelTab.SYMBOLS -> dockCategories.indexOf(state.category)
    }
}

private fun LazyListScope.dockItems(state: PanelState, palette: ThemePalette, model: SymbolPanelModel) {
    item(key = "recent") {
        DockCategory(SpaceIcon.RECENTS, stringResource(R.string.cat_recent), palette, state.recentsOpen, model::showRecents)
    }
    if (state.tab == PanelTab.ART) {
        items(ArtCategory.entries, key = { it.id }) { category ->
            DockCategory(category.icon(), stringResource(category.labelRes), palette, category == state.artCategory && !state.recentsOpen) {
                model.selectArtCategory(category)
            }
        }
    } else if (state.tab == PanelTab.FACES) {
        items(FaceCategory.entries, key = { it.id }) { category ->
            DockCategory(category.icon(), stringResource(category.labelRes), palette, category == state.faceCategory && !state.recentsOpen) {
                model.selectFaceCategory(category)
            }
        }
    } else {
        items(dockCategories, key = { it.id }) { category ->
            DockCategory(category.icon(), stringResource(category.labelRes), palette, category == state.category && !state.recentsOpen) {
                model.selectCategory(category)
            }
        }
    }
}
