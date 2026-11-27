package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spacekeyboard.keyboard.core.sound.ClipMath
import com.spacekeyboard.keyboard.core.sound.ClipRange
import com.spacekeyboard.keyboard.core.sound.StagedSound
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun SoundTrimSheet(sound: StagedSound, error: Int?, onConfirm: (ClipRange) -> Unit, onCancel: () -> Unit) {
    val state = rememberSaveable(sound, saver = TrimState.saver(sound)) { TrimState(sound) }
    val preview = remember { ClipPreview() }
    var playing by remember { mutableStateOf(false) }
    DisposableEffect(preview) { onDispose { preview.release() } }
    val toggle = {
        if (playing) {
            preview.stop()
            playing = false
        } else {
            playing = true
            preview.play(sound.file.path, state.range) { playing = false }
        }
    }
    val confirm = {
        preview.stop()
        onConfirm(state.range)
    }
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onCancel() } },
            contentAlignment = Alignment.BottomCenter,
        ) {
            SheetBody(state, error, playing, toggle, confirm, onCancel)
        }
    }
}

@Composable
private fun SheetBody(
    state: TrimState,
    error: Int?,
    playing: Boolean,
    onToggle: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(SpaceTheme.colors.surface)
            .pointerInput(Unit) { detectTapGestures { } }
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpaceText(stringResource(R.string.sound_trim_title), SpaceTheme.typography.titleSmall)
        Waveform(state)
        SelectionLabel(state)
        ZoomRow(state)
        error?.let { Notice(it) }
        SpaceText(
            stringResource(R.string.sound_trim_hint, (ClipMath.MAX_MS / 1000L).toInt()),
            SpaceTheme.typography.bodySmall,
            color = SpaceTheme.colors.onSurfaceVariant,
        )
        SpaceButton(stringResource(if (playing) R.string.sound_trim_stop else R.string.sound_trim_play), onToggle)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpaceButton(stringResource(R.string.sound_trim_cancel), onCancel, Modifier.weight(1f))
            SpaceButton(stringResource(R.string.sound_trim_use), onConfirm, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SelectionLabel(state: TrimState) {
    val range = state.range
    SpaceText(
        stringResource(
            R.string.sound_trim_selected,
            ClipMath.seconds(range.startMs),
            ClipMath.seconds(range.endMs),
            ClipMath.seconds(range.lengthMs),
        ),
        SpaceTheme.typography.bodySmall,
        color = SpaceTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun ZoomRow(state: TrimState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Zoom.entries.forEach { ZoomChip(it, it == state.zoom) { state.select(it) } }
    }
}

@Composable
private fun RowScope.ZoomChip(zoom: Zoom, selected: Boolean, onSelect: () -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 40.dp)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(stringResource(zoom.labelRes), SpaceTheme.typography.bodySmall, maxLines = 1)
    }
}
