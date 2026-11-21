package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipConfirm
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon

@Composable
fun ClipBody(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val assigning = state.assigning
    when {
        state.confirm != null -> ConfirmView(state.confirm, palette, actions)
        assigning != null -> AssignView(assigning, state, palette, actions)
        state.locked -> LockedView(palette, actions)
        state.items.isEmpty() -> Note(stringResource(emptyText(state)), palette)
        else -> ItemList(state.items, palette, actions)
    }
}

private fun emptyText(state: ClipSnapshot): Int = when {
    state.tab == ClipTab.CURRENT && !state.enabled -> R.string.clip_off
    state.tab == ClipTab.CURRENT -> R.string.clip_empty_current
    state.tab == ClipTab.PINNED -> R.string.clip_empty_pinned
    state.section == null -> R.string.clip_no_sections
    else -> R.string.clip_empty_section
}

@Composable
private fun Note(text: String, palette: ThemePalette) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label))
    }
}

@Composable
private fun LockedView(palette: ThemePalette, actions: ClipActions) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpaceText(stringResource(R.string.clip_locked_hint), SpaceTheme.typography.bodySmall, color = Color(palette.label))
        ClipKey(stringResource(R.string.clip_unlock), palette, locked = true, onClick = actions::onUnlock)
    }
}

@Composable
private fun ConfirmView(confirm: ClipConfirm, palette: ThemePalette, actions: ClipActions) {
    val message = if (confirm is ClipConfirm.DeleteSection) R.string.clip_delete_section_confirm else R.string.clip_clear_confirm
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpaceText(stringResource(message), SpaceTheme.typography.bodySmall, color = Color(palette.label))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClipKey(stringResource(R.string.clip_yes), palette, selected = true) { actions.onConfirm(true) }
            ClipKey(stringResource(R.string.clip_no), palette) { actions.onConfirm(false) }
        }
    }
}

@Composable
private fun AssignView(item: ClipItem, state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Column(Modifier.fillMaxSize().padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SpaceText(
            stringResource(R.string.clip_choose_section),
            SpaceTheme.typography.bodySmall,
            color = Color(palette.label),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(state.sections.size) { index ->
                val section = state.sections[index]
                ClipKey(section.name, palette, Modifier.fillMaxWidth(), item.section == section.name, locked = section.locked) {
                    actions.onAssign(section.name)
                }
            }
            if (item.section != null) {
                item { ClipKey(stringResource(R.string.clip_remove_section), palette, Modifier.fillMaxWidth()) { actions.onAssign(null) } }
            }
            item { ClipKey(stringResource(R.string.clip_new_section), palette, Modifier.fillMaxWidth(), icon = PanelIcon.ADD, onClick = actions::onNewSection) }
        }
        ClipKey(stringResource(R.string.clip_cancel), palette, Modifier.fillMaxWidth(), onClick = actions::onAssignCancel)
    }
}

@Composable
private fun ItemList(list: List<ClipItem>, palette: ThemePalette, actions: ClipActions) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(list.size, key = { list[it].id }) { index -> ItemRow(list[index], palette, actions) }
    }
}

@Composable
private fun ItemRow(item: ClipItem, palette: ThemePalette, actions: ClipActions) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).clickable { actions.onPick(item) }.padding(horizontal = 8.dp, vertical = 8.dp)) {
            SpaceText(ClipLabels.preview(item.text), SpaceTheme.typography.body, color = Color(palette.label))
        }
        val pinLabel = stringResource(if (item.pinned) R.string.clip_unpin else R.string.clip_pin)
        ClipKey(stringResource(R.string.clip_pin), palette, selected = item.pinned, description = pinLabel) { actions.onPin(item.id) }
        ClipKey(stringResource(R.string.clip_save), palette, selected = item.section != null, icon = PanelIcon.SAVE) { actions.onAssignStart(item.id) }
        ClipIconKey(PanelIcon.CLOSE, stringResource(R.string.clip_delete), palette) { actions.onDelete(item.id) }
    }
}
