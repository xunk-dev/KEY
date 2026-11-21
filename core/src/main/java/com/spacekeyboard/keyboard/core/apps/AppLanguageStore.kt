package com.spacekeyboard.keyboard.core.apps

import android.content.Context
import android.content.SharedPreferences

class AppLanguageStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun get(packageName: String): String? = read()[packageName]

    fun put(packageName: String, language: String) {
        if (packageName.isEmpty() || packageName.length > MAX_NAME || language.isEmpty()) return
        val all = LinkedHashMap(read())
        all.remove(packageName)
        all[packageName] = language
        while (all.size > MAX_ENTRIES) all.remove(all.keys.first())
        save(all)
    }

    fun clear() {
        prefs.edit().remove(ENTRIES).apply()
    }

    fun size(): Int = read().size

    private fun read(): Map<String, String> {
        val raw = prefs.getString(ENTRIES, null).orEmpty()
        if (raw.isEmpty()) return emptyMap()
        val result = LinkedHashMap<String, String>()
        raw.split(RECORD).forEach { record ->
            val cut = record.indexOf(FIELD)
            if (cut > 0 && cut < record.length - 1) result[record.substring(0, cut)] = record.substring(cut + 1)
        }
        return result
    }

    private fun save(all: Map<String, String>) {
        prefs.edit().putString(ENTRIES, all.entries.joinToString(RECORD) { it.key + FIELD + it.value }).apply()
    }

    companion object {
        const val MAX_ENTRIES = 200
        private const val MAX_NAME = 160
        private const val NAME = "space_app_languages"
        private const val ENTRIES = "entries"
        private const val RECORD = "\n"
        private const val FIELD = "="
    }
}
