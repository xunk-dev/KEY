package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeyFn
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

private const val PER_ROW = 3

@Composable
internal fun KeyFnSection(layouts: LayoutController, language: String) {
    val snapshot = rememberLayoutSnapshot(layouts)
    val rtl = LayoutCatalog.isRtl(language)
    val slots = snapshot.layouts[language]?.rows?.lastOrNull().orEmpty().filter(KeyFn::isSlot)
    var picked by remember(language) { mutableStateOf<String?>(null) }
    val slot = slots.firstOrNull { it.id == picked } ?: slots.firstOrNull() ?: return
    val current = fnOf(slot, rtl)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SpaceText(stringResource(R.string.keyfn_title), SpaceTheme.typography.titleSmall)
        SpaceText(stringResource(R.string.keyfn_hint), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.forEach { FnChip(slotName(it, rtl), it.id == slot.id) { picked = it.id } }
        }
        KeyFn.entries.chunked(PER_ROW).forEach { group ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                group.forEach { FnChip(stringResource(it.labelRes), it == current) { layouts.setKeyFn(language, slot.id, it) } }
                repeat(PER_ROW - group.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        SpaceButton(stringResource(R.string.keyfn_default), onClick = { layouts.setKeyFn(language, slot.id, null) })
    }
}

private fun fnOf(key: KeySpec, rtl: Boolean): KeyFn? =
    KeyFn.entries.firstOrNull { it.action == key.action && (it.action != KeyAction.COMMIT || it.label(rtl) == key.label) }

@Composable
private fun slotName(key: KeySpec, rtl: Boolean): String =
    fnOf(key, rtl)?.let { stringResource(it.labelRes) } ?: key.label

@Composable
private fun RowScope.FnChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SpaceText(text, SpaceTheme.typography.label.copy(textAlign = TextAlign.Center), maxLines = 2)
    }
}
