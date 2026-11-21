package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemeColors
import com.spacekeyboard.keyboard.core.theme.ThemeDef
import com.spacekeyboard.keyboard.core.theme.ThemeCatalog
import com.spacekeyboard.keyboard.core.theme.ThemeResolver
import com.spacekeyboard.keyboard.core.theme.ThemeSlot
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun ThemeGrid(selectedId: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ThemeCatalog.all.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { ThemeCard(it, it.id == selectedId, onSelect) }
            }
        }
    }
}

@Composable
private fun RowScope.ThemeCard(theme: ThemeDef, selected: Boolean, onSelect: (String) -> Unit) {
    val palette = ThemeResolver.resolve(theme.colors)
    val shape = RoundedCornerShape(SpaceTheme.look.cardRadius.coerceAtMost(18.dp))
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 68.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(palette.background), Color(palette.keyFill))))
            .border(if (selected) 2.dp else 1.dp, Color(if (selected) palette.glow else palette.border), shape)
            .selectable(selected = selected, role = Role.RadioButton) { onSelect(theme.id) }
            .padding(12.dp),
    ) {
        SpaceText(
            text = stringResource(theme.nameRes),
            style = SpaceTheme.typography.titleSmall,
            color = Color(palette.label),
            modifier = Modifier.align(Alignment.CenterStart),
        )
        ThemeMark(Modifier.align(Alignment.CenterEnd), selected, palette.label, palette.glow)
    }
}

@Composable
private fun ThemeMark(modifier: Modifier, selected: Boolean, label: Int, glow: Int) {
    Box(modifier) {
        if (selected) {
            SpaceIconView(SpaceIcon.CHECK, Color(label), size = 20.dp)
        } else {
            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(glow)))
        }
    }
}

@Composable
fun SlotTabs(selected: ThemeSlot, colors: ThemeColors, onSelect: (ThemeSlot) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ThemeSlot.entries.forEach { slot ->
            SlotTab(slot, colors[slot], slot == selected, onSelect)
        }
    }
}

@Composable
private fun RowScope.SlotTab(slot: ThemeSlot, color: Int, selected: Boolean, onSelect: (ThemeSlot) -> Unit) {
    val shape = RoundedCornerShape(SpaceTheme.look.fieldRadius.coerceAtMost(16.dp))
    val outline = if (selected) SpaceTheme.colors.primary else SpaceTheme.colors.outline
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, outline, shape)
            .selectable(selected = selected, role = Role.Tab) { onSelect(slot) }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(22.dp).clip(CircleShape).background(Color(color)))
        SpaceText(stringResource(slotLabel(slot)), SpaceTheme.typography.bodySmall)
    }
}

private fun slotLabel(slot: ThemeSlot): Int = when (slot) {
    ThemeSlot.BACKGROUND -> R.string.slot_background
    ThemeSlot.KEYS -> R.string.slot_keys
    ThemeSlot.GLOW -> R.string.slot_glow
    ThemeSlot.BORDER -> R.string.slot_border
}
