package com.spacekeyboard.keyboard.clipboard

import android.content.Context
import android.content.SharedPreferences

class ClipPrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun enabled(): Boolean = prefs.getBoolean(ENABLED, false)

    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(ENABLED, value).apply()
    }

    fun window(): UnlockWindow = UnlockWindow.byId(prefs.getString(WINDOW, null))

    fun setWindow(window: UnlockWindow) {
        prefs.edit().putString(WINDOW, window.id).apply()
    }

    fun wipeRequested(): Boolean = prefs.getBoolean(WIPE, false)

    fun requestWipe() {
        prefs.edit().putBoolean(WIPE, true).apply()
    }

    fun clearWipeRequest() {
        prefs.edit().remove(WIPE).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_keyboard_clipboard"
        const val ENABLED = "enabled"
        const val WINDOW = "window"
        const val WIPE = "wipe"
    }
}
