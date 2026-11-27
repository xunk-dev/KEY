package com.spacekeyboard.keyboard.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpFillerTest {
    private fun run(form: FormModel, certain: Boolean = false): OtpResult {
        var out: OtpResult? = null
        OtpFiller(form, certain).fill(CODE) { out = it }
        form.runAll()
        return checkNotNull(out)
    }

    private fun assertFilled(result: OtpResult, form: FormModel) {
        assertEquals(OtpOutcome.FILLED, result.outcome)
        assertEquals(CODE, form.actual())
    }

    @Test
    fun singleFieldTakesTheWholeCodeInOneCommit() {
        val form = SingleField()
        assertFilled(run(form), form)
        assertEquals(1, form.commits)
    }

    @Test
    fun singleFieldWorksWithoutCursorOrTextReports() {
        val noCursor = SingleField(reportsCursor = false)
        assertFilled(run(noCursor), noCursor)
        val noText = SingleField(readable = false)
        assertFilled(run(noText), noText)
    }

    @Test
    fun separateBoxesFollowTheAutoAdvanceWhateverItsDelay() {
        listOf(0L, 100L, 250L, 380L, 600L, 1200L).forEach { delay ->
            val form = BoxRow(6, delay)
            assertFilled(run(form, certain = true), form)
        }
    }

    @Test
    fun separateBoxesFinishWhenTheFieldEndsAfterTheLastDigit() {
        val form = BoxRow(6, 150, endsAfterLast = true)
        assertFilled(run(form, certain = true), form)
    }

    @Test
    fun separateBoxesStopWhenTheNextBoxNoLongerFits() {
        val form = BoxRow(6, 150, fitsBelow = 3)
        val result = run(form, certain = true)
        assertEquals(OtpOutcome.PARTIAL, result.outcome)
        assertEquals(3, result.placed)
        assertEquals(CODE.drop(3), result.rest(CODE))
    }

    @Test
    fun aBoxThatRefusesInputLeavesAPartialResultWithoutRepeats() {
        val form = BoxRow(6, 150, stuckAt = 3)
        val result = run(form, certain = true)
        assertEquals(OtpOutcome.PARTIAL, result.outcome)
        assertEquals(3, result.placed)
        assertEquals("483", form.actual())
    }

    @Test
    fun spreadPasteIsDetectedOnceAndNotRepeated() {
        listOf(0L, 120L, 400L).forEach { delay ->
            val form = BoxRow(6, delay, spreads = true)
            assertFilled(run(form, certain = true), form)
            assertEquals(1, form.commits)
        }
    }

    @Test
    fun aFieldShorterThanTheCodeIsClearedAndReported() {
        listOf(4, 1).forEach { max ->
            val form = SingleField(max = max)
            assertEquals(OtpOutcome.TOO_SHORT, run(form).outcome)
            assertEquals("", form.actual())
        }
    }

    @Test
    fun aFieldThatRejectsLongInputIsClearedAndReported() {
        val form = SingleField(max = 4, rejectLong = true)
        assertEquals(OtpOutcome.TOO_SHORT, run(form).outcome)
        assertEquals("", form.actual())
    }

    @Test
    fun aRoomierFieldStillTakesTheCode() {
        val form = SingleField(max = 8)
        assertFilled(run(form), form)
    }

    @Test
    fun hiddenFieldsNeverGetRepeatedDigits() {
        listOf(false, true).forEach { masked ->
            listOf(false, true).forEach { cursor ->
                val form = SingleField(readable = masked, masked = masked, reportsCursor = cursor)
                assertFilled(run(form), form)
                assertEquals(CODE.length, form.committedChars)
            }
        }
    }

    @Test
    fun aFullyBlindFieldIsFilledOnce() {
        val form = SingleField(readable = false, reportsCursor = false)
        assertFilled(run(form), form)
        assertEquals(CODE.length, form.committedChars)
    }

    @Test
    fun aPartialCodeIsCompletedNotRetyped() {
        val form = SingleField(initial = "483")
        assertFilled(run(form), form)
        assertEquals(3, form.committedChars)
    }

    @Test
    fun aPartialCodeThatCannotFitIsLeftAlone() {
        val form = SingleField(initial = "483", max = 4)
        assertEquals(OtpOutcome.TOO_SHORT, run(form).outcome)
        assertEquals("483", form.actual())
    }

    @Test
    fun aFieldAlreadyHoldingTheCodeIsNotTouched() {
        val form = SingleField(initial = CODE)
        assertEquals(OtpOutcome.FILLED, run(form).outcome)
        assertEquals(0, form.commits)
    }

    @Test
    fun staleDigitsAreClearedOnlyWhenTheFieldIsCertain() {
        val certain = SingleField(initial = "999")
        assertFilled(run(certain, certain = true), certain)
        val unsure = SingleField(initial = "999")
        assertEquals(OtpOutcome.BLOCKED, run(unsure).outcome)
        assertEquals("999", unsure.actual())
    }

    @Test
    fun otherTextBlocksEvenACertainField() {
        val form = SingleField(initial = "abc")
        assertEquals(OtpOutcome.BLOCKED, run(form, certain = true).outcome)
        assertEquals("abc", form.actual())
    }

    @Test
    fun aGoneFieldIsLostWithoutTyping() {
        val form = SingleField()
        form.isAlive = false
        assertEquals(OtpOutcome.LOST, run(form, certain = true).outcome)
        assertEquals(0, form.commits)
    }

    @Test
    fun aFakeRestartWithoutMovingDoesNotRepeatDigits() {
        val form = SingleField(spurious = true)
        assertFilled(run(form), form)
        assertEquals(CODE.length, form.committedChars)
    }

    @Test
    fun anEmptyCodeIsLost() {
        val result = OtpFiller(SingleField(), false).let { filler ->
            var out: OtpResult? = null
            filler.fill("  ") { out = it }
            checkNotNull(out)
        }
        assertEquals(OtpOutcome.LOST, result.outcome)
        assertTrue(!result.ok)
    }

    private companion object {
        const val CODE = "483920"
    }
}
