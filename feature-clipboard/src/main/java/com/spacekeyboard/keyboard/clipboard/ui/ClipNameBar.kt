package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import com.spacekeyboard.keyboard.emoji.ui.PanelIconView

private const val CARET = "|"
private const val HINT_MASK = 0x99FFFFFF.toInt()
private const val FIELD_ALPHA = 0.45f
private val BarKey = 36.dp

@Composable
internal fun ClipNameBar(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NameField(state.naming, state.titling != null, palette, Modifier.weight(1f).height(BarKey))
        RoundKey(PanelIcon.DONE, stringResource(R.string.clip_ok), palette, true, actions::onNameOk)
        RoundKey(PanelIcon.CLOSE, stringResource(R.string.clip_cancel), palette, false, actions::onNameCancel)
    }
}

@Composable
private fun NameField(name: String, titling: Boolean, palette: ThemePalette, modifier: Modifier) {
    val blank = name.isEmpty()
    Row(
        modifier = modifier.clip(CircleShape).background(Color(palette.background).copy(alpha = FIELD_ALPHA)).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClipIcon(if (titling) SpaceIcon.PENCIL else SpaceIcon.FOLDER_ADD, Color(palette.label), 18.dp)
        val hint = stringResource(if (titling) R.string.clip_title_hint else R.string.clip_section_name)
        val text = if (blank) hint else name + CARET
        val ink = if (blank) palette.label and HINT_MASK else palette.keyText
        SpaceText(text, SpaceTheme.typography.body, color = Color(ink), maxLines = 1)
    }
}

@Composable
private fun RoundKey(icon: PanelIcon, description: String, palette: ThemePalette, selected: Boolean, onClick: () -> Unit) {
    val haptics = rememberSpaceHaptics()
    val fill = if (selected) palette.keyPressed else palette.keyFill
    val ink = if (selected) palette.keyTextPressed else palette.keyText
    Box(
        modifier = Modifier
            .size(BarKey)
            .clip(CircleShape)
            .background(Color(fill))
            .semantics { contentDescription = description }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) { PanelIconView(icon, Color(ink), Modifier.size(16.dp)) }
}
