package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import kotlin.math.abs

internal fun onlyDigitsChanged(previous: LayoutSnapshot, next: LayoutSnapshot, specOf: (LayoutSnapshot) -> LayoutSpec): Boolean {
    val typing = previous.prefs.typing.copy(nativeDigits = next.prefs.typing.nativeDigits)
    val sameShape = specOf(previous).rows.map { it.size } == specOf(next).rows.map { it.size }
    return sameShape && next.activeId == previous.activeId && typing == next.prefs.typing && previous.prefs.copy(typing = next.prefs.typing) == next.prefs
}

internal fun holdDelayFor(key: KeySpec, micAt: () -> Boolean): Long = when {
    key.action != KeyAction.SPACE -> KeyTiming.LONG_PRESS_MS
    micAt() -> KeyTiming.MIC_HOLD_MS
    else -> KeyTiming.SPACE_HOLD_MS
}

internal fun KeyTouch.slidOffSpace(slopPx: Float): Boolean =
    placed.key.action == KeyAction.SPACE && abs(lastX - downX) > slopPx
