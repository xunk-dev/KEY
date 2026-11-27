package com.spacekeyboard.keyboard.clipboard.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.spacekeyboard.keyboard.clipboard.BusyKind
import com.spacekeyboard.keyboard.clipboard.ClipBusy
import com.spacekeyboard.keyboard.clipboard.ClipConfirm
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.a11y.spaceCombinedClickable
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun ClipBody(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val assigning = state.assigning
    val saving = state.busy?.takeIf { it.kind == BusyKind.SAVING && state.tab == ClipTab.CURRENT }
    when {
        state.confirm is ClipConfirm.PasteLarge -> PasteLargeView(state.confirm, palette, actions)
        state.confirm != null -> ConfirmView(state.confirm, palette, actions)
        assigning != null -> AssignView(assigning, state, palette, actions)
        state.locked -> LockedView(palette, actions)
        state.items.isEmpty() && saving == null -> Note(stringResource(emptyText(state)), palette)
        else -> ItemList(state.items, saving, state.grouped, palette, actions)
    }
}

private fun emptyText(state: ClipSnapshot): Int = when {
    state.tab == ClipTab.CURRENT -> if (state.enabled) R.string.clip_empty_current else R.string.clip_empty_off
    state.tab == ClipTab.PINNED -> R.string.clip_empty_pinned
    state.section == null -> R.string.clip_no_sections
    else -> R.string.clip_empty_section
}

@Composable
private fun Note(text: String, palette: ThemePalette) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClipIcon(SpaceIcon.CLIP_EMPTY, Color(palette.label).copy(alpha = 0.6f), 40.dp)
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label))
    }
}

@Composable
private fun LockedView(palette: ThemePalette, actions: ClipActions) {
    Column(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClipIcon(SpaceIcon.LOCK, Color(palette.label).copy(alpha = 0.6f), 28.dp)
        SpaceText(stringResource(R.string.clip_locked_hint), SpaceTheme.typography.bodySmall, color = Color(palette.label))
        ClipKey(stringResource(R.string.clip_unlock), palette, spaceIcon = SpaceIcon.UNLOCK, onClick = actions::onUnlock)
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
            item { ClipKey(stringResource(R.string.clip_new_section), palette, Modifier.fillMaxWidth(), spaceIcon = SpaceIcon.FOLDER_ADD, onClick = actions::onNewSection) }
        }
        ClipKey(stringResource(R.string.clip_cancel), palette, Modifier.fillMaxWidth(), onClick = actions::onAssignCancel)
    }
}

@Composable
private fun ItemList(list: List<ClipItem>, saving: ClipBusy?, grouped: Boolean, palette: ThemePalette, actions: ClipActions) {
    var menu by remember { mutableStateOf<MenuAnchor?>(null) }
    val rows = remember(list, grouped) { clipRows(list, grouped) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, top = 6.dp, end = 8.dp, bottom = FAB_CLEARANCE),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (saving != null) item(key = SAVING_KEY) { SavingCard(saving, palette, actions) }
            items(rows.size, key = { rows[it].key }) { index ->
                when (val row = rows[index]) {
                    is ClipRow.Header -> KindHeader(row.kind, row.count, palette)
                    is ClipRow.Entry -> ItemCard(row.item, palette, actions) { top -> menu = MenuAnchor(row.item, top) }
                }
            }
        }
        menu?.let { anchor -> ItemMenu(anchor, palette, actions) { menu = null } }
    }
}

private fun relativeAge(at: Long): String =
    DateUtils.getRelativeTimeSpanString(at, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

@Composable
private fun ItemCard(item: ClipItem, palette: ThemePalette, actions: ClipActions, onMore: (Float) -> Unit) {
    var top by remember { mutableStateOf(0f) }
    val label = Color(palette.label)
    val age = remember(item.copiedAt) { relativeAge(item.copiedAt) }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val title = item.title
    SwipeCard(
        pinned = item.pinned,
        onTitle = { actions.onTitle(item.id) },
        onPin = { actions.onPin(item.id) },
        modifier = Modifier.fillMaxWidth().onGloballyPositioned { top = it.positionInParent().y },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(palette.keyFill).copy(alpha = CARD_ALPHA))
                .drawBehind { if (title != null) drawTitleBar(rtl) }
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val viewLabel = stringResource(R.string.clip_view)
            Column(
                Modifier
                    .weight(1f)
                    .spaceCombinedClickable(
                        onClick = { actions.onPick(item) },
                        onLongClick = { actions.onView(item.id) },
                        onLongClickLabel = viewLabel,
                    )
                    .padding(vertical = 4.dp),
            ) {
                ItemTexts(item, title, label, age)
            }
            val pinLabel = stringResource(if (item.pinned) R.string.clip_unpin else R.string.clip_pin)
            ClipSpaceKey(SpaceIcon.PIN, pinLabel, palette, selected = item.pinned, size = CARD_KEY, iconSize = CARD_ICON) { actions.onPin(item.id) }
            ClipSpaceKey(SpaceIcon.MORE, stringResource(R.string.clip_more), palette, size = CARD_KEY, iconSize = CARD_ICON) { onMore(top) }
        }
    }
}

@Composable
private fun ColumnScope.ItemTexts(item: ClipItem, title: String?, label: Color, age: String) {
    if (title != null) {
        SpaceText(
            title,
            SpaceTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = ClipTitleInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SpaceText(
            ClipLabels.preview(item.text) + "  ·  " + age + sizeSuffix(item),
            SpaceTheme.typography.bodySmall,
            color = label.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        SpaceText(
            ClipLabels.preview(item.text),
            SpaceTheme.typography.body,
            color = label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        SpaceText(age + sizeSuffix(item), SpaceTheme.typography.bodySmall, color = label.copy(alpha = 0.6f))
    }
}

private fun DrawScope.drawTitleBar(rtl: Boolean) {
    val width = TITLE_BAR.toPx()
    val inset = TITLE_BAR_INSET.toPx()
    val x = if (rtl) size.width - width else 0f
    drawRect(ClipTitleInk, Offset(x, inset), Size(width, size.height - inset * 2))
}

@Composable
private fun sizeSuffix(item: ClipItem): String =
    if (item.blob != null) "  ·  " + stringResource(R.string.clip_item_size, ClipLabels.compact(item.length)) else ""

private const val CARD_ALPHA = 0.55f
private val CARD_KEY = 40.dp
private val CARD_ICON = 20.dp
private val FAB_CLEARANCE = 68.dp
private val TITLE_BAR = 3.dp
private val TITLE_BAR_INSET = 12.dp
private const val SAVING_KEY = "saving"
