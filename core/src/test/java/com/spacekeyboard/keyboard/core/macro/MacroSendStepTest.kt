package com.spacekeyboard.keyboard.core.macro

import org.junit.Assert.assertEquals
import org.junit.Test

class MacroSendStepTest {
    @Test
    fun sendStepSurvivesEncodeAndDecode() {
        val steps = listOf(MacroStep.Text("hi"), MacroStep.Send, MacroStep.Enter)
        assertEquals(steps, MacroCodec.decode(MacroCodec.encode(steps)))
    }

    @Test
    fun sendStepIsShownInThePreview() {
        val macro = Macro(1L, listOf(MacroStep.Text("hi"), MacroStep.Send))
        assertEquals(true, macro.preview().contains("➤"))
    }
}
