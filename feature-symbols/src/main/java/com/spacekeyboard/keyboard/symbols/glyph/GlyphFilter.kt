package com.spacekeyboard.keyboard.symbols.glyph

import android.graphics.Paint
import com.spacekeyboard.keyboard.symbols.model.asText
import java.util.concurrent.ConcurrentHashMap

fun interface GlyphFilter {
    fun has(codePoint: Int): Boolean
}

class PaintGlyphFilter : GlyphFilter {
    private val paint = Paint()
    private val known = ConcurrentHashMap<Int, Boolean>()

    override fun has(codePoint: Int): Boolean {
        if (codePoint < ASCII_LIMIT) return true
        return known.getOrPut(codePoint) { paint.hasGlyph(codePoint.asText()) }
    }

    private companion object {
        const val ASCII_LIMIT = 0x80
    }
}
