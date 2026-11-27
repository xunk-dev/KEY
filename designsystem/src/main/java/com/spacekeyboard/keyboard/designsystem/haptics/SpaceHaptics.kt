package com.spacekeyboard.keyboard.designsystem.haptics

import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

enum class SpaceHaptic {
    Tap,
    Select,
    Tick,
    LongPress,
    DragStart,
    DragEnd,
    Confirm,
    Reject
}

private const val TICK_GAP_MS = 45L

private fun SpaceHaptic.constant(): Int = when (this) {
    SpaceHaptic.Tap -> HapticFeedbackConstants.VIRTUAL_KEY
    SpaceHaptic.Select -> if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_TICK else HapticFeedbackConstants.CLOCK_TICK
    SpaceHaptic.Tick -> if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_FREQUENT_TICK else HapticFeedbackConstants.CLOCK_TICK
    SpaceHaptic.LongPress -> HapticFeedbackConstants.LONG_PRESS
    SpaceHaptic.DragStart -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.GESTURE_START else HapticFeedbackConstants.VIRTUAL_KEY
    SpaceHaptic.DragEnd -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.GESTURE_END else HapticFeedbackConstants.CLOCK_TICK
    SpaceHaptic.Confirm -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
    SpaceHaptic.Reject -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.VIRTUAL_KEY
}

@Stable
class SpaceHaptics(private val view: View) {
    private var lastTick = 0L

    fun perform(kind: SpaceHaptic) {
        if (kind == SpaceHaptic.Tick) {
            val now = SystemClock.uptimeMillis()
            if (now - lastTick < TICK_GAP_MS) return
            lastTick = now
        }
        view.performHapticFeedback(kind.constant())
    }
}

@Composable
fun rememberSpaceHaptics(): SpaceHaptics {
    val view = LocalView.current
    return remember(view) { SpaceHaptics(view) }
}

@Composable
fun HapticOnChange(key: Any?, enabled: Boolean = true, kind: SpaceHaptic = SpaceHaptic.Tick) {
    val haptics = rememberSpaceHaptics()
    val skip = remember { booleanArrayOf(true) }
    LaunchedEffect(key) {
        if (skip[0]) {
            skip[0] = false
            return@LaunchedEffect
        }
        if (enabled) haptics.perform(kind)
    }
}
