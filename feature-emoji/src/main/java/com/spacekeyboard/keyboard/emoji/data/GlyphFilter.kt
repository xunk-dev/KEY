package com.spacekeyboard.keyboard.emoji.data

import android.graphics.Paint
import android.graphics.Typeface
import com.spacekeyboard.keyboard.emoji.model.Emoji
import com.spacekeyboard.keyboard.emoji.model.EmojiGroup
import com.spacekeyboard.keyboard.emoji.model.SkinTone

class GlyphFilter(typeface: Typeface?) {
    private val paint = Paint().apply { this.typeface = typeface }

    fun renderable(text: String): Boolean = paint.hasGlyph(text)

    fun apply(groups: List<EmojiGroup>): List<EmojiGroup> = groups.mapNotNull { group ->
        val kept = group.emojis.filter { renderable(it.text) }.map(::checkTones)
        if (kept.isEmpty()) null else EmojiGroup(group.id, kept)
    }

    private fun checkTones(emoji: Emoji): Emoji {
        if (!emoji.hasTones) return emoji
        val probe = emoji.forTone(SkinTone.MEDIUM)
        return if (probe != emoji.text && renderable(probe)) emoji else emoji.copy(tones = emptyList())
    }
}
