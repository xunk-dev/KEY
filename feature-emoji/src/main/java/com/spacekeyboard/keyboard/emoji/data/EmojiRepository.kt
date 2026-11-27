package com.spacekeyboard.keyboard.emoji.data

import android.content.Context
import android.graphics.Typeface
import com.spacekeyboard.keyboard.emoji.model.EmojiGroup
import com.spacekeyboard.keyboard.emoji.model.EmojiName
import com.spacekeyboard.keyboard.emoji.model.EmojiParser
import com.spacekeyboard.keyboard.emoji.model.HintParser
import com.spacekeyboard.keyboard.emoji.model.NameParser
import java.io.IOException

class EmojiRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private var names: List<Map<String, EmojiName>>? = null
    private var hints: Map<String, List<String>>? = null

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

    @Synchronized
    fun hints(): Map<String, List<String>> {
        hints?.let { return it }
        val loaded = try {
            HintParser.parse(lines(HINTS).asSequence())
        } catch (_: IOException) {
            emptyMap()
        }
        hints = loaded
        return loaded
    }

    private fun lines(name: String): List<String> =
        assets.open("emoji/$name").bufferedReader(Charsets.UTF_8).use { it.readLines() }

    private companion object {
        const val CATALOG = "emoji.tsv"
        const val HINTS = "hints.tsv"
        val LANGUAGES = listOf("en", "ar")
    }
}
