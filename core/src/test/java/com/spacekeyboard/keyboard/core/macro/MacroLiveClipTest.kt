package com.spacekeyboard.keyboard.core.macro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroLiveClipTest {
    private val recorder = MacroRecorder()

    @Test
    fun pastedTextStaysAStepOfItsOwn() {
        recorder.add(MacroStep.Text("hi "))
        assertTrue(recorder.addPasted("link"))
        assertEquals(listOf(MacroStep.Text("hi "), MacroStep.Text("link")), recorder.steps())
    }

    @Test
    fun theLastPasteCanBecomeALivePaste() {
        recorder.add(MacroStep.Text("hi "))
        recorder.addPasted("link")
        assertTrue(recorder.liveLast())
        assertEquals(listOf(MacroStep.Text("hi "), MacroStep.LiveClip), recorder.steps())
    }

    @Test
    fun onlyTheStepRightAfterThePasteCanBeConverted() {
        recorder.addPasted("link")
        recorder.add(MacroStep.Text("!"))
        assertFalse(recorder.liveLast())
        assertEquals(listOf(MacroStep.Text("link!")), recorder.steps())
    }

    @Test
    fun typedTextIsNeverTurnedIntoALivePaste() {
        recorder.add(MacroStep.Text("abc"))
        assertFalse(recorder.liveLast())
        assertEquals(listOf(MacroStep.Text("abc")), recorder.steps())
    }

    @Test
    fun convertingTwiceDoesNothingTheSecondTime() {
        recorder.addPasted("x")
        assertTrue(recorder.liveLast())
        assertFalse(recorder.liveLast())
        assertEquals(listOf(MacroStep.LiveClip), recorder.steps())
    }

    @Test
    fun textTypedAfterALivePasteStartsANewStep() {
        recorder.addPasted("x")
        recorder.liveLast()
        recorder.add(MacroStep.Text("y"))
        assertEquals(listOf(MacroStep.LiveClip, MacroStep.Text("y")), recorder.steps())
    }

    @Test
    fun deleteAfterALivePasteIsKeptAsAStep() {
        recorder.addPasted("x")
        recorder.liveLast()
        recorder.add(MacroStep.Delete)
        assertEquals(listOf(MacroStep.LiveClip, MacroStep.Delete), recorder.steps())
    }

    @Test
    fun aLivePasteFreesTheCharactersItNoLongerHolds() {
        recorder.addPasted("a".repeat(MacroRecorder.MAX_CHARS - 10))
        recorder.liveLast()
        assertTrue(recorder.addPasted("b".repeat(MacroRecorder.MAX_CHARS - 10)))
    }

    @Test
    fun pasteIsRefusedWhenTheRecordingIsFull() {
        repeat(MacroRecorder.MAX_STEPS) { recorder.add(MacroStep.Enter) }
        assertFalse(recorder.addPasted("x"))
        assertEquals(MacroRecorder.MAX_STEPS, recorder.size)
    }

    @Test
    fun emptyPasteChangesNothing() {
        assertTrue(recorder.addPasted(""))
        assertFalse(recorder.liveLast())
        assertEquals(0, recorder.size)
    }

    @Test
    fun previewShowsALivePasteMark() {
        val macro = Macro(1, listOf(MacroStep.Text("a"), MacroStep.LiveClip, MacroStep.Text("b")))
        assertEquals("a 📋 b", macro.preview())
    }
}
