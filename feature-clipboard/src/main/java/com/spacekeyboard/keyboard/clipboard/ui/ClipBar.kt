package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon

@Composable
fun ClipBar(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClipKey(stringResource(R.string.clip_keys), palette, onClick = actions::onKeys)
        ClipTab.entries.forEach { tab ->
            ClipKey(stringResource(ClipLabels.tab(tab)), palette, Modifier.weight(1f), tab == state.tab) { actions.onTab(tab) }
        }
        if (state.tab == ClipTab.CURRENT && state.items.isNotEmpty()) {
            ClipKey(stringResource(R.string.clip_clear), palette, icon = PanelIcon.CLEAR_ALL, onClick = actions::onClear)
        }
    }
}

@Composable
fun ClipSectionBar(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(state.sections.size) { index ->
                val section = state.sections[index]
                ClipKey(section.name, palette, selected = section.name == state.section, locked = section.locked) {
                    actions.onSection(section.name)
                }
            }
            item { ClipKey(stringResource(R.string.clip_new_section), palette, onClick = actions::onNewSection) }
        }
        val name = state.section
        if (name != null) SectionTools(name, state, palette, actions)
    }
}

@Composable
private fun SectionTools(name: String, state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    if (!state.locked) {
        val label = if (state.sectionLocked) R.string.clip_unlock_section else R.string.clip_lock_section
        ClipKey(stringResource(label), palette, selected = state.sectionLocked) { actions.onLockSection(name) }
    }
    ClipIconKey(PanelIcon.CLOSE, stringResource(R.string.clip_delete_section), palette) { actions.onDeleteSection(name) }
}
