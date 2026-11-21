package com.spacekeyboard.keyboard.translate

import org.junit.Assert.assertEquals
import org.junit.Test

class TranslateGateTest {
    private val base = GateInput(
        allowed = true,
        requested = true,
        text = "hello",
        route = TranslateRoute.AI,
        consent = true,
        configured = true,
        remaining = null,
    )

    private fun stop(failure: TranslateFailure) = GateDecision.Stop(failure)

    @Test
    fun aiGoesWhenEverythingIsInPlace() {
        assertEquals(GateDecision.Go(TranslateRoute.AI, false), TranslateGate.decide(base))
        assertEquals(GateDecision.Go(TranslateRoute.AI, false), TranslateGate.decide(base.copy(remaining = 3)))
    }

    @Test
    fun passwordFieldsNeverSendAnything() {
        assertEquals(stop(TranslateFailure.FIELD_BLOCKED), TranslateGate.decide(base.copy(allowed = false)))
        assertEquals(stop(TranslateFailure.FIELD_BLOCKED), TranslateGate.decide(base.copy(allowed = false, route = TranslateRoute.DEVICE)))
    }

    @Test
    fun nothingRunsWithoutAPress() {
        assertEquals(stop(TranslateFailure.NOT_REQUESTED), TranslateGate.decide(base.copy(requested = false)))
        assertEquals(stop(TranslateFailure.NOT_REQUESTED), TranslateGate.decide(base.copy(requested = false, route = TranslateRoute.DEVICE)))
    }

    @Test
    fun emptyAndLongTextStop() {
        assertEquals(stop(TranslateFailure.EMPTY), TranslateGate.decide(base.copy(text = " \n")))
        assertEquals(stop(TranslateFailure.TOO_LONG), TranslateGate.decide(base.copy(text = "x".repeat(TranslatePolicy.MAX_TEXT + 1))))
        assertEquals(GateDecision.Go(TranslateRoute.AI, false), TranslateGate.decide(base.copy(text = "x".repeat(TranslatePolicy.MAX_TEXT))))
    }

    @Test
    fun aiWithoutConsentStopsAndNeverFallsBackSilently() {
        assertEquals(stop(TranslateFailure.NO_CONSENT), TranslateGate.decide(base.copy(consent = false)))
    }

    @Test
    fun deviceRouteNeedsNoConsentAndIgnoresQuota() {
        val input = base.copy(route = TranslateRoute.DEVICE, consent = false, remaining = 0, configured = false)
        assertEquals(GateDecision.Go(TranslateRoute.DEVICE, false), TranslateGate.decide(input))
    }

    @Test
    fun exhaustedQuotaOrMissingConfigFallsBackToDevice() {
        assertEquals(GateDecision.Go(TranslateRoute.DEVICE, true), TranslateGate.decide(base.copy(remaining = 0)))
        assertEquals(GateDecision.Go(TranslateRoute.DEVICE, true), TranslateGate.decide(base.copy(configured = false)))
    }
}
