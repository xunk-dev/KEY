package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.content.Intent

object SettingsLink {
    const val EXTRA_OPEN_CUSTOMIZE = "com.spacekeyboard.keyboard.OPEN_CUSTOMIZE"

    fun open(context: Context): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_OPEN_CUSTOMIZE, true)
        context.startActivity(launch)
        return true
    }
}
