package com.spacekeyboard.keyboard.emoji.model

import java.util.Locale

object SearchText {
    private val dropped = setOf('ـ', 'ٰ')

    fun normalize(raw: String): String {
        val builder = StringBuilder(raw.length)
        raw.lowercase(Locale.ROOT).forEach { c ->
            when {
                c in 'ً'..'ْ' || c in dropped -> Unit
                c == 'أ' || c == 'إ' || c == 'آ' || c == 'ٱ' -> builder.append('ا')
                c == 'ى' -> builder.append('ي')
                c == 'ة' -> builder.append('ه')
                else -> builder.append(c)
            }
        }
        return builder.toString()
    }

    fun words(raw: String): List<String> {
        val result = ArrayList<String>()
        val current = StringBuilder()
        normalize(raw).forEach { c ->
            if (c.isLetterOrDigit()) {
                current.append(c)
            } else if (current.isNotEmpty()) {
                result += current.toString()
                current.setLength(0)
            }
        }
        if (current.isNotEmpty()) result += current.toString()
        return result
    }
}
