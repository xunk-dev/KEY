package com.spacekeyboard.keyboard.core.shortcuts

import android.content.Context
import android.content.SharedPreferences

class ShortcutStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun read(): Map<String, String> {
        val raw = prefs.getString(ENTRIES, null).orEmpty()
        if (raw.isEmpty()) return emptyMap()
        val result = LinkedHashMap<String, String>()
        raw.split(RECORD).forEach { record ->
            val cut = record.indexOf(FIELD)
            if (cut > 0) result[record.substring(0, cut)] = record.substring(cut + 1)
        }
        return result
    }

    fun book(): ShortcutBook = ShortcutBook(read())

    fun add(trigger: String, text: String): Boolean {
        val key = clean(trigger)
        val body = text.filterNot { it == RECORD_CHAR || it == FIELD_CHAR }
        if (key.isEmpty() || key.length > MAX_TRIGGER || body.isBlank() || body.length > MAX_TEXT) return false
        val all = LinkedHashMap(read())
        if (key !in all && all.size >= MAX_ENTRIES) return false
        all[key] = body
        save(all)
        return true
    }

    fun remove(trigger: String) {
        val all = LinkedHashMap(read())
        if (all.remove(trigger) != null) save(all)
    }

    fun clear() {
        prefs.edit().remove(ENTRIES).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun clean(trigger: String): String =
        trigger.trim().filterNot { it.isWhitespace() || it == RECORD_CHAR || it == FIELD_CHAR }

    private fun save(all: Map<String, String>) {
        val raw = all.entries.joinToString(RECORD) { it.key + FIELD + it.value }
        prefs.edit().putString(ENTRIES, raw).apply()
    }

    companion object {
        const val MAX_TRIGGER = 24
        const val MAX_TEXT = 500
        const val MAX_ENTRIES = 100
        private const val NAME = "space_shortcuts"
        private const val ENTRIES = "entries"
        private const val RECORD_CHAR = '\u001E'
        private const val FIELD_CHAR = '\u001F'
        private val RECORD = RECORD_CHAR.toString()
        private val FIELD = FIELD_CHAR.toString()
    }
}
