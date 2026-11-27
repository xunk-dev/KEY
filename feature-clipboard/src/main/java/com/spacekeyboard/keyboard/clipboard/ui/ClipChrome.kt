package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import kotlinx.coroutines.delay

internal const val UNDO_MS = 5_000L
private const val RING_ALPHA = 0.5f
private val FabSize = 46.dp
private val ToolSize = 38.dp

@Composable
internal fun ClipClearSlot(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    val line = Color(palette.border).copy(alpha = LINE_ALPHA)
    val canClear = state.tab == ClipTab.CURRENT && state.items.isNotEmpty() && state.confirm == null
    Box(Modifier.size(width = 46.dp, height = 40.dp).underline(line), contentAlignment = Alignment.Center) {
        if (canClear) {
            ClipSpaceKey(SpaceIcon.TRASH, stringResource(R.string.clip_clear), palette, danger = true, size = ToolSize, iconSize = 20.dp, onClick = actions::onClear)
        }
    }
}

@Composable
internal fun ClipFloatingBar(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions, modifier: Modifier = Modifier) {
    if (state.confirm != null || state.assigning != null) return
    Row(
        modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeysFab(palette, actions)
        state.undo?.let { UndoPill(it, palette, actions, Modifier.weight(1f)) }
    }
}

@Composable
private fun KeysFab(palette: ThemePalette, actions: ClipActions) {
    val haptics = rememberSpaceHaptics()
    val label = stringResource(R.string.clip_keys)
    Box(
        Modifier
            .size(FabSize)
            .shadow(6.dp, CircleShape)
            .background(Color(palette.keyFill), CircleShape)
            .border(1.dp, Color(palette.glow).copy(alpha = RING_ALPHA), CircleShape)
            .clip(CircleShape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                actions.onKeys()
            },
        contentAlignment = Alignment.Center,
    ) {
        ClipIcon(SpaceIcon.KEYS, Color(palette.keyText), 24.dp)
    }
}

@Composable
private fun UndoPill(item: ClipItem, palette: ThemePalette, actions: ClipActions, modifier: Modifier) {
    val haptics = rememberSpaceHaptics()
    val shape = RoundedCornerShape(24.dp)
    LaunchedEffect(item.id) {
        delay(UNDO_MS)
        actions.onUndoDismiss()
    }
    Row(
        modifier
            .height(FabSize)
            .shadow(6.dp, shape)
            .background(Color(palette.keyFill), shape)
            .padding(start = 16.dp, end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceText(stringResource(R.string.clip_deleted), SpaceTheme.typography.bodySmall, color = Color(palette.keyText), maxLines = 1, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(role = Role.Button) {
                    haptics.perform(SpaceHaptic.Confirm)
                    actions.onUndo()
                }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            SpaceText(stringResource(R.string.clip_undo), SpaceTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color(palette.glow))
        }
    }
}
