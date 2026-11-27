package com.spacekeyboard.keyboard.translate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberTranslateState(controller: TranslateController): TranslateState {
    var state by remember(controller) { mutableStateOf(controller.state) }
    DisposableEffect(controller) {
        val subscription = controller.subscribe { state = it }
        onDispose { subscription.close() }
    }
    return state
}
