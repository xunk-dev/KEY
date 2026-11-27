package com.spacekeyboard.keyboard.symbols.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberPanelState(model: SymbolPanelModel): PanelState {
    var state by remember(model) { mutableStateOf(model.snapshot) }
    DisposableEffect(model) {
        val subscription = model.subscribe { state = it }
        onDispose { subscription.close() }
    }
    return state
}
