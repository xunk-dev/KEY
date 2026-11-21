package com.spacekeyboard.keyboard.clipboard

data class ClipItem(
    val id: Long,
    val text: String,
    val pinned: Boolean,
    val section: String?,
    val copiedAt: Long,
)

data class Section(val name: String, val locked: Boolean)

data class ClipBook(
    val items: List<ClipItem> = emptyList(),
    val sections: List<Section> = emptyList(),
    val nextId: Long = 1,
) {
    val current: List<ClipItem> get() = items.filter { !it.pinned && it.section == null }.sortedByDescending { it.copiedAt }

    val pinned: List<ClipItem> get() = items.filter { it.pinned }.sortedByDescending { it.copiedAt }

    fun saved(section: String): List<ClipItem> =
        items.filter { it.section == section }.sortedByDescending { it.copiedAt }

    fun add(text: String, now: Long): ClipBook {
        if (text.isBlank()) return this
        val existing = items.firstOrNull { it.text == text }
        val next = if (existing != null) {
            replace(existing.copy(copiedAt = now))
        } else {
            copy(items = items + ClipItem(nextId, text, false, null, now), nextId = nextId + 1)
        }
        return next.prune(now)
    }

    fun prune(now: Long): ClipBook {
        val fresh = items.filter { it.pinned || it.section != null || now - it.copiedAt < ClipPolicy.TTL_MS }
        val keep = fresh.filter { !it.pinned && it.section == null }
            .sortedByDescending { it.copiedAt }.take(ClipPolicy.LIMIT).map { it.id }.toSet()
        return copy(items = fresh.filter { it.pinned || it.section != null || it.id in keep })
    }

    fun setPinned(id: Long, pinned: Boolean): ClipBook = update(id) { it.copy(pinned = pinned) }

    fun delete(id: Long): ClipBook = copy(items = items.filter { it.id != id })

    fun clearCurrent(): ClipBook = copy(items = items.filter { it.pinned || it.section != null })

    fun addSection(name: String): ClipBook {
        val clean = name.trim()
        if (clean.isEmpty() || sections.any { it.name == clean }) return this
        return copy(sections = sections + Section(clean, false))
    }

    fun setSectionLocked(name: String, locked: Boolean): ClipBook =
        copy(sections = sections.map { if (it.name == name) it.copy(locked = locked) else it })

    fun deleteSection(name: String): ClipBook =
        copy(sections = sections.filter { it.name != name }, items = items.filter { it.section != name })

    fun moveToSection(id: Long, name: String?): ClipBook {
        if (name != null && sections.none { it.name == name }) return this
        return update(id) { it.copy(section = name) }
    }

    private fun update(id: Long, change: (ClipItem) -> ClipItem): ClipBook =
        copy(items = items.map { if (it.id == id) change(it) else it })

    private fun replace(item: ClipItem): ClipBook = update(item.id) { item }
}

object ClipPolicy {
    const val LIMIT = 50
    const val TTL_MS = 24L * 60 * 60 * 1000
    const val MAX_TEXT = 5000
    const val MAX_SECTIONS = 20
    const val MAX_NAME = 24
}
