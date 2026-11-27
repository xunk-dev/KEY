package com.spacekeyboard.keyboard.emoji.data

import com.spacekeyboard.keyboard.emoji.R

enum class EmojiSet(val id: String, val labelRes: Int, val asset: String?) {
    NOTO("noto", R.string.emoji_set_noto, "emoji/NotoColorEmoji.ttf"),
    SYSTEM("system", R.string.emoji_set_system, null);

    companion object {
        fun byId(id: String?): EmojiSet = entries.firstOrNull { it.id == id } ?: NOTO
    }
}
