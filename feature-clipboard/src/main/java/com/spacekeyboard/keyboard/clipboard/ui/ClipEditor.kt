package com.spacekeyboard.keyboard.clipboard.ui

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipEditing
import com.spacekeyboard.keyboard.clipboard.ClipPolicy
import com.spacekeyboard.keyboard.clipboard.EditStatus
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import java.text.NumberFormat

interface ClipEditorActions {
    fun onChange(text: String)
    fun onSave(text: String)
    fun onClose()
}

class ClipEditorModel(val original: String, val initial: String, val restored: Boolean)

private const val FIELD_ALPHA = 0.55f
private const val DISABLED_ALPHA = 0.45f

@Composable
fun ClipEditorHost(palette: ThemePalette, model: ClipEditorModel, status: EditStatus?, actions: ClipEditorActions) {
    val rtl = LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL
    CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) { ClipEditor(palette, model, status, actions) }
    }
}

@Composable
private fun ClipEditor(palette: ThemePalette, model: ClipEditorModel, status: EditStatus?, actions: ClipEditorActions) {
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(model.initial, TextRange(model.initial.length)))
    }
    var asking by rememberSaveable { mutableStateOf(false) }
    val changed = field.text != model.original
    val leave = { if (changed) asking = true else actions.onClose() }
    BackHandler { if (asking) asking = false else leave() }
    LaunchedEffect(field.text) { actions.onChange(field.text) }
    if (asking) {
        EditorConfirm(palette, onKeep = { asking = false }, onDiscard = actions::onClose)
    } else {
        EditorBody(palette, model, field, status, actions, onValue = { field = it }, onCancel = leave)
    }
}

@Composable
private fun EditorBody(
    palette: ThemePalette,
    model: ClipEditorModel,
    field: TextFieldValue,
    status: EditStatus?,
    actions: ClipEditorActions,
    onValue: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val problem = ClipEditing.problem(field.text)
    val save = { if (problem == null) actions.onSave(field.text) else haptics.perform(SpaceHaptic.Reject) }
    var focused by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EditorBar(palette, ClipEditing.canSave(field.text, model.original), onCancel, save)
        EditorField(palette, field, onValue, Modifier.weight(1f).fillMaxWidth()) { focused = it }
        EditorFooter(palette, field.text, ClipLabels.problem(problem ?: status), model.restored && field.text == model.initial, focused)
    }
}

@Composable
private fun EditorBar(palette: ThemePalette, canSave: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ClipKey(stringResource(R.string.clip_cancel), palette, onClick = onCancel)
        SpaceText(
            stringResource(R.string.clip_edit_title),
            SpaceTheme.typography.body,
            color = Color(palette.label),
            modifier = Modifier.weight(1f),
        )
        ClipKey(
            stringResource(R.string.clip_edit_save),
            palette,
            Modifier.alpha(if (canSave) 1f else DISABLED_ALPHA),
            selected = canSave,
            icon = PanelIcon.DONE,
            onClick = onSave,
        )
    }
}

@Composable
private fun EditorField(palette: ThemePalette, value: TextFieldValue, onValue: (TextFieldValue) -> Unit, modifier: Modifier, onFocus: (Boolean) -> Unit) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val label = Color(palette.label)
    val style = SpaceTheme.typography.body.copy(color = label, textDirection = TextDirection.Content)
    val open: () -> Unit = {
        focus.requestFocus()
        keyboard?.show()
    }
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(palette.keyFill).copy(alpha = FIELD_ALPHA))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = open)
            .padding(12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValue,
            textStyle = style,
            cursorBrush = SolidColor(Color(palette.glow)),
            modifier = Modifier.fillMaxSize().focusRequester(focus).onFocusChanged { onFocus(it.isFocused) },
            decorationBox = { inner ->
                if (value.text.isEmpty()) {
                    SpaceText(stringResource(R.string.clip_edit_hint), style, color = label.copy(alpha = 0.5f))
                }
                inner()
            },
        )
    }
}

@Composable
private fun EditorFooter(palette: ThemePalette, text: String, message: Int?, draft: Boolean, typing: Boolean) {
    val label = Color(palette.label)
    val format = NumberFormat.getInstance()
    val max = format.format(ClipPolicy.MAX_TEXT)
    val over = text.length > ClipPolicy.MAX_TEXT
    val note = when {
        message != null -> stringResource(message, max)
        draft -> stringResource(R.string.clip_edit_draft)
        !typing -> stringResource(R.string.clip_edit_tap_hint)
        else -> ""
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        SpaceText(
            note,
            SpaceTheme.typography.bodySmall,
            color = if (message != null) ClipDanger else label.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
        )
        SpaceText(
            stringResource(R.string.clip_edit_counter, format.format(text.length), max),
            SpaceTheme.typography.bodySmall,
            color = if (over) ClipDanger else label.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun EditorConfirm(palette: ThemePalette, onKeep: () -> Unit, onDiscard: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpaceText(
            stringResource(R.string.clip_edit_discard_ask),
            SpaceTheme.typography.body.copy(textAlign = TextAlign.Center),
            color = Color(palette.label),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClipKey(stringResource(R.string.clip_edit_keep), palette, selected = true, onClick = onKeep)
            ClipKey(stringResource(R.string.clip_edit_discard), palette, onClick = onDiscard)
        }
    }
}
