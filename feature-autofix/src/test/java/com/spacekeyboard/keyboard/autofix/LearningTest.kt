package com.spacekeyboard.keyboard.autofix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningTest {
    private fun slip(model: ErrorModel, times: Int, config: AutoFixConfig = AutoFixConfig(live = false, words = false)) {
        repeat(times) { Typing(config, model).type("كتاي").back().type("ب").space() }
    }

    @Test
    fun correctionByHandIsLearnedAsLetterAndWord() {
        val model = ErrorModel()
        slip(model, 1)
        assertEquals(1, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.fixed)
        assertEquals(1, model.words.rule("كتاي", "كتاب")?.fixed)
        assertEquals(1, model.zones.rule(Cell('ي', 4), 'ب')?.fixed)
    }

    @Test
    fun repeatedSlipIsFixedWhileTyping() {
        val model = ErrorModel()
        slip(model, 3)
        val session = Typing(model = model).type("كتاي")
        assertEquals("كتاب", session.text)
    }

    @Test
    fun repeatedSlipIsFixedAtWordEndWhenLiveFixIsOff() {
        val model = ErrorModel()
        slip(model, 3)
        val session = Typing(AutoFixConfig(live = false), model).type("كتاي")
        assertEquals("كتاي", session.text)
        session.space()
        assertEquals("كتاب ", session.text)
    }

    @Test
    fun nothingChangesBeforeEnoughEvidence() {
        val model = ErrorModel()
        slip(model, 2)
        assertEquals("كتاي", Typing(model = model).type("كتاي").text)
        assertEquals("كتاي ", Typing(model = model).type("كتاي").space().text)
    }

    @Test
    fun gentleLevelWaitsForMoreEvidence() {
        val model = ErrorModel()
        slip(model, 3)
        val gentle = AutoFixConfig(level = AutoFixLevel.GENTLE)
        assertEquals("كتاي", Typing(gentle, model).type("كتاي").text)
        slip(model, 1)
        assertEquals("كتاب", Typing(gentle, model).type("كتاي").text)
    }

    @Test
    fun boldLevelActsEarlier() {
        val model = ErrorModel()
        slip(model, 2)
        assertEquals("كتاب", Typing(AutoFixConfig(level = AutoFixLevel.BOLD), model).type("كتاي").text)
    }

    @Test
    fun transpositionIsLearnedAsWordFix() {
        val model = ErrorModel()
        repeat(3) { Typing(model = model).type("كاتب").back(3).type("تاب").space() }
        assertEquals("كتاب ", Typing(model = model).type("كاتب").space().text)
        assertTrue(model.contexts.size == 0)
    }

    @Test
    fun missingLetterIsLearnedAsWordFix() {
        val model = ErrorModel()
        repeat(3) { Typing(model = model).type("كتب").back().type("اب").space() }
        assertEquals("كتاب ", Typing(model = model).type("كتب").space().text)
    }

    @Test
    fun extraLetterIsLearnedAsWordFix() {
        val model = ErrorModel()
        repeat(3) { Typing(model = model).type("مكتتب").back(3).type("تب").space() }
        assertEquals("مكتب ", Typing(model = model).type("مكتتب").space().text)
    }

    @Test
    fun slipNoticedAfterTheSpaceIsStillLearned() {
        val model = ErrorModel()
        Typing(model = model).type("كتاي").space().back(2).type("ب").space()
        assertEquals(1, model.words.rule("كتاي", "كتاب")?.fixed)
        assertEquals(1, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.fixed)
    }

    @Test
    fun rewritingADifferentWordLearnsNothing() {
        val model = ErrorModel()
        Typing(model = model).type("كتاب").back(4).type("قلم").space()
        assertTrue(model.isEmpty())
    }

    @Test
    fun changingMindAfterACorrectionLearnsNothing() {
        val model = ErrorModel()
        Typing(model = model).type("كتاي").back().type("ب").back().type("ت").space()
        assertNull(model.contexts.rule(Spot("كتا", 'ي'), 'ب'))
        assertEquals(1, model.contexts.rule(Spot("كتا", 'ي'), 'ت')?.fixed)
    }

    @Test
    fun correctionThatKeepsTypingStillCountsTheSlip() {
        val model = ErrorModel()
        Typing(model = model).type("كتاي").back().type("به").space()
        assertEquals(1, model.contexts.rule(Spot("كتا", 'ي'), 'ب')?.fixed)
    }

    @Test
    fun shortWordsAreNotLearnedAsWords() {
        val model = ErrorModel()
        repeat(3) { Typing(model = model).type("في").back().type("ن").space() }
        assertEquals(0, model.words.size)
    }
}
