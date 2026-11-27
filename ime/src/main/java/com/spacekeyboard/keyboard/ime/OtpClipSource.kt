package com.spacekeyboard.keyboard.ime

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.spacekeyboard.keyboard.core.otp.OtpExtractor
import com.spacekeyboard.keyboard.core.otp.OtpInbox
import com.spacekeyboard.keyboard.core.otp.OtpSource
import com.spacekeyboard.keyboard.core.otp.toCode

internal class OtpClipSource(
    context: Context,
    private val inbox: OtpInbox,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val manager = context.getSystemService(ClipboardManager::class.java)
    private val listener = ClipboardManager.OnPrimaryClipChangedListener { poll() }
    private var listening = false

    fun start() {
        if (listening) return
        listening = true
        manager?.addPrimaryClipChangedListener(listener)
    }

    fun stop() {
        if (!listening) return
        listening = false
        manager?.removePrimaryClipChangedListener(listener)
    }

    fun poll() {
        val clip = runCatching { manager?.primaryClip }.getOrNull() ?: return
        val description = clip.description
        val sensitive = description.extras?.getBoolean(SENSITIVE, false) == true
        if (sensitive || clip.itemCount == 0 || description.filterMimeTypes(TEXT) == null) return
        val text = clip.getItemAt(0).text?.toString() ?: return
        val match = OtpExtractor.extract(text) ?: return
        inbox.offer(match.toCode(OtpSource.CLIPBOARD, copiedAt(description)))
    }

    private fun copiedAt(description: ClipDescription): Long {
        val current = now()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return current
        val stamp = description.timestamp
        return if (stamp > 0L) minOf(stamp, current) else current
    }

    private companion object {
        const val TEXT = "text/*"
        const val SENSITIVE = "android.content.extra.IS_SENSITIVE"
    }
}
