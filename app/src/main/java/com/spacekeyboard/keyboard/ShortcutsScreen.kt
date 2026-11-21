package com.spacekeyboard.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun ShortcutsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ShortcutStore(context) }
    var entries by remember { mutableStateOf(store.read()) }
    var trigger by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    ScreenFrame(R.string.shortcuts_title, onBack) {
        BodyText(R.string.shortcuts_note)
        ShortcutField(trigger, stringResource(R.string.shortcuts_trigger), ShortcutStore.MAX_TRIGGER) { trigger = it }
        ShortcutField(text, stringResource(R.string.shortcuts_text), ShortcutStore.MAX_TEXT) { text = it }
        SpacePrimaryButton(stringResource(R.string.shortcuts_add), {
            if (store.add(trigger, text)) {
                trigger = ""
                text = ""
                entries = store.read()
            }
        }, icon = SpaceIcon.ADD)
        ShortcutList(entries) {
            store.remove(it)
            entries = store.read()
        }
    }
}

@Composable
private fun ShortcutList(entries: Map<String, String>, onDelete: (String) -> Unit) {
    if (entries.isEmpty()) return
    LookCard(Modifier.fillMaxWidth()) {
        entries.forEach { (key, value) -> ShortcutRow(key, value) { onDelete(key) } }
    }
}

@Composable
private fun ShortcutField(value: String, hint: String, limit: Int, onChange: (String) -> Unit) {
    ToolField {
        Column(modifier = Modifier.padding(12.dp)) {
            SpaceText(hint, SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= limit) onChange(it) },
                textStyle = SpaceTheme.typography.body.copy(color = SpaceTheme.colors.onSurface),
                cursorBrush = SolidColor(SpaceTheme.colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ShortcutRow(trigger: String, text: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SpaceText(trigger, SpaceTheme.typography.title)
            SpaceText(text, SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
        }
        SpaceText(
            text = stringResource(R.string.shortcuts_delete),
            style = SpaceTheme.typography.titleSmall,
            color = SpaceTheme.colors.primary,
            modifier = Modifier.clickable(role = Role.Button, onClick = onDelete).minTouchTarget(),
        )
    }
}
