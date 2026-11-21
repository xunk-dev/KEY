package com.spacekeyboard.keyboard.translate

import org.junit.Assert.assertEquals
import org.junit.Test

class TranslateReplyTest {
    private fun failed(failure: TranslateFailure) = TranslateOutcome.Failed(failure)

    @Test
    fun successCarriesTextAndRemaining() {
        val outcome = TranslateReply.from(200, "{\"text\":\"مرحبا\",\"remaining\":4}")
        assertEquals(TranslateOutcome.Done("مرحبا", 4, TranslateRoute.AI), outcome)
    }

    @Test
    fun successWithoutRemainingIsStillDone() {
        assertEquals(TranslateOutcome.Done("x", null, TranslateRoute.AI), TranslateReply.from(200, "{\"text\":\"x\"}"))
    }

    @Test
    fun brokenSuccessBodiesAreServerErrors() {
        listOf("", "{}", "{\"text\":\"  \"}", "{\"text\":5}", "html").forEach {
            assertEquals(failed(TranslateFailure.SERVER), TranslateReply.from(200, it))
        }
    }

    @Test
    fun statusCodesMapToFailures() {
        assertEquals(failed(TranslateFailure.QUOTA), TranslateReply.from(429, ""))
        assertEquals(failed(TranslateFailure.REJECTED), TranslateReply.from(401, ""))
        assertEquals(failed(TranslateFailure.REJECTED), TranslateReply.from(400, ""))
        assertEquals(failed(TranslateFailure.SERVER), TranslateReply.from(502, ""))
        assertEquals(failed(TranslateFailure.SERVER), TranslateReply.from(302, ""))
    }
}
