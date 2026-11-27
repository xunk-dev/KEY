package com.spacekeyboard.keyboard.core.backup

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PrefsBackup(
    private val open: (String) -> SharedPreferences,
    private val readable: (String, SharedPreferences) -> Boolean = BackupChecks::passes,
) {
    constructor(context: Context) : this({ name ->
        context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    })

    fun export(includeWords: Boolean): String {
        val files = JSONObject()
        names(includeWords).forEach { name -> files.put(name, dump(open(name))) }
        return JSONObject().put(VERSION_KEY, VERSION).put(FILES_KEY, files).toString(2)
    }

    fun import(text: String): Result {
        if (text.length > MAX_CHARS) return Result.TOO_LARGE
        val root = runCatching { JSONObject(text) }.getOrNull() ?: return Result.INVALID
        if (root.optInt(VERSION_KEY, -1) != VERSION) return Result.INVALID
        val files = root.optJSONObject(FILES_KEY) ?: return Result.INVALID
        val incoming = ALL.filter { files.optJSONObject(it) != null }
        if (incoming.isEmpty()) return Result.INVALID
        val before = incoming.associateWith { dump(open(it)) }
        val ok = incoming.all { name -> restore(open(name), files.getJSONObject(name)) && readable(name, open(name)) }
        if (ok) return Result.OK
        before.forEach { (name, entries) -> restore(open(name), entries) }
        return Result.INVALID
    }

    private fun names(includeWords: Boolean): List<String> = if (includeWords) ALL else ALL - WORDS

    private fun dump(prefs: SharedPreferences): JSONObject {
        val out = JSONObject()
        prefs.all.forEach { (key, value) ->
            when (value) {
                is Boolean -> out.put(key, JSONObject().put(TYPE, BOOL).put(VALUE, value))
                is Int -> out.put(key, JSONObject().put(TYPE, INT).put(VALUE, value))
                is Long -> out.put(key, JSONObject().put(TYPE, LONG).put(VALUE, value))
                is Float -> out.put(key, JSONObject().put(TYPE, FLOAT).put(VALUE, value.toDouble()))
                is String -> out.put(key, JSONObject().put(TYPE, STRING).put(VALUE, value))
                is Set<*> -> out.put(key, JSONObject().put(TYPE, SET).put(VALUE, JSONArray(value.filterIsInstance<String>())))
            }
        }
        return out
    }

    private fun restore(prefs: SharedPreferences, entries: JSONObject): Boolean {
        if (entries.length() > MAX_ENTRIES) return false
        val editor = prefs.edit().clear()
        val keys = entries.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val node = entries.optJSONObject(key) ?: continue
            if (key.isEmpty() || key.length > MAX_KEY || !write(editor, key, node)) return false
        }
        editor.apply()
        return true
    }

    private fun write(editor: SharedPreferences.Editor, key: String, node: JSONObject): Boolean {
        when (node.optString(TYPE)) {
            BOOL -> editor.putBoolean(key, node.getBoolean(VALUE))
            INT -> editor.putInt(key, node.getInt(VALUE))
            LONG -> editor.putLong(key, node.getLong(VALUE))
            FLOAT -> editor.putFloat(key, node.getDouble(VALUE).toFloat())
            STRING -> editor.putString(key, node.getString(VALUE).takeIf { it.length <= MAX_STRING } ?: return false)
            SET -> editor.putStringSet(key, strings(node.getJSONArray(VALUE)) ?: return false)
            else -> return false
        }
        return true
    }

    private fun strings(array: JSONArray): Set<String>? {
        if (array.length() > MAX_ENTRIES) return null
        return (0 until array.length()).map { array.getString(it) }.toSet()
    }

    enum class Result { OK, INVALID, TOO_LARGE }

    companion object {
        const val THEME = "space_theme"
        const val KEYBOARD = "space_keyboard"
        const val SHORTCUTS = "space_shortcuts"
        const val APPS = "space_app_languages"
        const val WORDS = "space_words"
        const val AUTOFIX = "space_keyboard_autofix"
        const val EMOJI = "space_keyboard_emoji"
        const val DECORATE = "space_keyboard_decorate"
        val ALL = listOf(THEME, KEYBOARD, SHORTCUTS, APPS, AUTOFIX, EMOJI, DECORATE, WORDS)
        private const val VERSION = 1
        private const val VERSION_KEY = "version"
        private const val FILES_KEY = "files"
        private const val TYPE = "t"
        private const val VALUE = "v"
        private const val BOOL = "b"
        private const val INT = "i"
        private const val LONG = "l"
        private const val FLOAT = "f"
        private const val STRING = "s"
        private const val SET = "a"
        private const val MAX_CHARS = 2_000_000
        private const val MAX_ENTRIES = 2_000
        private const val MAX_KEY = 120
        private const val MAX_STRING = 400_000
    }
}
