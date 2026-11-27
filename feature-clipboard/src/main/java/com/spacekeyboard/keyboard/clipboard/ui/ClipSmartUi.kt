package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipKind
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val HEADER_ALPHA = 0.8f
private const val HEADER_ICON_ALPHA = 0.7f

internal sealed interface ClipRow {
    val key: Any

    data class Header(val kind: ClipKind, val count: Int) : ClipRow {
        override val key: Any get() = "kind:" + kind.name
    }

    data class Entry(val item: ClipItem) : ClipRow {
        override val key: Any get() = item.id
    }
}

internal fun clipRows(list: List<ClipItem>, grouped: Boolean): List<ClipRow> {
    if (!grouped) return list.map { ClipRow.Entry(it) }
    val rows = ArrayList<ClipRow>(list.size + ClipKind.entries.size)
    list.groupBy { it.primaryKind }.forEach { (kind, group) ->
        rows += ClipRow.Header(kind, group.size)
        group.forEach { rows += ClipRow.Entry(it) }
    }
    return rows
}

internal fun kindIcon(kind: ClipKind?): SpaceIcon = when (kind) {
    null -> SpaceIcon.KIND_ALL
    ClipKind.LINK -> SpaceIcon.KIND_LINK
    ClipKind.EMAIL -> SpaceIcon.KIND_EMAIL
    ClipKind.PHONE -> SpaceIcon.KIND_PHONE
    ClipKind.USERNAME -> SpaceIcon.KIND_USERNAME
    ClipKind.TOKEN -> SpaceIcon.KIND_TOKEN
    ClipKind.NUMBER -> SpaceIcon.KIND_NUMBER
    ClipKind.TEXT -> SpaceIcon.KIND_TEXT
}

@Composable
internal fun ClipSmartSlot(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val line = Color(palette.border).copy(alpha = LINE_ALPHA)
    val label = stringResource(if (state.smart) R.string.clip_smart_on else R.string.clip_smart_off)
    Box(Modifier.size(width = 46.dp, height = 40.dp).underline(line), contentAlignment = Alignment.Center) {
        ClipSpaceKey(SpaceIcon.SMART, label, palette, selected = state.smart, size = 38.dp, iconSize = 20.dp, onClick = actions::onSmart)
    }
}

@Composable
internal fun ClipKindBar(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val kinds = remember(state.kindCounts) { state.kindCounts.entries.toList() }
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item { ClipKindChip(null, 0, state.kind == null, palette) { actions.onKind(null) } }
        items(kinds.size) { index ->
            val entry = kinds[index]
            ClipKindChip(entry.key, entry.value, state.kind == entry.key, palette) { actions.onKind(entry.key) }
        }
    }
}

@Composable
private fun ClipKindChip(kind: ClipKind?, count: Int, isSelected: Boolean, palette: ThemePalette, onClick: () -> Unit) {
    val haptics = rememberSpaceHaptics()
    val label = stringResource(if (kind == null) R.string.clip_kind_all else ClipLabels.kind(kind))
    val fill = Color(if (isSelected) palette.keyPressed else palette.keyFill)
    val tint = Color(if (isSelected) palette.keyTextPressed else palette.keyText)
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(fill)
            .semantics {
                contentDescription = label
                selected = isSelected
            }
            .clickable(role = Role.Tab) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            }
            .padding(start = 10.dp, end = if (count > 0) 6.dp else 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClipIcon(kindIcon(kind), tint, 16.dp)
        SpaceText(label, SpaceTheme.typography.bodySmall, color = tint)
        if (count > 0) CountBadge(count, palette, isSelected)
    }
}

@Composable
internal fun KindHeader(kind: ClipKind, count: Int, palette: ThemePalette) {
    val label = Color(palette.label)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClipIcon(kindIcon(kind), label.copy(alpha = HEADER_ICON_ALPHA), 16.dp)
        SpaceText(
            stringResource(ClipLabels.kind(kind)),
            SpaceTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = label.copy(alpha = HEADER_ALPHA),
        )
        CountBadge(count, palette, false)
    }
}
