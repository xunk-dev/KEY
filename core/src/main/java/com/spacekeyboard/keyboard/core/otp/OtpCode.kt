package com.spacekeyboard.keyboard.core.otp

enum class OtpSource { NOTIFICATION, CLIPBOARD }

data class OtpCode(
    val value: String,
    val receivedAt: Long,
    val ttlMs: Long,
    val source: OtpSource,
    val bare: Boolean,
) {
    val expiresAt: Long get() = receivedAt + ttlMs

    fun ageAt(now: Long): Long = now - receivedAt

    fun spoken(): String = value.toList().joinToString(" ")

    fun shown(): String = when {
        value.length == 6 && value.all(Char::isDigit) -> value.chunked(3).joinToString(" ")
        value.length == 8 && value.all(Char::isDigit) -> value.chunked(4).joinToString(" ")
        else -> value
    }
}

fun OtpMatch.toCode(source: OtpSource, receivedAt: Long): OtpCode =
    OtpCode(code, receivedAt, minOf(ttlMs, OtpExtractor.DEFAULT_TTL_MS), source, bare)
