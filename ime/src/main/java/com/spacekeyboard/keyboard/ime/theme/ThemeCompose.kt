package com.spacekeyboard.keyboard.ime.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberThemeSnapshot(controller: ThemeController): ThemeSnapshot {
    var snapshot by remember(controller) { mutableStateOf(controller.snapshot) }
    DisposableEffect(controller) {
        val subscription = controller.subscribe { snapshot = it }
        onDispose { subscription.close() }
    }
    return snapshot
}
