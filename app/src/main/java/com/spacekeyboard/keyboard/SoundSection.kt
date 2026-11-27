package com.spacekeyboard.keyboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.core.sound.ClickSoundChoice
import com.spacekeyboard.keyboard.core.sound.ClickSoundImport
import com.spacekeyboard.keyboard.core.sound.ClickSoundStore
import com.spacekeyboard.keyboard.core.sound.ClipMath
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.ClickSoundPlayer

private val PICK_TYPES = arrayOf("audio/*", "application/ogg", "video/mp4", "video/3gpp")

@Composable
internal fun ClickSoundSection(store: ClickSoundStore, choice: ClickSoundChoice, volume: Int) {
    val context = LocalContext.current
    val importer = remember(store) { ClickSoundImport(context.contentResolver, store, context.cacheDir) }
    val staging = rememberSaveable(store, saver = SoundImportState.saver(importer)) { SoundImportState(importer) }
    val player = remember(context) { ClickSoundPlayer(context) }
    DisposableEffect(player) { onDispose { player.release() } }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) staging.pick(uri)
    }
    staging.staged?.let { SoundTrimSheet(it, staging.error, staging::confirm, staging::cancel) }
    SpaceText(stringResource(R.string.sound_title), SpaceTheme.typography.titleSmall)
    LookCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CurrentSound(choice, staging.busy) { player.preview(levelOf(volume)) }
            SoundActions(choice, staging.busy, { picker.launch(PICK_TYPES) }) {
                store.clear()
                staging.clearError()
            }
            if (staging.staged == null) staging.error?.let { Notice(it) }
            BodyText(R.string.sound_hint)
        }
    }
    ClickPresetSection(store, choice) { player.preview(levelOf(volume)) }
}

@Composable
private fun SoundActions(choice: ClickSoundChoice, busy: Boolean, onPick: () -> Unit, onRemove: () -> Unit) {
    if (busy) {
        SpaceText(stringResource(R.string.sound_busy), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
        return
    }
    SpaceButton(stringResource(if (choice.custom) R.string.sound_replace else R.string.sound_pick), onPick)
    if (choice.custom) SpaceButton(stringResource(R.string.sound_remove), onRemove)
}

@Composable
private fun CurrentSound(choice: ClickSoundChoice, busy: Boolean, onTest: () -> Unit) {
    val colors = SpaceTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
            SpaceIconView(SpaceIcon.SOUND, colors.primary, size = 24.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SpaceText(
                if (choice.custom) choice.name.ifBlank { stringResource(R.string.sound_custom) } else stringResource(R.string.sound_system),
                SpaceTheme.typography.titleSmall,
                maxLines = 1,
            )
            SpaceText(
                if (choice.custom) stringResource(R.string.sound_clip_length, ClipMath.seconds(choice.durationMs)) else stringResource(R.string.sound_system_note),
                SpaceTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        TestButton(!busy, onTest)
    }
}

@Composable
private fun TestButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = SpaceTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SpaceTheme.look.field)
            .border(1.dp, colors.outline, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = stringResource(R.string.sound_test), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(SpaceIcon.PLAY, colors.onSurface, size = 20.dp)
    }
}

private fun levelOf(volume: Int): Float {
    val level = volume / Limits.VOLUME_MAX.toFloat()
    return level * level
}
