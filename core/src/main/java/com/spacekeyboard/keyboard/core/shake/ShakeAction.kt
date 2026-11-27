package com.spacekeyboard.keyboard.core.shake

enum class ShakeAction(val id: String) {
    CALCULATOR("calculator"),
    EMOJI("emoji"),
    CLIPBOARD("clipboard"),
    TRANSLATE("translate"),
    SHORTCUTS("shortcuts"),
    VOICE("voice"),
    SYMBOLS("symbols"),
    IMAGES("images"),
    DECOR("decor"),
    FLOAT("float"),
    ;

    companion object {
        fun byId(id: String?): ShakeAction = entries.firstOrNull { it.id == id } ?: CALCULATOR
    }
}

enum class ShakeSensitivity(val id: String, val threshold: Float) {
    LOW("low", 14f),
    MEDIUM("medium", 10f),
    HIGH("high", 7f),
    ;

    companion object {
        fun byId(id: String?): ShakeSensitivity = entries.firstOrNull { it.id == id } ?: MEDIUM
    }
}
