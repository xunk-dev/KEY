package com.spacekeyboard.keyboard.symbols.data

import android.content.Context
import android.content.SharedPreferences
import com.spacekeyboard.keyboard.symbols.state.PanelTab

object RecentList {
    const val LIMIT = 40
    private const val SEPARATOR = "\u0001"

    fun record(current: List<String>, text: String): List<String> =
        if (text.isBlank()) current else (listOf(text) + current.filter { it != text }).take(LIMIT)

    fun remove(current: List<String>, text: String): List<String> = current.filter { it != text }

    fun encode(items: List<String>): String = items.joinToString(SEPARATOR)

    fun decode(raw: String?): List<String> =
        raw.orEmpty().split(SEPARATOR).filter { it.isNotEmpty() }.distinct().take(LIMIT)
}

class SymbolRecents(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences("space_keyboard_symbol_recents", Context.MODE_PRIVATE),
    )

    fun list(tab: PanelTab): List<String> = RecentList.decode(prefs.getString(tab.name, null))

    fun record(tab: PanelTab, text: String) = save(tab, RecentList.record(list(tab), text))

    fun remove(tab: PanelTab, text: String) = save(tab, RecentList.remove(list(tab), text))

    fun clear(tab: PanelTab) {
        prefs.edit().remove(tab.name).apply()
    }

    private fun save(tab: PanelTab, items: List<String>) {
        prefs.edit().putString(tab.name, RecentList.encode(items)).apply()
    }
}
