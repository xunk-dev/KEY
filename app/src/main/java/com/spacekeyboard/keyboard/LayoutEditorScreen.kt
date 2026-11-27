package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController

@Composable
fun LayoutEditorScreen(theme: ThemeController, layouts: LayoutController, onBack: () -> Unit) {
    val snapshot = rememberLayoutSnapshot(layouts)
    var selected by remember { mutableStateOf(LayoutCatalog.entries.first { it.id == snapshot.prefs.language }) }
    var dragging by remember { mutableStateOf(false) }
    ScreenFrame(R.string.editor_title, onBack, scrollable = !dragging) {
        BodyText(R.string.editor_hint)
        ChipGrid(LayoutCatalog.entries, selected, { it.nameRes }, { selected = it })
        key(selected.id) {
            KeyboardPreview(
                theme = theme,
                layouts = layouts,
                fixedLayoutId = selected.id,
                editing = true,
                onSwap = { a, b -> layouts.swap(selected.id, a, b) },
                onDragging = { dragging = it },
            )
        }
        SpaceButton(stringResource(R.string.reset_layout), onClick = { layouts.resetOrder(selected.id) })
        ToolbarSection(snapshot.prefs.toolbar, layouts) { dragging = it }
    }
}

@Composable
private fun ToolbarSection(items: List<ToolbarItem>, layouts: LayoutController, onDragging: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SpaceSectionTitle(stringResource(R.string.editor_toolbar))
        BodyText(R.string.editor_toolbar_hint)
        BodyText(R.string.editor_toolbar_bar_hint)
        ToolbarEditor(items, layouts::moveToolbar, onDragging)
        SpaceButton(stringResource(R.string.reset_toolbar), layouts::resetToolbar)
    }
}
