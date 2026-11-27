package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.model.SkinTone

val DockHeight = 38.dp
val DockKey = 36.dp
private const val RING_ALPHA = 0.35f

@Composable
fun DockBar(palette: ThemePalette, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(DockHeight)
            .clip(CircleShape)
            .background(Color(palette.keyFill))
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun EmojiDock(
    state: EmojiSnapshot,
    ids: List<String>,
    shown: () -> Int,
    palette: ThemePalette,
    actions: EmojiActions,
    onSelect: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    var picking by remember { mutableStateOf(false) }
    val activeIndex = shown()
    LaunchedEffect(activeIndex) { listState.reveal(activeIndex) }
    DockBar(palette) {
        DockButton(PanelIcon.KEYBOARD, stringResource(R.string.emoji_keys), palette, false, Modifier.size(DockKey), actions::onKeys)
        if (picking) {
            TonePicker(state.tone, palette, Modifier.weight(1f)) { actions.onTone(it); picking = false }
        } else {
            CategoryStrip(ids, activeIndex, listState, palette, Modifier.weight(1f), onSelect)
        }
        ToneButton(state.tone, palette, picking) { picking = !picking }
        DockDelete(palette, stringResource(R.string.emoji_delete), actions::onDelete)
    }
}

@Composable
private fun CategoryStrip(
    ids: List<String>,
    activeIndex: Int,
    listState: LazyListState,
    palette: ThemePalette,
    modifier: Modifier,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(ids.size) { index ->
            val id = ids[index]
            DockCategory(
                panelIconForGroup(id).space ?: SpaceIcon.RECENTS,
                stringResource(EmojiLabels.group(id)),
                palette,
                index == activeIndex,
            ) { onSelect(index) }
        }
    }
}

private suspend fun LazyListState.reveal(index: Int) {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index }
    val inside = item != null && item.offset >= info.viewportStartOffset && item.offset + item.size <= info.viewportEndOffset
    if (inside) return
    val size = item?.size ?: info.visibleItemsInfo.firstOrNull()?.size ?: 0
    animateScrollToItem(index, -(info.viewportSize.width - size) / 2)
}

@Composable
private fun ToneButton(tone: SkinTone, palette: ThemePalette, active: Boolean, onClick: () -> Unit) {
    val label = stringResource(R.string.emoji_tone)
    Box(
        modifier = Modifier
            .size(DockKey)
            .clip(CircleShape)
            .background(if (active) Color(palette.keyPressed) else Color.Transparent)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(22.dp).border(1.5.dp, Color(palette.keyText).copy(alpha = RING_ALPHA), CircleShape).padding(3.dp).background(Color(tone.swatch), CircleShape))
    }
}

@Composable
private fun TonePicker(current: SkinTone, palette: ThemePalette, modifier: Modifier, onPick: (SkinTone) -> Unit) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        SkinTone.entries.forEach { tone ->
            val ring = if (tone == current) Color(palette.keyTextPressed) else Color.Transparent
            val label = stringResource(EmojiLabels.tone(tone))
            Box(
                modifier = Modifier
                    .size(DockKey)
                    .clip(CircleShape)
                    .semantics { contentDescription = label }
                    .clickable(role = Role.Button) { onPick(tone) },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(26.dp).border(2.dp, ring, CircleShape).padding(3.dp).background(Color(tone.swatch), CircleShape))
            }
        }
    }
}

@Composable
fun DockButton(icon: PanelIcon, description: String, palette: ThemePalette, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val ink = Color(if (selected) palette.keyTextPressed else palette.keyText)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) Color(palette.keyPressed) else Color.Transparent)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PanelIconView(icon, ink)
    }
}
