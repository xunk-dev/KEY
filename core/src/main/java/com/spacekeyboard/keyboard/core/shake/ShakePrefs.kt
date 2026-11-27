package com.spacekeyboard.keyboard.core.shake

import android.content.Context
import android.content.SharedPreferences

data class ShakePrefs(
    val enabled: Boolean = false,
    val action: ShakeAction = ShakeAction.CALCULATOR,
    val sensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
)

class ShakePrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun read(): ShakePrefs = ShakePrefs(
        enabled = prefs.getBoolean(ENABLED, false),
        action = ShakeAction.byId(prefs.getString(ACTION, null)),
        sensitivity = ShakeSensitivity.byId(prefs.getString(SENSITIVITY, null)),
    )

    fun setEnabled(value: Boolean) = save { putBoolean(ENABLED, value) }

    fun setAction(action: ShakeAction) = save { putString(ACTION, action.id) }

    fun setSensitivity(level: ShakeSensitivity) = save { putString(SENSITIVITY, level.id) }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun save(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    private companion object {
        const val NAME = "space_keyboard_shake"
        const val ENABLED = "enabled"
        const val ACTION = "action"
        const val SENSITIVITY = "sensitivity"
    }
}
