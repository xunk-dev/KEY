package com.spacekeyboard.keyboard.core.theme

import android.content.Context
import android.content.SharedPreferences

class ThemeStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun read(): ThemeChoice {
        val overrides = ThemeCatalog.all.mapNotNull { theme ->
            val custom = ThemeSlot.entries.mapNotNull { slot ->
                val key = overrideKey(theme.id, slot)
                if (prefs.contains(key)) slot to prefs.getInt(key, 0) else null
            }.toMap()
            if (custom.isEmpty()) null else theme.id to custom
        }.toMap()
        return ThemeChoice(
            themeId = ThemeCatalog.byId(prefs.getString(THEME, null)).id,
            overrides = overrides,
            animated = prefs.getBoolean(ANIMATED, false),
            followSystem = prefs.getBoolean(FOLLOW_SYSTEM, false),
        )
    }

    fun selectTheme(id: String) {
        prefs.edit().putString(THEME, ThemeCatalog.byId(id).id).apply()
    }

    fun setOverride(themeId: String, slot: ThemeSlot, argb: Int) {
        prefs.edit().putInt(overrideKey(themeId, slot), ColorMath.opaque(argb)).apply()
    }

    fun resetTheme(themeId: String) {
        val editor = prefs.edit()
        ThemeSlot.entries.forEach { editor.remove(overrideKey(themeId, it)) }
        editor.apply()
    }

    fun setAnimated(animated: Boolean) {
        prefs.edit().putBoolean(ANIMATED, animated).apply()
    }

    fun setFollowSystem(value: Boolean) {
        prefs.edit().putBoolean(FOLLOW_SYSTEM, value).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun overrideKey(themeId: String, slot: ThemeSlot) = "override.$themeId.${slot.key}"

    private companion object {
        const val NAME = "space_theme"
        const val THEME = "theme"
        const val ANIMATED = "animated"
        const val FOLLOW_SYSTEM = "follow_system"
    }
}
