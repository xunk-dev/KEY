package com.spacekeyboard.keyboard.core.layout

import android.content.Context

class LayoutRepository(context: Context) {
    private val resources = context.applicationContext.resources
    private val cache = HashMap<String, LayoutSpec>()

    @Synchronized
    fun base(id: String): LayoutSpec = cache.getOrPut(id) {
        val entry = LayoutCatalog.all.firstOrNull { it.id == id } ?: LayoutCatalog.entries.first()
        runCatching { LayoutParser.parse(read(entry.rawRes)) }.getOrElse { LayoutSpec.fallback(entry.id) }
    }

    fun arranged(id: String, order: List<List<String>>?): LayoutSpec {
        val base = base(id)
        return order?.let { base.arranged(it) } ?: base
    }

    private fun read(rawRes: Int): String =
        resources.openRawResource(rawRes).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
