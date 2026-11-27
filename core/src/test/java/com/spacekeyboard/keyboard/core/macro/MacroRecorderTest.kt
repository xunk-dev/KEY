package com.spacekeyboard.keyboard.core.macro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroRecorderTest {
    private val recorder = MacroRecorder()

    @Test
    fun typedLettersJoinIntoOneText() {
        "مرحبا".forEach { recorder.add(MacroStep.Text(it.toString())) }
        recorder.add(MacroStep.Enter)
        assertEquals(listOf(MacroStep.Text("مرحبا"), MacroStep.Enter), recorder.steps())
    }

    @Test
    fun deleteRemovesTheLastTypedCharacterInsteadOfBeingRecorded() {
        recorder.add(MacroStep.Text("abc"))
        recorder.add(MacroStep.Delete)
        assertEquals(listOf(MacroStep.Text("ab")), recorder.steps())
    }

    @Test
    fun deleteRemovesAWholeEmojiSequence() {
        recorder.add(MacroStep.Text("a"))
        recorder.add(MacroStep.Text("👍🏽"))
        recorder.add(MacroStep.Delete)
        assertEquals(listOf(MacroStep.Text("a")), recorder.steps())
    }

    @Test
    fun deleteWithNothingTypedBeforeItIsKept() {
        recorder.add(MacroStep.Enter)
        recorder.add(MacroStep.Delete)
        assertEquals(listOf(MacroStep.Enter, MacroStep.Delete), recorder.steps())
    }

    @Test
    fun deletingTheOnlyCharacterDropsTheStep() {
        recorder.add(MacroStep.Text("x"))
        recorder.add(MacroStep.Delete)
        assertEquals(emptyList<MacroStep>(), recorder.steps())
    }

    @Test
    fun stepsAreCapped() {
        repeat(MacroRecorder.MAX_STEPS) { assertTrue(recorder.add(MacroStep.Enter)) }
        assertFalse(recorder.add(MacroStep.Enter))
        assertEquals(MacroRecorder.MAX_STEPS, recorder.size)
    }

    @Test
    fun totalTextIsCapped() {
        assertTrue(recorder.add(MacroStep.Text("a".repeat(MacroRecorder.MAX_CHARS))))
        assertFalse(recorder.add(MacroStep.Text("b")))
    }

    @Test
    fun longTextStartsANewStepInsteadOfGrowingForever() {
        recorder.add(MacroStep.Text("a".repeat(MacroRecorder.MAX_JOIN)))
        recorder.add(MacroStep.Text("b"))
        assertEquals(2, recorder.size)
    }
}
