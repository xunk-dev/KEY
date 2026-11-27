package com.spacekeyboard.keyboard.translate

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object TranslateReport {
    const val SUPPORT_EMAIL = "xunk.app@gmail.com"

    fun open(context: Context, subject: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (error: ActivityNotFoundException) {
            false
        }
    }
}
