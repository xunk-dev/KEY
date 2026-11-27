package com.spacekeyboard.keyboard.core.otp

class OtpFiller(private val target: OtpTarget, private val certain: Boolean) {
    private class Snap(val epoch: Int, val text: String?, val raw: Int?, val cursor: Int?)

    fun fill(code: String, done: (OtpResult) -> Unit) {
        val want = code.filter(Char::isLetterOrDigit)
        if (want.isEmpty()) return done(OtpResult(OtpOutcome.LOST, 0, 0))
        Run(want, done).start()
    }

    private inner class Run(val want: String, val done: (OtpResult) -> Unit) {
        var from = 0
        var pos = 0
        var steps = 0
        var finished = false
        var startedEmpty = true
        var travelled = false
        var patient = false

        fun start() {
            if (!target.alive()) return finish(OtpOutcome.LOST)
            val seen = target.read()?.let(OtpText::flat).orEmpty()
            startedEmpty = seen.isEmpty()
            when {
                seen.isEmpty() -> burst()
                want.equals(seen, ignoreCase = true) -> finish(OtpOutcome.FILLED, want.length)
                want.startsWith(seen, ignoreCase = true) -> resume(seen.length)
                certain && seen.all(Char::isDigit) -> {
                    target.wipe()
                    startedEmpty = true
                    burst()
                }
                else -> finish(OtpOutcome.BLOCKED)
            }
        }

        fun resume(start: Int) {
            from = start
            pos = start
            target.moveToEnd()
            burst()
        }

        fun finish(outcome: OtpOutcome, placed: Int = pos) {
            if (finished) return
            finished = true
            done(OtpResult(outcome, if (outcome == OtpOutcome.FILLED) want.length else placed, want.length))
        }

        fun burst() {
            val before = snap()
            val chunk = want.substring(pos)
            target.commit(chunk)
            await({ moved(before) || (placed(before) ?: 0) >= chunk.length }, BURST_WAIT) { judgeBurst(before, chunk) }
        }

        fun judgeBurst(before: Snap, chunk: String) {
            if (finished) return
            val got = placed(before)
            val jumped = moved(before)
            if (jumped) travelled = true
            when {
                got != null && got >= chunk.length -> finish(OtpOutcome.FILLED)
                jumped && distributed(snap()) -> finish(OtpOutcome.FILLED)
                jumped -> advance(1)
                got == null -> finish(OtpOutcome.FILLED)
                got == 1 && chunk.length > 1 && !patient -> linger(before, chunk)
                got > 0 -> truncated(got)
                else -> perChar()
            }
        }

        fun linger(before: Snap, chunk: String) {
            patient = true
            await({ moved(before) }, LATE_WAIT) { judgeBurst(before, chunk) }
        }

        fun distributed(now: Snap): Boolean {
            val text = now.text.orEmpty()
            return text.isNotEmpty() && want.endsWith(text, ignoreCase = true)
        }

        fun truncated(landed: Int) {
            if (startedEmpty) target.wipe() else target.erase(landed)
            finish(OtpOutcome.TOO_SHORT, 0)
        }

        fun advance(count: Int) {
            pos = (pos + count).coerceAtMost(want.length)
            if (pos >= want.length) return finish(OtpOutcome.FILLED)
            if (!target.alive() || !target.fits()) return finish(OtpOutcome.PARTIAL)
            perChar()
        }

        fun perChar() {
            if (finished) return
            if (pos >= want.length) return finish(OtpOutcome.FILLED)
            if (++steps > want.length + EXTRA_STEPS) return finish(OtpOutcome.PARTIAL)
            val before = snap()
            val hold = travelled && pos < want.length - 1
            target.commit(want[pos].toString())
            val ready = { moved(before) || (!hold && (placed(before) ?: 0) > 0) }
            await(ready, if (hold) LATE_WAIT else STEP_WAIT) { judgeStep(before) }
        }

        fun judgeStep(before: Snap) {
            if (finished) return
            val got = placed(before)
            val jumped = moved(before)
            if (jumped) travelled = true
            if (!(jumped || got == null || got > 0)) return rejected()
            pos += 1
            if (pos >= want.length) return finish(OtpOutcome.FILLED)
            if (!target.alive() || !target.fits()) return finish(OtpOutcome.PARTIAL)
            perChar()
        }

        fun rejected() {
            if (pos == from) return finish(OtpOutcome.BLOCKED)
            if (travelled) return finish(OtpOutcome.PARTIAL)
            if (startedEmpty) target.wipe() else target.erase(pos - from)
            finish(OtpOutcome.TOO_SHORT, 0)
        }

        fun await(ready: () -> Boolean, limit: Long, waited: Long = 0L, then: () -> Unit) {
            if (finished) return
            if (ready() || waited >= limit) return then()
            target.later(POLL_MS) { await(ready, limit, waited + POLL_MS, then) }
        }

        fun snap(): Snap {
            val raw = target.read()
            return Snap(target.epoch(), raw?.let(OtpText::flat), raw?.length, target.cursor())
        }

        fun moved(before: Snap): Boolean = target.epoch() != before.epoch

        fun placed(before: Snap): Int? {
            if (moved(before)) return null
            val raw = target.read()
            val cursor = target.cursor()
            val byText = textDelta(before, raw)
            val byCursor = if (before.cursor != null && cursor != null) cursor - before.cursor else null
            return listOfNotNull(byText, byCursor).maxOrNull()
        }

        fun textDelta(before: Snap, raw: String?): Int? {
            val beforeText = before.text ?: return null
            val beforeRaw = before.raw ?: return null
            val rawText = raw ?: return null
            val flatDelta = OtpText.flat(rawText).length - beforeText.length
            return if (flatDelta == 0) maxOf(0, rawText.length - beforeRaw) else flatDelta
        }
    }

    private companion object {
        const val POLL_MS = 30L
        const val BURST_WAIT = 450L
        const val STEP_WAIT = 400L
        const val LATE_WAIT = 1500L
        const val EXTRA_STEPS = 2
    }
}
