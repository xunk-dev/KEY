package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import kotlin.math.roundToInt

private const val FLIP_DP = 56f
private val FRAME_RADIUS = 22.dp

private fun PanelMode.sizable() = this == PanelMode.NORMAL || this == PanelMode.ONE_HAND_LEFT || this == PanelMode.ONE_HAND_RIGHT

private fun Modifier.swallowTouches(): Modifier =
    pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }

@Composable
fun ResizeOverlay(theme: ThemeController, layouts: LayoutController, resize: ResizeController) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val palette = rememberThemeSnapshot(theme).palette
        val snapshot = rememberLayoutSnapshot(layouts)
        val density = LocalDensity.current.density
        val screenPx = LocalContext.current.resources.displayMetrics.heightPixels
        val latest = rememberUpdatedState(snapshot)
        BoxWithConstraints(Modifier.fillMaxSize().swallowTouches()) {
            val width = constraints.maxWidth.toFloat()
            val (leftEdge, rightEdge) = edgesOf(snapshot.prefs, width)
            val ctx = ResizeCtx(
                layouts, latest, Color(palette.glow), Color(palette.background), density, screenPx, width, leftEdge, rightEdge,
            )
            Frame(ctx, snapshot.prefs, resize)
        }
    }
}

@Composable
private fun BoxScope.Frame(c: ResizeCtx, prefs: KeyboardPrefs, resize: ResizeController) {
    val areaWidth = with(LocalDensity.current) { (c.rightEdge - c.leftEdge).coerceAtLeast(0f).toDp() }
    Box(
        Modifier
            .align(Alignment.TopStart)
            .offset { IntOffset(c.leftEdge.roundToInt(), 0) }
            .width(areaWidth)
            .fillMaxHeight()
            .border(2.dp, c.accent, RoundedCornerShape(FRAME_RADIUS)),
    ) {
        HeightGrip(c)
        if (prefs.mode.sizable()) {
            if (prefs.mode != PanelMode.ONE_HAND_LEFT) LeftGrip(c)
            if (prefs.mode != PanelMode.ONE_HAND_RIGHT) RightGrip(c)
        }
        Controls(c, prefs, resize)
    }
}

@Composable
private fun BoxScope.Controls(c: ResizeCtx, prefs: KeyboardPrefs, resize: ResizeController) {
    val ring = c.accent.copy(alpha = 0.55f)
    val glass = c.ink.copy(alpha = 0.82f)
    Column(
        Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CircleButton(SpaceIcon.UNDO, stringResource(R.string.resize_reset), glass, c.accent, SIDE_BUTTON_DP.dp, ring) {
                c.layouts.resetSize()
            }
            if (prefs.mode.sizable()) MoveHandle(c, glass, ring)
            CircleButton(SpaceIcon.CHECK, stringResource(R.string.resize_done), c.accent, c.ink, SIDE_BUTTON_DP.dp) { resize.close() }
        }
        SizeTag(c, prefs, glass, ring)
    }
}

@Composable
private fun SizeTag(c: ResizeCtx, prefs: KeyboardPrefs, glass: Color, ring: Color) {
    val shape = RoundedCornerShape(13.dp)
    Box(Modifier.border(1.dp, ring, shape).clip(shape).background(glass).padding(horizontal = 12.dp, vertical = 4.dp)) {
        SpaceText("${widthOf(prefs)}% × ${prefs.heightPercent}%", SpaceTheme.typography.bodySmall, color = c.accent)
    }
}

@Composable
private fun MoveHandle(c: ResizeCtx, glass: Color, ring: Color) {
    val drag = remember { Drag() }
    val label = stringResource(R.string.resize_side)
    Box(
        Modifier
            .size(CENTER_DP.dp)
            .border(1.dp, ring, CircleShape)
            .clip(CircleShape)
            .background(glass)
            .semantics { contentDescription = label }
            .rawDrag(
                onStart = { drag.target = null },
                onDrag = { dx, _ ->
                    val threshold = FLIP_DP * c.density
                    val target = when {
                        dx < -threshold -> PanelMode.ONE_HAND_LEFT
                        dx > threshold -> PanelMode.ONE_HAND_RIGHT
                        else -> null
                    }
                    if (target != null && target != drag.target) {
                        drag.target = target
                        if (c.latest.value.prefs.mode != target) c.layouts.setMode(target)
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(SpaceIcon.SWAP_SIDE, c.accent, Modifier, 30.dp)
    }
}
