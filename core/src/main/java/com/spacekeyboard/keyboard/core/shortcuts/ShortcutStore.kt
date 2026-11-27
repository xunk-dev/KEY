package com.spacekeyboard.keyboard.core.shortcuts

import android.content.Context
import android.content.SharedPreferences

enum class ShortcutCheck { OK, NO_TRIGGER, TRIGGER_TOO_LONG, NO_TEXT, TEXT_TOO_LONG, FULL }

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
        if (check(trigger, text) != ShortcutCheck.OK) return false
        val all = LinkedHashMap(read())
        all[cleanTrigger(trigger)] = cleanText(text)
        save(all)
        return true
    }

    fun check(trigger: String, text: String): ShortcutCheck {
        val key = cleanTrigger(trigger)
        val body = cleanText(text)
        return when {
            key.isEmpty() -> ShortcutCheck.NO_TRIGGER
            key.length > MAX_TRIGGER -> ShortcutCheck.TRIGGER_TOO_LONG
            body.isBlank() -> ShortcutCheck.NO_TEXT
            body.length > MAX_TEXT -> ShortcutCheck.TEXT_TOO_LONG
            key !in read() && read().size >= MAX_ENTRIES -> ShortcutCheck.FULL
            else -> ShortcutCheck.OK
        }
    }

    fun has(trigger: String): Boolean = cleanTrigger(trigger) in read()

    fun triggerFor(text: String): String? = read().entries.firstOrNull { it.value == cleanText(text) }?.key

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

    fun cleanTrigger(trigger: String): String =
        trigger.trim().filterNot { it.isWhitespace() || it == RECORD_CHAR || it == FIELD_CHAR }

    private fun cleanText(text: String): String = text.filterNot { it == RECORD_CHAR || it == FIELD_CHAR }

    private fun save(all: Map<String, String>) {
        val raw = all.entries.joinToString(RECORD) { it.key + FIELD + it.value }
        prefs.edit().putString(ENTRIES, raw).apply()
    }

    companion object {
        const val MAX_TRIGGER = 24
        const val MAX_TEXT = 2000
        const val MAX_ENTRIES = 100
        private const val NAME = "space_shortcuts"
        private const val ENTRIES = "entries"
        private const val RECORD_CHAR = '\u001E'
        private const val FIELD_CHAR = '\u001F'
        private val RECORD = RECORD_CHAR.toString()
        private val FIELD = FIELD_CHAR.toString()
    }
}
