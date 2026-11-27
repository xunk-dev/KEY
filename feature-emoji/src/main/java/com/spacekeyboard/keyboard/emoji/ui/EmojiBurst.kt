package com.spacekeyboard.keyboard.emoji.ui

import android.os.SystemClock
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptics
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.emojiPress(onTap: () -> Unit, onHold: (() -> Unit)? = null, onBurst: ((Int) -> Unit)? = null): Modifier =
    if (onBurst != null && onHold == null) {
        emojiBurst(onTap, onBurst)
    } else {
        combinedClickable(role = Role.Button, onLongClick = onHold, onClick = onTap)
    }

private fun Modifier.emojiBurst(onTap: () -> Unit, onBurst: (Int) -> Unit): Modifier = composed {
    val haptics = rememberSpaceHaptics()
    val tap by rememberUpdatedState(onTap)
    val burst by rememberUpdatedState(onBurst)
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            when (awaitHoldOrRelease()) {
                Press.Tap -> { haptics.perform(SpaceHaptic.Tap); tap() }
                Press.Held -> { haptics.perform(SpaceHaptic.Tap); tap(); repeatWhileHeld(haptics) { burst(it) } }
                Press.Cancelled -> Unit
            }
        }
    }.semantics {
        role = Role.Button
        onClick { tap(); true }
    }
}

private enum class Press { Tap, Held, Cancelled }

private suspend fun AwaitPointerEventScope.awaitHoldOrRelease(): Press = try {
    val up = withTimeout(EmojiBurstPlan.HOLD_MS) { waitForUpOrCancellation() }
    if (up == null) Press.Cancelled else { up.consume(); Press.Tap }
} catch (_: PointerEventTimeoutCancellationException) {
    Press.Held
}

private suspend fun AwaitPointerEventScope.repeatWhileHeld(haptics: SpaceHaptics, emit: (Int) -> Unit) {
    val begin = SystemClock.uptimeMillis()
    var sent = 1
    var stage = 0
    while (true) {
        val wait = EmojiBurstPlan.stages[EmojiBurstPlan.stageIndex(SystemClock.uptimeMillis() - begin)].intervalMs
        if (!stillDown(wait)) return
        val now = EmojiBurstPlan.stageIndex(SystemClock.uptimeMillis() - begin)
        if (now > stage) { stage = now; haptics.perform(SpaceHaptic.Select) }
        val more = EmojiBurstPlan.batch(stage, sent)
        if (more == 0) {
            haptics.perform(SpaceHaptic.LongPress)
            waitForUpOrCancellation()
            return
        }
        emit(more)
        sent += more
    }
}

private suspend fun AwaitPointerEventScope.stillDown(ms: Long): Boolean = try {
    withTimeout(ms) { waitForUpOrCancellation() }
    false
} catch (_: PointerEventTimeoutCancellationException) {
    true
}
