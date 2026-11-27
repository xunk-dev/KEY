package com.spacekeyboard.keyboard.core.suggest

import android.content.Context
import android.content.SharedPreferences

class ScopedStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun load(): ScopedStats = ScopedStats.decode(prefs.getString(KEY, null).orEmpty())

    fun save(stats: ScopedStats) {
        prefs.edit().putString(KEY, stats.encode()).apply()
    }

    fun has(): Boolean = prefs.contains(KEY)

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_scoped_words"
        const val KEY = "scopes"
    }
}
