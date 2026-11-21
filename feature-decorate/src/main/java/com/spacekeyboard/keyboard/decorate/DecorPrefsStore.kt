package com.spacekeyboard.keyboard.decorate

import android.content.Context
import android.content.SharedPreferences

class DecorPrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun favorites(): Set<String> =
        prefs.getString(FAVORITES, null).orEmpty().split(',').filter { DecorCatalog.byId(it) != null }.toSet()

    fun toggleFavorite(id: String) {
        val next = favorites().let { if (id in it) it - id else it + id }
        prefs.edit().putString(FAVORITES, next.joinToString(",")).apply()
    }

    fun active(): String? = prefs.getString(ACTIVE, null)?.takeIf { DecorCatalog.byId(it) != null }

    fun setActive(id: String?) {
        prefs.edit().apply { if (id == null) remove(ACTIVE) else putString(ACTIVE, id) }.apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_keyboard_decorate"
        const val FAVORITES = "favorites"
        const val ACTIVE = "active"
    }
}
