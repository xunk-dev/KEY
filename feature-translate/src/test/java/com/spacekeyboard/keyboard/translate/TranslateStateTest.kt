package com.spacekeyboard.keyboard.translate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateStateTest {
    private val done = TranslateOutcome.Done("hola", 5, TranslateRoute.AI)

    private fun opened() = TranslateState().open("hello", TranslateSource.SELECTION)

    @Test
    fun opensWithSourceAndClosesClean() {
        val state = opened()
        assertTrue(state.open)
        assertEquals("hello", state.source)
        val closed = state.begin().finish(done, false).close()
        assertEquals(TranslateState(remaining = 5), closed)
    }

    @Test
    fun openingAgainForgetsTheDailyCount() {
        val spent = opened().begin().finish(TranslateOutcome.Done("hola", 0, TranslateRoute.AI), true)
        assertEquals(0, spent.remaining)
        assertEquals(null, spent.close().open("hello", TranslateSource.ALL).remaining)
    }

    @Test
    fun blockedFieldsCannotOpen() {
        val state = TranslateState().setAllowed(false).open("x", TranslateSource.ALL)
        assertFalse(state.open)
    }

    @Test
    fun losingPermissionClosesAndClears() {
        val state = opened().begin().finish(done, false).setAllowed(false)
        assertFalse(state.open)
        assertEquals("", state.result)
        assertEquals("", state.source)
        assertTrue(TranslateState().setAllowed(false).setAllowed(true).allowed)
    }

    @Test
    fun finishStoresResultAndRemaining() {
        val state = opened().begin().finish(done, true)
        assertTrue(state.canUseResult)
        assertEquals(TranslateStatus.DONE, state.status)
        assertEquals(5, state.remaining)
        assertEquals(TranslateRoute.AI, state.usedRoute)
        assertTrue(state.fellBack)
    }

    @Test
    fun failureClearsResultAndKeepsReason() {
        val state = opened().begin().finish(done, false).begin().finish(TranslateOutcome.Failed(TranslateFailure.NETWORK), false)
        assertEquals(TranslateStatus.FAILED, state.status)
        assertEquals(TranslateFailure.NETWORK, state.failure)
        assertFalse(state.canUseResult)
    }

    @Test
    fun beginWhileWorkingIsIgnored() {
        val working = opened().begin()
        assertEquals(working, working.begin())
    }

    @Test
    fun changingTargetOrStyleDropsTheOldResult() {
        val state = opened().begin().finish(done, false)
        assertEquals("", state.setTarget("fr").result)
        assertEquals("", state.setStyle(TranslateStyle.FORMAL).result)
        assertEquals(state, state.setTarget(state.target))
        assertEquals(state, state.setStyle(state.style))
        assertEquals(state, state.setTarget("xx"))
    }

    @Test
    fun settingsChangesClearResultsButNotWhileWorking() {
        val state = opened().begin().finish(done, false)
        val same = state.withSettings(state.target, state.style, state.route, state.consent)
        assertEquals(state, same)
        assertEquals("", state.withSettings("de", state.style, state.route, state.consent).result)
        val working = opened().begin()
        assertEquals(TranslateStatus.WORKING, working.withSettings("de", working.style, working.route, true).status)
    }

    @Test
    fun aiReadyNeedsRouteAndConsent() {
        val base = TranslateState()
        assertFalse(base.aiReady)
        assertFalse(base.copy(route = TranslateRoute.AI).aiReady)
        assertTrue(base.copy(route = TranslateRoute.AI, consent = true).aiReady)
        assertNull(base.failure)
    }

    @Test
    fun typingBuildsTheSourceAndDropsAnOldResult() {
        val typed = TranslateState().open("", TranslateSource.NONE).type("مرحبا").begin().finish(done, false).type(" بك")
        assertEquals("مرحبا بك", typed.source)
        assertEquals(TranslateStatus.IDLE, typed.status)
        assertEquals("", typed.result)
    }

    @Test
    fun typingIsIgnoredWhileClosed() {
        assertEquals(TranslateState(), TranslateState().type("x").backspace())
    }

    @Test
    fun backspaceRemovesOneCharacterOrOneEmoji() {
        val state = TranslateState().open("ab\uD83D\uDE00", TranslateSource.NONE)
        assertEquals("ab", state.backspace().source)
        assertEquals("a", state.backspace().backspace().source)
        assertEquals("", state.backspace().backspace().backspace().backspace().source)
    }

    @Test
    fun deleteWordRemovesTheLastWord() {
        val state = TranslateState().open("hello big world", TranslateSource.NONE)
        assertEquals("hello big ", state.deleteWord().source)
        assertEquals("hello ", state.deleteWord().deleteWord().source)
    }

    @Test
    fun typedTextIsCappedJustOverTheLimit() {
        val state = TranslateState().open("", TranslateSource.NONE).type("a".repeat(TranslatePolicy.MAX_TEXT + 50))
        assertEquals(TranslatePolicy.MAX_TEXT + 1, state.source.length)
    }

    @Test
    fun expandedOnlyWhileOpenAndResetsOnClose() {
        assertFalse(TranslateState().setExpanded(true).expanded)
        val open = opened().setExpanded(true)
        assertTrue(open.expanded)
        assertEquals(TranslateState(), open.close())
    }

    @Test
    fun missingModelsAreReadFromTheFailure() {
        val state = opened().begin().finish(TranslateOutcome.Failed(TranslateFailure.NEEDS_MODEL, "ar,fr"), false)
        assertEquals(listOf("ar", "fr"), state.missingModels)
        assertEquals(TranslateStatus.FAILED, state.status)
    }

    @Test
    fun fetchingClearsTheFailureAndItsDetail() {
        val state = opened().begin().finish(TranslateOutcome.Failed(TranslateFailure.NEEDS_MODEL, "ar"), false).fetching()
        assertEquals(TranslateStatus.FETCHING, state.status)
        assertNull(state.failure)
        assertEquals(emptyList<String>(), state.missingModels)
        assertFalse(state.canUseResult)
    }

    @Test
    fun otherFailuresHaveNoMissingModels() {
        val state = opened().begin().finish(TranslateOutcome.Failed(TranslateFailure.NETWORK), false)
        assertEquals(emptyList<String>(), state.missingModels)
    }
}
