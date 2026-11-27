package com.spacekeyboard.keyboard

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.sound.ClickSoundChoice
import com.spacekeyboard.keyboard.core.sound.ClickSoundStore
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun ClickPresetSection(store: ClickSoundStore, choice: ClickSoundChoice, onPicked: () -> Unit) {
    PresetGroup(R.string.sound_presets_calm, CALM_PRESETS, store, choice, onPicked)
    PresetGroup(R.string.sound_presets_space, SPACE_PRESETS, store, choice, onPicked)
    BodyText(R.string.sound_presets_hint)
}

@Composable
private fun PresetGroup(
    @StringRes title: Int,
    presets: List<ClickPreset>,
    store: ClickSoundStore,
    choice: ClickSoundChoice,
    onPicked: () -> Unit,
) {
    val context = LocalContext.current
    SpaceText(stringResource(title), SpaceTheme.typography.titleSmall)
    LookCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                val label = stringResource(preset.label)
                PresetRow(label, stringResource(preset.note), choice.custom && choice.name == label) {
                    if (installPreset(context, store, preset)) onPicked()
                }
            }
        }
    }
}

@Composable
private fun PresetRow(name: String, note: String, selected: Boolean, onClick: () -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PresetDot(selected)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SpaceText(name, SpaceTheme.typography.titleSmall, maxLines = 1)
            SpaceText(note, SpaceTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun PresetDot(selected: Boolean) {
    val colors = SpaceTheme.colors
    val ring = if (selected) colors.primary else colors.outline
    Box(Modifier.size(20.dp).clip(CircleShape).border(2.dp, ring, CircleShape), contentAlignment = Alignment.Center) {
        if (selected) Box(Modifier.size(10.dp).clip(CircleShape).background(colors.primary))
    }
}
