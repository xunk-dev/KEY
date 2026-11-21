package com.spacekeyboard.keyboard.ime.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberLayoutSnapshot(controller: LayoutController): LayoutSnapshot {
    var snapshot by remember(controller) { mutableStateOf(controller.snapshot) }
    DisposableEffect(controller) {
        val subscription = controller.subscribe { snapshot = it }
        onDispose { subscription.close() }
    }
    return snapshot
}
