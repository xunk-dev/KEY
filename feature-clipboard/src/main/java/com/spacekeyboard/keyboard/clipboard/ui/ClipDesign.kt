package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import java.text.NumberFormat

internal const val DIM_ALPHA = 0.65f
internal const val LINE_ALPHA = 0.35f
internal const val INDICATOR_INSET = 0.15f
internal const val INDICATOR_MS = 220
private const val BADGE_MAX = 99
private const val BADGE_FILL = 0.16f
private const val BADGE_FILL_SELECTED = 0.24f
private val BADGE_TEXT = 11.sp
private const val STRIP_FILL = 0.12f
private const val STRIP_EDGE = 0.35f
private val TOOL_SIZE = 38.dp
private val TOOL_ICON = 20.dp

@Composable
fun ClipIcon(icon: SpaceIcon, tint: Color, size: Dp = 22.dp, modifier: Modifier = Modifier) {
    SpaceIconView(icon, tint, modifier, size)
}

@Composable
internal fun CountBadge(count: Int, palette: ThemePalette, selected: Boolean) {
    val text = if (count > BADGE_MAX) "$BADGE_MAX+" else NumberFormat.getInstance().format(count)
    val fill = Color(palette.glow).copy(alpha = if (selected) BADGE_FILL_SELECTED else BADGE_FILL)
    val style = SpaceTheme.typography.bodySmall.copy(fontSize = BADGE_TEXT, lineHeight = BADGE_TEXT, fontWeight = FontWeight.Medium)
    Box(
        Modifier
            .defaultMinSize(minWidth = 19.dp, minHeight = 19.dp)
            .background(fill, CircleShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(text, style, color = Color(palette.label))
    }
}

@Composable
fun ClipEnableStrip(palette: ThemePalette, actions: ClipActions) {
    val haptics = rememberSpaceHaptics()
    val accent = Color(palette.glow)
    val shape = RoundedCornerShape(14.dp)
    val enableLabel = stringResource(R.string.clip_enable_description)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .background(accent.copy(alpha = STRIP_FILL), shape)
            .border(1.dp, accent.copy(alpha = STRIP_EDGE), shape)
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceText(stringResource(R.string.clip_off_short), SpaceTheme.typography.bodySmall, color = Color(palette.label), modifier = Modifier.weight(1f))
        Box(
            Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(accent)
                .semantics { contentDescription = enableLabel }
                .clickable(role = Role.Button) {
                    haptics.perform(SpaceHaptic.Confirm)
                    actions.onEnable()
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            SpaceText(stringResource(R.string.clip_enable), SpaceTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color(palette.background))
        }
    }
}

@Composable
fun ClipSectionBar(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(state.sections.size) { index ->
                val section = state.sections[index]
                ClipChip(section.name, palette, selected = section.name == state.section, locked = section.locked) {
                    actions.onSection(section.name)
                }
            }
            item { ClipChip(stringResource(R.string.clip_add_section), palette, icon = SpaceIcon.FOLDER_ADD, onClick = actions::onNewSection) }
        }
        val name = state.section
        if (name != null && state.confirm == null && state.assigning == null) SectionTools(state, palette, actions, name)
    }
}

@Composable
private fun SectionTools(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions, name: String) {
    Row(Modifier.padding(end = 6.dp)) {
        if (!state.locked) {
            val locked = state.sectionLocked
            val label = stringResource(if (locked) R.string.clip_unlock_section else R.string.clip_lock_section)
            ClipSpaceKey(if (locked) SpaceIcon.UNLOCK else SpaceIcon.LOCK, label, palette, selected = locked, size = TOOL_SIZE, iconSize = TOOL_ICON) {
                actions.onLockSection(name)
            }
        }
        ClipSpaceKey(SpaceIcon.TRASH, stringResource(R.string.clip_delete_section), palette, danger = true, size = TOOL_SIZE, iconSize = TOOL_ICON) {
            actions.onDeleteSection(name)
        }
    }
}

@Composable
fun ClipChip(
    text: String,
    palette: ThemePalette,
    selected: Boolean = false,
    locked: Boolean = false,
    icon: SpaceIcon? = null,
    onClick: () -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val fill = Color(if (selected) palette.keyPressed else palette.keyFill)
    val tint = Color(if (selected) palette.keyTextPressed else palette.keyText)
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fill)
            .semantics { contentDescription = text }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            }
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (locked) ClipIcon(SpaceIcon.LOCK, tint, 14.dp)
        if (icon != null) ClipIcon(icon, tint, 18.dp)
        SpaceText(text, SpaceTheme.typography.bodySmall, color = tint)
    }
}

@Composable
fun ClipSpaceKey(
    icon: SpaceIcon,
    description: String,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    danger: Boolean = false,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    onClick: () -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val fill = if (selected) Color(palette.keyPressed) else Color.Transparent
    val tint = when {
        danger -> ClipDanger
        selected -> Color(palette.keyTextPressed)
        else -> Color(palette.label)
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(fill)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        ClipIcon(icon, tint, iconSize)
    }
}
