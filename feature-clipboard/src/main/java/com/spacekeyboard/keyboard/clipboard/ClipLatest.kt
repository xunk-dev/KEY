package com.spacekeyboard.keyboard.clipboard

object ClipLatest {
    fun livePasteable(sections: List<Section>, item: ClipItem): Boolean =
        item.blob == null && item.text.isNotBlank() && sections.none { it.name == item.section && it.locked }

    fun item(session: ClipSession, now: Long): ClipItem? {
        if (!session.enabled || !session.allowed) return null
        val book = session.book.prune(now)
        val latest = book.items.maxByOrNull { it.copiedAt } ?: return null
        return latest.takeIf { livePasteable(book.sections, it) }
    }

    fun text(session: ClipSession, now: Long): String? = item(session, now)?.text
}

fun ClipboardController.latestItem(): ClipItem? {
    ensureLoaded()
    return ClipLatest.item(session, System.currentTimeMillis())
}

fun ClipboardController.latestText(): String? {
    ensureLoaded()
    return ClipLatest.text(session, System.currentTimeMillis())
}
