package com.spacekeyboard.keyboard.dock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.R
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics

private const val PRESSED_SCALE = 0.9f

@Composable
internal fun KeyboardFab(palette: DockPalette, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) PRESSED_SCALE else 1f, spring(0.5f, 600f), label = "fab")
    val haptics = rememberSpaceHaptics()
    val label = stringResource(R.string.dock_keyboard_try)
    val icon = remember { tryIcon() }
    Box(
        modifier = modifier
            .size(DockMetrics.FabSize)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .drawBehind { drawFabGlow(palette.glow) }
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(palette.gradientTop, palette.gradientBottom)))
            .drawBehind { drawFabRelief() }
            .border(2.dp, palette.ring, CircleShape)
            .pointerInput(onOpen) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { haptics.perform(SpaceHaptic.Tap); onOpen() },
                )
            }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = label
                onClick { onOpen(); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(icon, contentDescription = null, modifier = Modifier.size(DockMetrics.TryIconSize))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFabRelief() {
    val relief = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.55f),
        0.08f to Color.Transparent,
        0.72f to Color.Transparent,
        1f to Color(0xFF783C00).copy(alpha = 0.28f),
    )
    drawCircle(relief)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFabGlow(glow: Color) {
    val radius = size.minDimension * 0.95f
    drawCircle(Brush.radialGradient(listOf(glow, Color.Transparent), center, radius), radius, center)
}
