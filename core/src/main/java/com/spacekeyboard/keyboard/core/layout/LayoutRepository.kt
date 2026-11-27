package com.spacekeyboard.keyboard.core.layout

import android.content.Context

class LayoutRepository(context: Context) {
    private val resources = context.applicationContext.resources
    private val cache = HashMap<String, LayoutSpec>()

    @Synchronized
    fun base(id: String, style: ArabicStyle = ArabicStyle.ELEVEN): LayoutSpec {
        val entry = LayoutCatalog.all.firstOrNull { it.id == id } ?: LayoutCatalog.entries.first()
        val arabic = entry.id == LayoutCatalog.default
        val raw = if (arabic) style.rawRes else entry.rawRes
        return cache.getOrPut(if (arabic) "${entry.id}/${style.id}" else entry.id) {
            runCatching { LayoutParser.parse(read(raw)) }.getOrElse { LayoutSpec.fallback(entry.id) }
        }
    }

    fun arranged(id: String, order: List<List<String>>?, style: ArabicStyle = ArabicStyle.ELEVEN): LayoutSpec {
        val base = base(id, style)
        return order?.let { base.arranged(it) } ?: base
    }

    private fun read(rawRes: Int): String =
        resources.openRawResource(rawRes).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
