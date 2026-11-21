package com.spacekeyboard.keyboard.emoji.model

enum class SkinTone(val id: String, val modifier: Int, val swatch: Int) {
    NONE("none", 0, 0xFFFFCC4D.toInt()),
    LIGHT("light", 0x1F3FB, 0xFFF7DECE.toInt()),
    MEDIUM_LIGHT("medium_light", 0x1F3FC, 0xFFF3D2A2.toInt()),
    MEDIUM("medium", 0x1F3FD, 0xFFD5AB88.toInt()),
    MEDIUM_DARK("medium_dark", 0x1F3FE, 0xFFAF7E57.toInt()),
    DARK("dark", 0x1F3FF, 0xFF7C533E.toInt());

    val slot: Int get() = ordinal - 1

    companion object {
        fun byId(id: String?): SkinTone = entries.firstOrNull { it.id == id } ?: NONE

        fun ofModifier(codePoint: Int): SkinTone? = entries.firstOrNull { it != NONE && it.modifier == codePoint }
    }
}

data class Emoji(val text: String, val tones: List<String> = emptyList()) {
    val hasTones: Boolean get() = tones.size == TONE_COUNT

    fun forTone(tone: SkinTone): String =
        if (tone == SkinTone.NONE || !hasTones) text else tones[tone.slot].ifEmpty { text }

    companion object {
        const val TONE_COUNT = 5
    }
}

data class EmojiGroup(val id: String, val emojis: List<Emoji>)

object EmojiGroups {
    val ids: List<String> = listOf(
        "smileys", "people", "animals", "food", "travel", "activities", "objects", "symbols", "flags",
    )

    val representatives: Map<String, String> = mapOf(
        "smileys" to "😀",
        "people" to "👋",
        "animals" to "🐻",
        "food" to "🍔",
        "travel" to "🚗",
        "activities" to "⚽",
        "objects" to "💡",
        "symbols" to "❤️",
        "flags" to "🏁",
    )
}
