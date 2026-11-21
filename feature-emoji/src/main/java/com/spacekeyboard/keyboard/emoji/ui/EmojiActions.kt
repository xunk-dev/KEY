package com.spacekeyboard.keyboard.emoji.ui

import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.model.SkinTone

interface EmojiActions {
    fun onPick(text: String)
    fun onDelete()
    fun onKeys()
    fun onSearch()
    fun onCloseSearch()
    fun onGroup(id: String)
    fun onTone(tone: SkinTone)
    fun onClearRecents()
}

object EmojiLabels {
    fun group(id: String): Int = when (id) {
        "smileys" -> R.string.emoji_group_smileys
        "people" -> R.string.emoji_group_people
        "animals" -> R.string.emoji_group_animals
        "food" -> R.string.emoji_group_food
        "travel" -> R.string.emoji_group_travel
        "activities" -> R.string.emoji_group_activities
        "objects" -> R.string.emoji_group_objects
        "symbols" -> R.string.emoji_group_symbols
        "flags" -> R.string.emoji_group_flags
        else -> R.string.emoji_recents
    }

    fun tone(tone: SkinTone): Int = when (tone) {
        SkinTone.NONE -> R.string.emoji_tone_none
        SkinTone.LIGHT -> R.string.emoji_tone_light
        SkinTone.MEDIUM_LIGHT -> R.string.emoji_tone_medium_light
        SkinTone.MEDIUM -> R.string.emoji_tone_medium
        SkinTone.MEDIUM_DARK -> R.string.emoji_tone_medium_dark
        SkinTone.DARK -> R.string.emoji_tone_dark
    }
}
