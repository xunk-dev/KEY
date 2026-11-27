package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView

internal const val CENTER_DP = 56
internal const val SIDE_BUTTON_DP = 44

@Composable
internal fun CircleButton(
    icon: SpaceIcon,
    label: String,
    fill: Color,
    tint: Color,
    size: Dp,
    ring: Color = Color.Transparent,
    onClick: () -> Unit,
) {
    val tap by rememberUpdatedState(onClick)
    Box(
        Modifier
            .size(size)
            .border(1.dp, ring, CircleShape)
            .clip(CircleShape)
            .background(fill)
            .semantics { contentDescription = label }
            .pointerInput(Unit) { detectTapGestures { tap() } },
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, tint, Modifier, size * 0.5f)
    }
}

@Composable
internal fun Grip(horizontal: Boolean, accent: Color, modifier: Modifier) {
    val pill = if (horizontal) DpSize(76.dp, 8.dp) else DpSize(8.dp, 52.dp)
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.size(pill).clip(RoundedCornerShape(4.dp)).background(accent))
    }
}
