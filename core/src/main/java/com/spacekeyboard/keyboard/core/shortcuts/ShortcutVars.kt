package com.spacekeyboard.keyboard.core.shortcuts

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShortcutVars {
    val NAMES = listOf("date", "time", "day", "clipboard")

    private val TOKEN = Regex("""\{(date|time|day|clipboard)\}""", RegexOption.IGNORE_CASE)

    fun has(text: String): Boolean = '{' in text && TOKEN.containsMatchIn(text)

    fun expand(text: String, now: Date, locale: Locale, clipboard: () -> String): String {
        if (!has(text)) return text
        val pasted by lazy(clipboard)
        return TOKEN.replace(text) { match ->
            when (match.groupValues[1].lowercase()) {
                "date" -> DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(now)
                "time" -> DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(now)
                "day" -> SimpleDateFormat("EEEE", locale).format(now)
                else -> pasted
            }
        }
    }
}
