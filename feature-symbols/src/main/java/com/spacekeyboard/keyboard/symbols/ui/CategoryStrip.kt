package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.state.PanelState
import com.spacekeyboard.keyboard.symbols.state.PanelTab
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel

@Composable
internal fun CategoryStrip(state: PanelState, palette: ThemePalette, model: SymbolPanelModel) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (state.tab == PanelTab.SYMBOLS) {
            items(SymbolCategory.entries, key = { it.id }) { category ->
                PanelButton(stringResource(category.labelRes), palette, category == state.category) {
                    model.selectCategory(category)
                }
            }
        } else {
            items(FaceCategory.entries, key = { it.id }) { category ->
                PanelButton(stringResource(category.labelRes), palette, category == state.faceCategory) {
                    model.selectFaceCategory(category)
                }
            }
        }
    }
}
