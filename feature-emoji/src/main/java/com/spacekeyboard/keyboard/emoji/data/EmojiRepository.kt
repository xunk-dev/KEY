package com.spacekeyboard.keyboard.emoji.data

import android.content.Context
import android.graphics.Typeface
import com.spacekeyboard.keyboard.emoji.model.EmojiGroup
import com.spacekeyboard.keyboard.emoji.model.EmojiName
import com.spacekeyboard.keyboard.emoji.model.EmojiParser
import com.spacekeyboard.keyboard.emoji.model.NameParser
import java.io.IOException

class EmojiRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private var names: List<Map<String, EmojiName>>? = null

    fun groups(typeface: Typeface?): List<EmojiGroup> {
        val parsed = EmojiParser.parse(lines(CATALOG).asSequence())
        return GlyphFilter(typeface).apply(parsed)
    }

    @Synchronized
    fun names(): List<Map<String, EmojiName>> = names ?: LANGUAGES.map { language ->
        try {
            NameParser.parse(lines("names_$language.tsv").asSequence())
        } catch (_: IOException) {
            emptyMap()
        }
    }.also { names = it }

    private fun lines(name: String): List<String> =
        assets.open("emoji/$name").bufferedReader(Charsets.UTF_8).use { it.readLines() }

    private companion object {
        const val CATALOG = "emoji.tsv"
        val LANGUAGES = listOf("en", "ar")
    }
}
