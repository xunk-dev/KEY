package com.spacekeyboard.keyboard.ime

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.otp.NotificationFacts
import com.spacekeyboard.keyboard.core.otp.OtpInbox
import com.spacekeyboard.keyboard.core.otp.OtpNotificationGate
import com.spacekeyboard.keyboard.core.otp.OtpSource
import com.spacekeyboard.keyboard.core.otp.toCode

class OtpNotificationService : NotificationListenerService() {
    private val store by lazy { KeyboardPrefsStore(this) }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val posted = sbn ?: return
        val now = System.currentTimeMillis()
        val facts = NotificationFacts(posted.packageName, packageName, posted.isOngoing, posted.postTime)
        if (!OtpNotificationGate.accepts(store.otpNotificationsOn(), facts, now)) return
        val match = runCatching { OtpNotificationText.match(posted.notification) }.getOrNull() ?: return
        OtpInbox.shared.offer(match.toCode(OtpSource.NOTIFICATION, OtpNotificationGate.receivedAt(facts, now)))
    }
}
