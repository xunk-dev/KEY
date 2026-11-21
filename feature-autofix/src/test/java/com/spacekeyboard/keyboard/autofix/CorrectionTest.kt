package com.spacekeyboard.keyboard.autofix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CorrectionTest {
    private fun trained(times: Int = 3): ErrorModel {
        val model = ErrorModel()
        val off = AutoFixConfig(live = false, words = false)
        repeat(times) { Typing(off, model).type("كتاي").back().type("ب").space() }
        return model
    }

    @Test
    fun backspaceRightAfterAWordFixRestoresTheOriginal() {
        val model = trained()
        val session = Typing(AutoFixConfig(live = false), model).type("كتاي").space()
        assertEquals("كتاب ", session.text)
        session.back()
        assertEquals("كتاي", session.text)
    }

    @Test
    fun oneUndoSwitchesOffAFreshWordFix() {
        val model = trained()
        val config = AutoFixConfig(live = false)
        Typing(config, model).type("كتاي").space().back()
        assertEquals("كتاي ", Typing(config, model).type("كتاي").space().text)
        assertEquals(1, model.words.rule("كتاي", "كتاب")?.undone)
    }

    @Test
    fun aWellEstablishedWordFixSurvivesOneUndo() {
        val model = trained(8)
        val config = AutoFixConfig(live = false)
        Typing(config, model).type("كتاي").space().back()
        assertEquals("كتاب ", Typing(config, model).type("كتاي").space().text)
    }

    @Test
    fun keepingTheFixCountsAsApplied() {
        val model = trained()
        Typing(AutoFixConfig(live = false), model).type("كتاي").space().type("و")
        assertEquals(1, model.words.rule("كتاي", "كتاب")?.applied)
    }

    @Test
    fun deletingALiveFixOnceStopsItAtThatSpot() {
        val model = trained()
        val session = Typing(model = model).type("كتاي")
        assertEquals("كتاب", session.text)
        session.back().type("ي")
        assertEquals("كتاي", session.text)
        assertEquals(1, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.undone)
    }

    @Test
    fun keptLiveFixesCountAsApplied() {
        val model = trained()
        Typing(model = model).type("كتاي").space()
        assertEquals(1, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.applied)
    }

    @Test
    fun otherWordsAreNeverTouched() {
        val model = trained()
        assertEquals("كتاس", Typing(model = model).type("كتاس").text)
        assertEquals("قلم ", Typing(model = model).type("قلم").space().text)
    }

    @Test
    fun aDifferentPrefixIsNotFixed() {
        val model = trained()
        assertEquals("ذكتاي", Typing(AutoFixConfig(words = false), model).type("ذكتاي").text)
    }

    @Test
    fun wordStuckInsideALongerWordIsNotFixed() {
        val model = trained()
        val session = Typing(AutoFixConfig(live = false), model)
        session.pad.commit("س")
        session.engine.reset()
        session.type("كتاي").space()
        assertEquals("سكتاي ", session.text)
    }

    @Test
    fun enterFixesTheWordToo() {
        val model = trained()
        val session = Typing(AutoFixConfig(live = false), model).type("كتاي").enter()
        assertEquals("كتاب\n", session.text)
    }

    @Test
    fun punctuationFixesTheWordAndStaysTypedAfterBackspaceUndo() {
        val model = trained()
        val session = Typing(AutoFixConfig(live = false), model)
        session.type("كتاي")
        session.type(".")
        assertEquals("كتاب.", session.text)
        session.back()
        assertEquals("كتاي", session.text)
    }

    @Test
    fun switchedOffEngineNeitherLearnsNorChangesText() {
        val model = trained()
        val before = model.contexts.size
        val session = Typing(AutoFixConfig(enabled = false), model).type("كتاي").space().back(2).type("ب")
        assertEquals("كتاب", session.text)
        assertEquals(before, model.contexts.size)
        assertEquals(3, model.words.rule("كتاي", "كتاب")?.fixed)
    }

    @Test
    fun blockedFieldNeitherLearnsNorFixes() {
        val model = trained()
        val session = Typing(model = model, mayLearn = false, mayFix = false).type("كتاي").space()
        assertEquals("كتاي ", session.text)
        Typing(model = model, mayLearn = false, mayFix = false).type("كتاي").back().type("ب").space()
        assertEquals(3, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.fixed)
    }

    @Test
    fun learningOffStillAllowsFixing() {
        val model = trained()
        val session = Typing(model = model, mayLearn = false, mayFix = true).type("كتاي").space()
        assertEquals("كتاب ", session.text)
        assertEquals(0, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.applied)
    }

    @Test
    fun selectionDeleteResetsTracking() {
        val model = ErrorModel()
        val session = Typing(model = model).type("كتاي")
        session.pad.selected = true
        session.back()
        session.pad.selected = false
        session.type("ب").space()
        assertTrue(model.isEmpty())
    }

    @Test
    fun typedDigitsMakeTheTokenOpaque() {
        val model = trained()
        val session = Typing(AutoFixConfig(live = false), model).type("كتاي").type("1")
        assertEquals("كتاي1", session.text)
        val code = Typing(AutoFixConfig(), model).type("7").type("كتاي").space()
        assertEquals("7كتاي ", code.text)
        assertFalse(model.isEmpty())
        assertNotNull(model)
    }
}
