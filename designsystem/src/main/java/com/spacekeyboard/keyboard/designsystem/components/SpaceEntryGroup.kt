package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Immutable
class SpaceEntry(val label: String, val icon: SpaceIcon, val tone: BadgeTone, val onClick: () -> Unit)

private class RowMetrics(val padding: Dp, val gap: Dp, val badge: Dp, val icon: Dp)

private fun rowMetrics(look: SpaceLook) = when (look) {
    SpaceLook.Galaxy -> RowMetrics(14.dp, 14.dp, 38.dp, 22.dp)
    SpaceLook.Day -> RowMetrics(16.dp, 14.dp, 44.dp, 24.dp)
    SpaceLook.Eclipse -> RowMetrics(18.dp, 16.dp, 24.dp, 24.dp)
}

private val TILE_HEIGHT = 108.dp
private val WIDE_HEIGHT = 68.dp
private val TILE_GAP = 12.dp

@Composable
fun SpaceEntryGroup(
    title: String,
    entries: List<SpaceEntry>,
    modifier: Modifier = Modifier,
    asList: Boolean = false,
) {
    val look = SpaceTheme.look
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (look.look == SpaceLook.Galaxy) 10.dp else 8.dp)) {
        SpaceSectionTitle(title)
        if (look.tiles && !asList) TileGrid(entries) else SpaceEntryList(entries)
    }
}

@Composable
fun SpaceEntryList(entries: List<SpaceEntry>, modifier: Modifier = Modifier) {
    LookCard(modifier.fillMaxWidth()) {
        entries.forEachIndexed { index, entry -> NavRow(entry, divider = index < entries.lastIndex) }
    }
}

@Composable
private fun NavRow(entry: SpaceEntry, divider: Boolean) {
    val look = SpaceTheme.look
    val metrics = rowMetrics(look.look)
    val line = SpaceTheme.colors.divider
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = look.rowHeight)
            .clickable(role = Role.Button, onClick = entry.onClick)
            .then(if (divider) Modifier.drawBehind { drawDivider(line, size.width, size.height, 1.dp.toPx()) } else Modifier)
            .padding(horizontal = metrics.padding, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(metrics.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(entry.icon, entry.tone, size = metrics.badge, iconSize = metrics.icon)
        SpaceText(entry.label, rowLabel(), Modifier.weight(1f))
        SpaceIconView(SpaceIcon.CHEVRON, look.chevron, size = 20.dp)
    }
}

private fun DrawScope.drawDivider(color: Color, width: Float, height: Float, stroke: Float) {
    drawLine(color, Offset(0f, height - stroke / 2f), Offset(width, height - stroke / 2f), stroke)
}

@Composable
private fun rowLabel() = SpaceTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)

@Composable
private fun TileGrid(entries: List<SpaceEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(TILE_GAP)) {
        entries.chunked(2).forEach { pair ->
            if (pair.size == 2) {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(TILE_GAP)) {
                    pair.forEach { Tile(it, Modifier.weight(1f).fillMaxHeight()) }
                }
            } else {
                WideTile(pair.first())
            }
        }
    }
}

@Composable
private fun Tile(entry: SpaceEntry, modifier: Modifier) {
    LookCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TILE_HEIGHT)
                .clickable(role = Role.Button, onClick = entry.onClick)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconBadge(entry.icon, entry.tone)
            SpaceText(entry.label, rowLabel())
        }
    }
}

@Composable
private fun WideTile(entry: SpaceEntry) {
    LookCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = WIDE_HEIGHT)
                .clickable(role = Role.Button, onClick = entry.onClick)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(entry.icon, entry.tone)
            SpaceText(entry.label, rowLabel(), Modifier.weight(1f))
            SpaceIconView(SpaceIcon.CHEVRON, SpaceTheme.look.chevron, size = 20.dp)
        }
    }
}
