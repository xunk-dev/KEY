package com.spacekeyboard.keyboard.core.otp

interface OtpTarget {
    fun epoch(): Int
    fun alive(): Boolean
    fun fits(): Boolean
    fun read(): String?
    fun cursor(): Int?
    fun commit(text: String)
    fun erase(count: Int)
    fun wipe()
    fun moveToEnd()
    fun later(delayMs: Long, block: () -> Unit)
}

enum class OtpOutcome { FILLED, PARTIAL, TOO_SHORT, LOST, BLOCKED }

class OtpResult(val outcome: OtpOutcome, val placed: Int, val total: Int) {
    val ok: Boolean get() = outcome == OtpOutcome.FILLED

    fun rest(code: String): String = code.drop(placed.coerceIn(0, code.length))
}
