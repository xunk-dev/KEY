package com.spacekeyboard.keyboard.ime

import android.app.Notification
import android.os.Bundle
import com.spacekeyboard.keyboard.core.otp.OtpExtractor
import com.spacekeyboard.keyboard.core.otp.OtpMatch

internal object OtpNotificationText {
    private const val MESSAGE_TEXT = "text"
    private const val MAX_LINES = 3

    fun match(notification: Notification): OtpMatch? {
        val extras = notification.extras ?: return null
        val title = listOf(Notification.EXTRA_TITLE_BIG, Notification.EXTRA_TITLE)
            .firstNotNullOfOrNull { text(extras, it) }.orEmpty()
        return pieces(extras, notification.tickerText?.toString())
            .firstNotNullOfOrNull { OtpExtractor.extract("$title\n$it")?.takeUnless { found -> found.bare } }
    }

    private fun pieces(extras: Bundle, ticker: String?): List<String> {
        val body = listOf(Notification.EXTRA_BIG_TEXT, Notification.EXTRA_TEXT).mapNotNull { text(extras, it) }
        val minor = listOf(Notification.EXTRA_SUB_TEXT, Notification.EXTRA_SUMMARY_TEXT, Notification.EXTRA_INFO_TEXT)
            .mapNotNull { text(extras, it) }
        return (messages(extras) + body + lines(extras) + minor + listOfNotNull(ticker)).filter { it.isNotBlank() }
    }

    private fun text(extras: Bundle, key: String): String? = extras.getCharSequence(key)?.toString()

    @Suppress("DEPRECATION")
    private fun messages(extras: Bundle): List<String> =
        extras.getParcelableArray(Notification.EXTRA_MESSAGES).orEmpty()
            .mapNotNull { (it as? Bundle)?.getCharSequence(MESSAGE_TEXT)?.toString() }
            .takeLast(1)

    private fun lines(extras: Bundle): List<String> =
        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES).orEmpty().map { it.toString() }.takeLast(MAX_LINES).reversed()
}
