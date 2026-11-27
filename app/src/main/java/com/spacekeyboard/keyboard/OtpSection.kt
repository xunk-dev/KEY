package com.spacekeyboard.keyboard

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.layout.TypingPrefs
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.ime.OtpNotificationService

@Composable
internal fun OtpSettings(typing: TypingPrefs) {
    val context = LocalContext.current
    SettingsCard {
        SwitchRow(stringResource(R.string.typing_otp_offers), typing.otpOffers) { KeyboardPrefsStore(context).setOtpOffers(it) }
        SwitchRow(stringResource(R.string.typing_otp_notifications), typing.otpNotifications) { KeyboardPrefsStore(context).setOtpNotifications(it) }
    }
    BodyText(R.string.typing_otp_offers_hint)
    BodyText(R.string.typing_otp_notifications_hint)
    SpaceButton(stringResource(R.string.typing_otp_access_open), { openNotificationAccess(context) })
}

private fun openNotificationAccess(context: Context) {
    val detail = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
        .putExtra(
            Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
            ComponentName(context, OtpNotificationService::class.java).flattenToString(),
        )
    val general = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    val first = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) detail else general
    try {
        context.startActivity(first.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        runCatching { context.startActivity(general.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
