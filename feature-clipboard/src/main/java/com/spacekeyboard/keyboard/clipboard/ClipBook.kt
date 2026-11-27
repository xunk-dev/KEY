package com.spacekeyboard.keyboard.clipboard

data class ClipItem(
    val id: Long,
    val text: String,
    val pinned: Boolean,
    val section: String?,
    val copiedAt: Long,
    val blob: String? = null,
    val total: Long = 0,
    val digest: String = "",
    val title: String? = null,
) {
    val length: Long get() = if (blob == null) text.length.toLong() else total

    val kinds: List<ClipKind> by lazy {
        if (blob == null) ClipClassifier.kinds(text) else listOf(ClipKind.TEXT)
    }

    val primaryKind: ClipKind get() = kinds.first()
}

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
        val existing = items.firstOrNull { it.blob == null && it.text == text }
        val next = if (existing != null) {
            replace(existing.copy(copiedAt = now))
        } else {
            copy(items = items + ClipItem(nextId, text, false, null, now), nextId = nextId + 1)
        }
        return next.prune(now)
    }

    fun addLarge(preview: String, total: Long, blob: String, digest: String, now: Long): ClipBook {
        val twin = items.firstOrNull { it.blob != null && it.total == total && it.digest == digest }
        val next = if (twin != null) {
            replace(twin.copy(copiedAt = now))
        } else {
            copy(items = items + ClipItem(nextId, preview, false, null, now, blob, total, digest), nextId = nextId + 1)
        }
        return next.prune(now)
    }

    fun prune(now: Long): ClipBook {
        val fresh = items.filter { it.pinned || it.section != null || now - it.copiedAt < ClipPolicy.TTL_MS }
        val keep = fresh.filter { !it.pinned && it.section == null }
            .sortedByDescending { it.copiedAt }.take(ClipPolicy.LIMIT).map { it.id }.toSet()
        return copy(items = withinBudget(fresh.filter { it.pinned || it.section != null || it.id in keep }))
    }

    private fun withinBudget(list: List<ClipItem>): List<ClipItem> {
        var total = list.sumOf { it.total }
        val dropped = HashSet<Long>()
        list.filter { it.blob != null && !it.pinned && it.section == null }.sortedBy { it.copiedAt }.forEach {
            if (total > ClipPolicy.TOTAL_CHARS) {
                dropped += it.id
                total -= it.total
            }
        }
        return list.filter { it.id !in dropped }
    }

    fun setPinned(id: Long, pinned: Boolean): ClipBook = update(id) { it.copy(pinned = pinned) }

    fun setTitle(id: Long, title: String?): ClipBook {
        val clean = title?.trim()?.take(ClipPolicy.MAX_TITLE)?.ifEmpty { null }
        return update(id) { it.copy(title = clean) }
    }

    fun delete(id: Long): ClipBook = copy(items = items.filter { it.id != id })

    fun restore(item: ClipItem): ClipBook {
        if (items.any { it.id == item.id }) return this
        val home = item.section?.takeIf { name -> sections.any { it.name == name } }
        return copy(items = items + item.copy(section = home))
    }

    fun checkEdit(id: Long, newText: String): EditStatus {
        val item = items.firstOrNull { it.id == id } ?: return EditStatus.MISSING
        if (item.blob != null) return EditStatus.UNAVAILABLE
        return ClipEditing.problem(newText) ?: if (newText == item.text) EditStatus.UNCHANGED else EditStatus.OK
    }

    fun mergeTarget(id: Long, newText: String): ClipItem? {
        val item = items.firstOrNull { it.id == id } ?: return null
        return items.firstOrNull { it.id != id && it.blob == null && it.text == newText && openTo(item, it) }
    }

    fun edit(id: Long, newText: String): ClipBook {
        if (checkEdit(id, newText) != EditStatus.OK) return this
        val edited = items.first { it.id == id }.copy(text = newText)
        val twin = mergeTarget(id, newText) ?: return replace(edited)
        val merged = edited.copy(
            pinned = edited.pinned || twin.pinned,
            section = edited.section ?: twin.section,
            title = edited.title ?: twin.title,
            copiedAt = maxOf(edited.copiedAt, twin.copiedAt),
        )
        return copy(items = items.filter { it.id != twin.id }.map { if (it.id == id) merged else it })
    }

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

    private fun openTo(edited: ClipItem, other: ClipItem): Boolean {
        val home = sections.firstOrNull { it.name == other.section }
        return home?.locked != true || other.section == edited.section
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
    const val MAX_TITLE = 40
    const val TOTAL_CHARS = 8_000_000_000L
    const val DIRECT_COPY = 100_000
    const val PASTE_CONFIRM = 100_000
    const val CHUNK_CHARS = 4_000
    const val RESERVE_BYTES = 268_435_456L
    const val HARD_BYTES = 17_179_869_184L
    const val BLOB_GRACE_MS = 600_000L
}
