package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun <T> ChipGrid(items: List<T>, selected: T, labelRes: (T) -> Int, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { item -> Chip(stringResource(labelRes(item)), item == selected) { onSelect(item) } }
            }
        }
    }
}

@Composable
private fun RowScope.Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(SpaceTheme.look.fieldRadius.coerceAtMost(16.dp))
    val colors = SpaceTheme.colors
    val outline = if (selected) colors.primary else colors.outline
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(text, SpaceTheme.typography.bodySmall)
    }
}
