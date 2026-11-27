package com.spacekeyboard.keyboard.autofix

import android.content.Context
import android.content.SharedPreferences

class AutoFixPrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun config(): AutoFixConfig = AutoFixConfig(
        enabled = prefs.getBoolean(ENABLED, true),
        level = AutoFixLevel.byId(prefs.getString(LEVEL, null)),
        live = prefs.getBoolean(LIVE, false),
        words = prefs.getBoolean(WORDS, true),
    )

    fun setEnabled(value: Boolean) = save { putBoolean(ENABLED, value) }

    fun setLevel(level: AutoFixLevel) = save { putString(LEVEL, level.id) }

    fun setLive(value: Boolean) = save { putBoolean(LIVE, value) }

    fun setWords(value: Boolean) = save { putBoolean(WORDS, value) }

    fun epoch(): Int = prefs.getInt(EPOCH, 0)

    fun bumpEpoch() = save { putInt(EPOCH, epoch() + 1) }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun save(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    private companion object {
        const val NAME = "space_keyboard_autofix"
        const val ENABLED = "enabled"
        const val LEVEL = "level"
        const val LIVE = "live"
        const val WORDS = "words"
        const val EPOCH = "epoch"
    }
}
