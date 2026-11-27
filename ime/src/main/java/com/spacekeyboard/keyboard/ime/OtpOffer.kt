package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.otp.OtpCode
import com.spacekeyboard.keyboard.core.otp.OtpFiller
import com.spacekeyboard.keyboard.core.otp.OtpFit
import com.spacekeyboard.keyboard.core.otp.OtpGate
import com.spacekeyboard.keyboard.core.otp.OtpInbox
import com.spacekeyboard.keyboard.core.otp.OtpOutcome
import com.spacekeyboard.keyboard.core.otp.OtpResult
import com.spacekeyboard.keyboard.core.otp.OtpTarget

class OtpOffer(
    private val context: Context,
    private val extras: TypingExtras,
    private val settle: () -> Unit,
    private val connection: () -> InputConnection?,
    private val target: () -> OtpTarget,
    private val inbox: OtpInbox = OtpInbox.shared,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val source = OtpClipSource(context, inbox, now)
    private val handler = Handler(Looper.getMainLooper())
    private val tick = Runnable { refresh() }
    private var subscription: AutoCloseable? = null
    private var busySince = 0L

    var code by mutableStateOf<OtpCode?>(null)
        private set

    fun begin() {
        if (!extras.otpOffers) return stop()
        if (subscription == null) subscription = inbox.subscribe { handler.post(::refresh) }
        source.start()
        source.poll()
        refresh()
    }

    fun stop() {
        handler.removeCallbacks(tick)
        subscription?.close()
        subscription = null
        source.stop()
        code = null
    }

    fun refresh() {
        handler.removeCallbacks(tick)
        val fit = extras.otpFit
        val held = if (extras.otpOffers && fit != OtpFit.NONE) inbox.current() else null
        val shown = held?.takeIf { OtpGate.allows(fit, it, now(), existing()) }
        code = shown
        if (held != null && shown != null) handler.postDelayed(tick, OtpGate.nextChange(fit, held) - now() + GRACE_MS)
    }

    fun use() {
        val chosen = code ?: return
        if (busy()) return
        busySince = now()
        settle()
        OtpFiller(target(), extras.otpFit == OtpFit.CERTAIN).fill(chosen.value) { finished(chosen, it) }
    }

    fun dismiss() {
        code?.let(inbox::dismiss)
        refresh()
    }

    private fun busy(): Boolean = busySince != 0L && now() - busySince < BUSY_MAX_MS

    private fun finished(chosen: OtpCode, result: OtpResult) {
        busySince = 0L
        when (result.outcome) {
            OtpOutcome.FILLED -> inbox.consume(chosen)
            OtpOutcome.PARTIAL -> toast(R.string.otp_partial)
            OtpOutcome.TOO_SHORT -> toast(R.string.otp_too_short)
            OtpOutcome.BLOCKED -> toast(R.string.otp_blocked)
            OtpOutcome.LOST -> Unit
        }
        refresh()
    }

    private fun existing(): String = connection()?.let(ImeOtpTarget::fieldText).orEmpty()

    private fun toast(message: Int) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    private companion object {
        const val GRACE_MS = 50L
        const val BUSY_MAX_MS = 15_000L
    }
}
