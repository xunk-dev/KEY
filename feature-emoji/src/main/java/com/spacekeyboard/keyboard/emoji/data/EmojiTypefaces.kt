package com.spacekeyboard.keyboard.emoji.data

import android.content.Context
import android.graphics.Typeface

object EmojiTypefaces {
    private val cache = HashMap<EmojiSet, Typeface>()

    @Synchronized
    fun load(context: Context, set: EmojiSet): Typeface? {
        val asset = set.asset ?: return null
        cache[set]?.let { return it }
        return try {
            Typeface.createFromAsset(context.applicationContext.assets, asset).also { cache[set] = it }
        } catch (_: RuntimeException) {
            null
        }
    }
}
