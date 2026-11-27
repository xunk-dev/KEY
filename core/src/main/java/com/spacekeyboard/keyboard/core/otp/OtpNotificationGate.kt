package com.spacekeyboard.keyboard.core.otp

class NotificationFacts(
    val packageName: String?,
    val ownPackage: String,
    val ongoing: Boolean,
    val postedAt: Long,
)

object OtpNotificationGate {
    private const val SKEW_MS = 5_000L

    fun accepts(enabled: Boolean, facts: NotificationFacts, now: Long): Boolean {
        val source = facts.packageName ?: return false
        val age = now - facts.postedAt
        return enabled && source != facts.ownPackage && !facts.ongoing && age in -SKEW_MS..OtpExtractor.DEFAULT_TTL_MS
    }

    fun receivedAt(facts: NotificationFacts, now: Long): Long = minOf(facts.postedAt, now)
}
