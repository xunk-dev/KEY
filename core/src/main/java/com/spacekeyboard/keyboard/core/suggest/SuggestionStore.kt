package com.spacekeyboard.keyboard.core.suggest

import android.content.Context
import android.content.SharedPreferences
import com.spacekeyboard.keyboard.core.R

class SuggestionStore(private val prefs: SharedPreferences, private val context: Context? = null) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
        context.applicationContext,
    )

    fun load(): WordStats = WordStats.decode(prefs.getString(KEY, null).orEmpty())

    fun save(stats: WordStats) {
        prefs.edit().putString(KEY, stats.encode()).apply()
    }

    fun has(): Boolean = prefs.contains(KEY)

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    fun seed(): List<String> {
        val resources = context?.resources ?: return emptyList()
        return listOf(R.raw.words_ar, R.raw.words_en).flatMap { id ->
            resources.openRawResource(id).bufferedReader(Charsets.UTF_8).use { it.readLines() }
        }.map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun seedPairs(): Map<String, List<String>> {
        val resources = context?.resources ?: return emptyMap()
        val result = LinkedHashMap<String, List<String>>()
        listOf(R.raw.pairs_ar, R.raw.pairs_en).forEach { id ->
            resources.openRawResource(id).bufferedReader(Charsets.UTF_8).useLines { lines ->
                lines.map { it.trim().split(WHITESPACE) }.filter { it.size >= 2 }.forEach { parts ->
                    result[parts[0]] = parts.drop(1)
                }
            }
        }
        return result
    }

    fun restoreBlocked() {
        if (!has()) return
        val stats = load()
        if (stats.blocked.isEmpty()) return
        stats.restoreBlocked()
        save(stats)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val NAME = "space_words"
        const val KEY = "stats"
    }
}
