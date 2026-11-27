package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.calc.CalcKey
import com.spacekeyboard.keyboard.core.calc.CalcReducer
import com.spacekeyboard.keyboard.core.calc.CalcState

class CalculatorController {
    var open by mutableStateOf(false)
        private set

    var state by mutableStateOf(CalcState())
        private set

    var onChange: (() -> Unit)? = null

    fun show() = set(true)

    fun close() = set(false)

    fun press(key: CalcKey) {
        state = CalcReducer.press(state, key)
    }

    private fun set(value: Boolean) {
        if (open == value) return
        open = value
        onChange?.invoke()
    }
}
