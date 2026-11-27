package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign
import kotlinx.coroutines.launch

internal val ClipTitleInk = Color(0xFF8FB4FF)
internal val ClipPinInk = Color(0xFFF2B658)

private val REVEAL = 96.dp
private val COMMIT = 64.dp
private val STRETCH = 40.dp
private val CARD_RADIUS = 16.dp
private const val FILL_BASE = 0.12f
private const val FILL_ARMED = 0.26f
private const val EDGE_BASE = 0.45f
private const val SHRINK = 0.04f
private const val TILT = 0.025f

private fun damp(raw: Float, commit: Float, stretch: Float): Float {
    val a = abs(raw)
    if (a <= commit) return raw
    return sign(raw) * (commit + stretch * (1f - exp(-(a - commit) / stretch)))
}

@Composable
internal fun SwipeCard(
    pinned: Boolean,
    onTitle: () -> Unit,
    onPin: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val commit = with(density) { COMMIT.toPx() }
    val stretch = with(density) { STRETCH.toPx() }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = rememberSpaceHaptics()
    val latestTitle by rememberUpdatedState(onTitle)
    val latestPin by rememberUpdatedState(onPin)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val leftEdge = if (rtl) Alignment.CenterEnd else Alignment.CenterStart
    val rightEdge = if (rtl) Alignment.CenterStart else Alignment.CenterEnd
    val x = offset.value
    val progress = (abs(x) / commit).coerceIn(0f, 1f)
    val armed = abs(x) >= commit

    Box(
        modifier.pointerInput(Unit) {
            var raw = 0f
            var wasArmed = false
            val settle = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            detectHorizontalDragGestures(
                onDragStart = {
                    raw = offset.value
                    wasArmed = false
                },
                onDragEnd = {
                    val end = offset.value
                    scope.launch { offset.animateTo(0f, settle) }
                    if (abs(end) >= commit) haptics.perform(SpaceHaptic.Confirm)
                    if (end >= commit) latestTitle() else if (end <= -commit) latestPin()
                },
                onDragCancel = { scope.launch { offset.animateTo(0f, settle) } },
                onHorizontalDrag = { change, delta ->
                    change.consume()
                    raw += delta
                    val next = damp(raw, commit, stretch)
                    val crossed = abs(next) >= commit
                    if (crossed != wasArmed) {
                        wasArmed = crossed
                        haptics.perform(SpaceHaptic.Tap)
                    }
                    scope.launch { offset.snapTo(next) }
                },
            )
        },
    ) {
        if (x > 0f) {
            Underlay(
                icon = SpaceIcon.PENCIL,
                label = stringResource(R.string.clip_swipe_title),
                ink = ClipTitleInk,
                widthPx = x,
                progress = progress,
                armed = armed,
                edge = leftEdge,
                modifier = Modifier.align(leftEdge),
            )
        } else if (x < 0f) {
            Underlay(
                icon = SpaceIcon.PIN,
                label = stringResource(if (pinned) R.string.clip_unpin else R.string.clip_pin),
                ink = ClipPinInk,
                widthPx = -x,
                progress = progress,
                armed = armed,
                edge = rightEdge,
                modifier = Modifier.align(rightEdge),
            )
        }
        Box(
            Modifier.graphicsLayer {
                val shrink = 1f - SHRINK * progress
                translationX = x
                scaleX = shrink
                scaleY = shrink
                rotationZ = x / density.density * TILT
                transformOrigin = TransformOrigin(if (x >= 0f) 0f else 1f, 1f)
            },
        ) { content() }
    }
}

@Composable
private fun Underlay(
    icon: SpaceIcon,
    label: String,
    ink: Color,
    widthPx: Float,
    progress: Float,
    armed: Boolean,
    edge: Alignment,
    modifier: Modifier,
) {
    val width: Dp = with(LocalDensity.current) { widthPx.toDp() }
    val shape = RoundedCornerShape(CARD_RADIUS)
    val arm by animateFloatAsState(
        targetValue = if (armed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "clipSwipeArm",
    )
    val fill = FILL_BASE + (FILL_ARMED - FILL_BASE) * arm
    val edgeAlpha = EDGE_BASE + (1f - EDGE_BASE) * arm
    Box(
        modifier
            .width(width)
            .fillMaxHeight()
            .graphicsLayer { alpha = 0.35f + 0.65f * progress }
            .clip(shape)
            .background(ink.copy(alpha = fill))
            .border(1.dp, ink.copy(alpha = edgeAlpha), shape),
    ) {
        Row(
            Modifier
                .requiredWidth(REVEAL)
                .fillMaxHeight()
                .align(edge)
                .graphicsLayer {
                    val s = 0.9f + 0.1f * progress
                    scaleX = s
                    scaleY = s
                },
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val lift = 0.7f + 0.3f * progress + 0.25f * arm
            ClipIcon(icon, ink, 20.dp, Modifier.graphicsLayer { scaleX = lift; scaleY = lift })
            SpaceText(label, SpaceTheme.typography.bodySmall, color = ink, maxLines = 1)
        }
    }
}
