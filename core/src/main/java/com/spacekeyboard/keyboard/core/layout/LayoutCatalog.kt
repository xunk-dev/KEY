package com.spacekeyboard.keyboard.core.layout

import com.spacekeyboard.keyboard.core.R

data class LayoutEntry(val id: String, val rawRes: Int, val nameRes: Int, val short: String = "", val title: String = "")

object LayoutCatalog {
    val entries: List<LayoutEntry> = listOf(
        LayoutEntry("ar", R.raw.layout_ar, R.string.layout_ar, "\u0639", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"),
        LayoutEntry("en", R.raw.layout_en, R.string.layout_en, "EN", "English"),
        LayoutEntry("fa", R.raw.layout_fa, R.string.layout_fa, "\u0641\u0627", "\u0641\u0627\u0631\u0633\u06CC"),
        LayoutEntry("az", R.raw.layout_az, R.string.layout_az, "Az", "Arabizi"),
    )

    val numbers = LayoutEntry("num", R.raw.layout_num, R.string.layout_num)

    val edit = LayoutEntry("edit", R.raw.layout_edit, R.string.layout_edit)

    val all: List<LayoutEntry> = entries + numbers + edit

    val ids: List<String> = entries.map { it.id }

    const val default = "ar"

    val defaultLanguages: List<String> = listOf("ar", "en")

    val optional: List<String> = listOf("fa", "az")

    private const val LAYOUT_TAG = "layout="

    private val LOCALE_LANGUAGES = listOf("ar", "en", "fa")

    fun next(id: String, enabled: List<String> = defaultLanguages): String {
        val cycle = enabled.filter { it in ids }.ifEmpty { defaultLanguages }
        return cycle[(cycle.indexOf(id) + 1).mod(cycle.size)]
    }

    fun short(id: String): String = entries.firstOrNull { it.id == id }?.short.orEmpty()

    fun title(id: String): String = entries.firstOrNull { it.id == id }?.title.orEmpty()

    fun forSubtype(locale: String?, extra: String?): String? {
        val tagged = extra.orEmpty().split(',').map { it.trim() }.firstOrNull { it.startsWith(LAYOUT_TAG) }
        val id = tagged?.removePrefix(LAYOUT_TAG)
        if (id != null && id in ids) return id
        return locale?.let(::forLocale)
    }

    fun forLocale(locale: String): String? {
        val code = locale.lowercase().substringBefore('_').substringBefore('-')
        return LOCALE_LANGUAGES.firstOrNull { it == code }
    }
}
