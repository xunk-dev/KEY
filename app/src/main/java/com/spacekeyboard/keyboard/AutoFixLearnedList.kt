package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.autofix.Kind
import com.spacekeyboard.keyboard.autofix.Learned
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val MAX_ROWS = 30
private const val CENTER_AREA = 4

private val areas = intArrayOf(
    R.string.autofix_area_top_left,
    R.string.autofix_area_top,
    R.string.autofix_area_top_right,
    R.string.autofix_area_left,
    R.string.autofix_area_center,
    R.string.autofix_area_right,
    R.string.autofix_area_bottom_left,
    R.string.autofix_area_bottom,
    R.string.autofix_area_bottom_right,
)

@Composable
fun LearnedSection(items: List<Learned>, onForget: (Learned) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceSectionTitle(stringResource(R.string.autofix_learned_title))
        if (items.isEmpty()) {
            AutoFixNote(R.string.autofix_learned_empty)
        } else {
            AutoFixNote(R.string.autofix_learned_note)
            if (items.any { it.kind == Kind.TOUCH }) AutoFixNote(R.string.autofix_area_note)
            items.take(MAX_ROWS).forEach { item -> LearnedRow(item) { onForget(item) } }
        }
    }
}

@Composable
private fun LearnedRow(item: Learned, onForget: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SpaceText(itemLabel(item), SpaceTheme.typography.body)
            val status = if (item.active) R.string.autofix_item_active else R.string.autofix_item_learning
            SpaceText(stringResource(status), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
        }
        SpaceText(
            text = stringResource(R.string.autofix_forget),
            style = SpaceTheme.typography.titleSmall,
            color = SpaceTheme.colors.primary,
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onForget)
                .minTouchTarget(),
        )
    }
}

@Composable
private fun itemLabel(item: Learned): String = when (item.kind) {
    Kind.WORD -> stringResource(R.string.autofix_item_word, item.from, item.to)
    Kind.LETTER -> stringResource(R.string.autofix_item_letter, item.from, item.to)
    Kind.TOUCH -> stringResource(R.string.autofix_item_touch, item.from, item.to, stringResource(areas.getOrElse(item.zone) { areas[CENTER_AREA] }))
}
