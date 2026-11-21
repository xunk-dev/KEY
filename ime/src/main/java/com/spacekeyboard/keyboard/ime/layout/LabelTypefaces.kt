package com.spacekeyboard.keyboard.ime.layout

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.spacekeyboard.keyboard.core.layout.LabelFont
import com.spacekeyboard.keyboard.core.layout.LabelStyle
import com.spacekeyboard.keyboard.designsystem.R as DesignR

class LabelTypefaces(context: Context) {
    private val context = context.applicationContext
    private val cache = HashMap<LabelStyle, Typeface>()

    fun resolve(style: LabelStyle): Typeface {
        val key = style.copy(scalePercent = 0)
        return cache.getOrPut(key) { build(key) }
    }

    private fun build(style: LabelStyle): Typeface {
        val flags = (if (style.bold) Typeface.BOLD else 0) or (if (style.italic) Typeface.ITALIC else 0)
        return when (style.font) {
            LabelFont.PLEX -> plex(style.bold, flags)
            LabelFont.SANS -> Typeface.create(Typeface.SANS_SERIF, flags)
            LabelFont.SERIF -> Typeface.create(Typeface.SERIF, flags)
            LabelFont.MONO -> Typeface.create(Typeface.MONOSPACE, flags)
            LabelFont.CURSIVE -> Typeface.create(CURSIVE, flags)
            LabelFont.CASUAL -> Typeface.create(CASUAL, flags)
        }
    }

    private fun plex(bold: Boolean, flags: Int): Typeface {
        val res = if (bold) DesignR.font.ibm_plex_sans_arabic_semibold else DesignR.font.ibm_plex_sans_arabic_regular
        val base = ResourcesCompat.getFont(context, res) ?: Typeface.DEFAULT
        return Typeface.create(base, flags and Typeface.ITALIC)
    }

    private companion object {
        const val CURSIVE = "cursive"
        const val CASUAL = "casual"
    }
}
