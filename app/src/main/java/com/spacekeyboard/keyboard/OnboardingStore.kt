package com.spacekeyboard.keyboard

import android.content.Context

internal class OnboardingStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isDone(): Boolean = prefs.getBoolean(DONE, false)

    fun markDone() {
        prefs.edit().putBoolean(DONE, true).apply()
    }

    private companion object {
        const val NAME = "space_app"
        const val DONE = "onboarding_done"
    }
}
