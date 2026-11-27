package com.spacekeyboard.keyboard.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpNotificationGateTest {
    private fun facts(
        source: String? = "com.chat",
        ongoing: Boolean = false,
        postedAt: Long = NOW - 1_000L,
    ) = NotificationFacts(source, "com.spacekeyboard.keyboard", ongoing, postedAt)

    @Test
    fun aFreshMessageFromAnotherAppIsAccepted() {
        assertTrue(OtpNotificationGate.accepts(true, facts(), NOW))
    }

    @Test
    fun theSwitchOffRejectsEverything() {
        assertFalse(OtpNotificationGate.accepts(false, facts(), NOW))
    }

    @Test
    fun ownOngoingAndAnonymousNotificationsAreRejected() {
        assertFalse(OtpNotificationGate.accepts(true, facts(source = "com.spacekeyboard.keyboard"), NOW))
        assertFalse(OtpNotificationGate.accepts(true, facts(ongoing = true), NOW))
        assertFalse(OtpNotificationGate.accepts(true, facts(source = null), NOW))
    }

    @Test
    fun staleNotificationsAreRejectedAndSmallClockSkewIsTolerated() {
        assertFalse(OtpNotificationGate.accepts(true, facts(postedAt = NOW - OtpExtractor.DEFAULT_TTL_MS - 1), NOW))
        assertTrue(OtpNotificationGate.accepts(true, facts(postedAt = NOW + 2_000L), NOW))
        assertFalse(OtpNotificationGate.accepts(true, facts(postedAt = NOW + 60_000L), NOW))
    }

    @Test
    fun receivedTimeNeverLiesInTheFuture() {
        assertEquals(NOW, OtpNotificationGate.receivedAt(facts(postedAt = NOW + 2_000L), NOW))
        assertEquals(NOW - 1_000L, OtpNotificationGate.receivedAt(facts(), NOW))
    }

    @Test
    fun theCodeKeepsAtMostFiveMinutesWhateverTheMessageSays() {
        val match = OtpExtractor.extract("Your code is 483920. Valid for 30 minutes.")!!
        assertEquals(OtpExtractor.DEFAULT_TTL_MS, match.toCode(OtpSource.NOTIFICATION, NOW).ttlMs)
        val short = OtpExtractor.extract("Your code is 483920. It is valid for 2 minutes.")!!
        assertEquals(120_000L, short.toCode(OtpSource.CLIPBOARD, NOW).ttlMs)
    }

    private companion object {
        const val NOW = 1_000_000L
    }
}
