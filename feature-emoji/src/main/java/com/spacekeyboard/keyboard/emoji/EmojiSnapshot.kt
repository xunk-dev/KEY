package com.spacekeyboard.keyboard.emoji

import android.graphics.Typeface
import com.spacekeyboard.keyboard.emoji.data.EmojiSet
import com.spacekeyboard.keyboard.emoji.model.Emoji
import com.spacekeyboard.keyboard.emoji.model.EmojiGroup
import com.spacekeyboard.keyboard.emoji.model.SkinTone

const val RECENTS_GROUP = "recents"

enum class EmojiMode { CLOSED, BROWSE, SEARCH }

enum class LoadState { IDLE, LOADING, READY, FAILED }

data class EmojiSnapshot(
    val mode: EmojiMode = EmojiMode.CLOSED,
    val allowed: Boolean = true,
    val load: LoadState = LoadState.IDLE,
    val groups: List<EmojiGroup> = emptyList(),
    val recents: List<String> = emptyList(),
    val tone: SkinTone = SkinTone.NONE,
    val set: EmojiSet = EmojiSet.NOTO,
    val group: String = "",
    val query: String = "",
    val results: List<Emoji> = emptyList(),
    val searchReady: Boolean = false,
    val typeface: Typeface? = null,
) {
    val open: Boolean get() = mode != EmojiMode.CLOSED

    val activeGroup: String
        get() = when {
            group == RECENTS_GROUP -> RECENTS_GROUP
            groups.any { it.id == group } -> group
            recents.isNotEmpty() -> RECENTS_GROUP
            else -> groups.firstOrNull()?.id ?: RECENTS_GROUP
        }
}
