package com.spacekeyboard.keyboard.clipboard

enum class UnlockWindow(val id: String, val millis: Long) {
    EVERY_TIME("every", 0),
    ONE_MINUTE("1m", 60_000L),
    FIVE_MINUTES("5m", 300_000L),
    FIFTEEN_MINUTES("15m", 900_000L),
    ONE_HOUR("1h", 3_600_000L);

    fun isOpen(unlockedAt: Long?, now: Long): Boolean =
        unlockedAt != null && millis > 0 && now >= unlockedAt && now - unlockedAt < millis

    companion object {
        fun byId(id: String?): UnlockWindow = entries.firstOrNull { it.id == id } ?: EVERY_TIME
    }
}

object ClipFilter {
    fun shouldRecord(enabled: Boolean, markedSensitive: Boolean, text: String): Boolean =
        enabled && !markedSensitive && text.isNotBlank() && text.length <= ClipPolicy.MAX_TEXT
}
