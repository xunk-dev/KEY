package com.spacekeyboard.keyboard.designsystem.a11y

import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics

val MinTouchTarget: Dp = 48.dp

fun Modifier.touchTarget(min: Dp = MinTouchTarget): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val width = maxOf(placeable.width, min.roundToPx()).coerceAtMost(constraints.maxWidth)
    val height = maxOf(placeable.height, min.roundToPx()).coerceAtMost(constraints.maxHeight)
    layout(width, height) { placeable.place((width - placeable.width) / 2, (height - placeable.height) / 2) }
}

private fun Modifier.hapticClickable(
    kind: SpaceHaptic,
    enabled: Boolean,
    onClickLabel: String?,
    role: Role?,
    onClick: () -> Unit
): Modifier = composed {
    val haptics = rememberSpaceHaptics()
    var focused by remember { mutableStateOf(false) }
    val ring = SpaceTheme.colors.primary
    onFocusChanged { focused = it.isFocused }
        .then(if (focused) Modifier.border(2.dp, ring, RoundedCornerShape(12.dp)) else Modifier)
        .clickable(enabled = enabled, onClickLabel = onClickLabel, role = role, onClick = {
            haptics.perform(kind)
            onClick()
        }).touchTarget()
}

fun Modifier.spaceClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = Role.Button,
    onClick: () -> Unit
): Modifier = hapticClickable(SpaceHaptic.Tap, enabled, onClickLabel, role, onClick)

fun Modifier.spaceSelectable(
    selected: Boolean,
    role: Role? = Role.Tab,
    onClick: () -> Unit
): Modifier = hapticClickable(SpaceHaptic.Select, true, null, role, onClick).semantics { this.selected = selected }

fun Modifier.spaceToggle(checked: Boolean, onChange: (Boolean) -> Unit): Modifier = composed {
    val haptics = rememberSpaceHaptics()
    toggleable(value = checked, role = Role.Switch, onValueChange = {
        haptics.perform(SpaceHaptic.Select)
        onChange(it)
    }).touchTarget()
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.spaceCombinedClickable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    role: Role? = Role.Button
): Modifier = composed {
    val haptics = rememberSpaceHaptics()
    combinedClickable(role = role, onLongClickLabel = onLongClickLabel, onLongClick = onLongClick?.let { long -> { haptics.perform(SpaceHaptic.LongPress); long() } }, onClick = {
        haptics.perform(SpaceHaptic.Tap)
        onClick()
    })
}

fun Modifier.liveMessage(): Modifier = semantics { liveRegion = LiveRegionMode.Polite }

fun Modifier.screenHeading(): Modifier = semantics { heading() }
