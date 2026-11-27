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

    fun smart(): Boolean = prefs.getBoolean(SMART, false)

    fun setSmart(value: Boolean) {
        prefs.edit().putBoolean(SMART, value).apply()
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

    fun tabOrder(): List<ClipTab> = ClipTabOrder.parse(prefs.getString(TAB_ORDER, null))

    fun setTabOrder(order: List<ClipTab>) {
        prefs.edit().putString(TAB_ORDER, ClipTabOrder.encode(order)).putBoolean(TAB_HINT, true).apply()
    }

    fun resetTabOrder() {
        prefs.edit().remove(TAB_ORDER).apply()
    }

    fun tabHintShown(): Boolean = prefs.getBoolean(TAB_HINT, false)

    fun markTabHintShown() {
        prefs.edit().putBoolean(TAB_HINT, true).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_keyboard_clipboard"
        const val ENABLED = "enabled"
        const val SMART = "smart_sort"
        const val WINDOW = "window"
        const val WIPE = "wipe"
        const val TAB_ORDER = "tab_order"
        const val TAB_HINT = "tab_hint"
    }
}
