package com.spacekeyboard.keyboard.emoji.ui

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.a11y.spaceHoldRepeat
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.RECENTS_GROUP
import com.spacekeyboard.keyboard.emoji.model.EmojiGroups
import com.spacekeyboard.keyboard.emoji.model.SkinTone

private val BarHeight = 40.dp

@Composable
fun EmojiTopBar(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, onKeys: () -> Unit) {
    var picking by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().height(BarHeight).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PanelKey(palette, stringResource(R.string.emoji_keys), Modifier.width(48.dp).height(BarHeight - 4.dp), onClick = onKeys) {
            SpaceText(stringResource(R.string.emoji_keys), SpaceTheme.typography.bodySmall, color = Color(palette.keyText))
        }
        PanelKey(palette, stringResource(R.string.emoji_search), Modifier.size(BarHeight - 4.dp), onClick = actions::onSearch) {
            PanelIconView(PanelIcon.SEARCH, Color(palette.keyText))
        }
        Box(modifier = Modifier.weight(1f)) {
            if (picking) TonePicker(state.tone, palette) { actions.onTone(it); picking = false } else Tabs(state, palette, actions)
        }
        PanelKey(palette, stringResource(R.string.emoji_tone), Modifier.size(BarHeight - 4.dp), selected = picking, onClick = { picking = !picking }) {
            Box(Modifier.size(20.dp).background(Color(state.tone.swatch), CircleShape))
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(BarHeight - 4.dp)
                .panelKeyLook(palette, false)
                .spaceHoldRepeat(actions::onDelete),
            contentAlignment = Alignment.Center,
        ) {
            PanelIconView(PanelIcon.BACKSPACE, Color(palette.keyText))
        }
    }
}

@Composable
private fun Tabs(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    val ids = listOf(RECENTS_GROUP) + state.groups.map { it.id }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        items(ids.size) { index ->
            val id = ids[index]
            Tab(id, id == state.activeGroup, palette, state.typeface) { actions.onGroup(id) }
        }
    }
}

@Composable
private fun Tab(id: String, selected: Boolean, palette: ThemePalette, typeface: Typeface?, onClick: () -> Unit) {
    PanelKey(palette, stringResource(EmojiLabels.group(id)), Modifier.size(BarHeight - 4.dp), selected = selected, onClick = onClick) {
        if (id == RECENTS_GROUP) {
            PanelIconView(PanelIcon.CLOCK, Color(palette.keyText))
        } else {
            EmojiGlyph(EmojiGroups.representatives[id].orEmpty(), typeface, 22.dp)
        }
    }
}

@Composable
private fun TonePicker(current: SkinTone, palette: ThemePalette, onPick: (SkinTone) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        SkinTone.entries.forEach { tone ->
            val ring = if (tone == current) Color(palette.glow) else Color.Transparent
            PanelKey(palette, stringResource(EmojiLabels.tone(tone)), Modifier.size(BarHeight - 4.dp), onClick = { onPick(tone) }) {
                Box(Modifier.size(22.dp).border(2.dp, ring, CircleShape).background(Color(tone.swatch), CircleShape))
            }
        }
    }
}
