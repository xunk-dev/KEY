package com.spacekeyboard.keyboard.symbols.model

import com.spacekeyboard.keyboard.symbols.R

enum class SymbolCategory(val id: String, val labelRes: Int) {
    FEATURED("featured", R.string.cat_featured),
    CURRENCY("currency", R.string.cat_currency),
    MATH("math", R.string.cat_math),
    ARROWS("arrows", R.string.cat_arrows),
    STARS("stars", R.string.cat_stars),
    HEARTS("hearts", R.string.cat_hearts),
    SHAPES("shapes", R.string.cat_shapes),
    MARKS("marks", R.string.cat_marks),
    SIGNS("signs", R.string.cat_signs),
    PUNCTUATION("punctuation", R.string.cat_punctuation),
    BRACKETS("brackets", R.string.cat_brackets),
    FRAMES("frames", R.string.cat_frames),
    GREEK("greek", R.string.cat_greek),
    CIRCLED("circled", R.string.cat_circled),
    NUMERALS("numerals", R.string.cat_numerals),
    ARABIC("arabic", R.string.cat_arabic),
    GAMES("games", R.string.cat_games),
    MUSIC("music", R.string.cat_music),
    MISC("misc", R.string.cat_misc),
    ORNAMENTS("ornaments", R.string.cat_ornaments),
    TECHNICAL("technical", R.string.cat_technical);

    companion object {
        fun byId(id: String): SymbolCategory? = entries.firstOrNull { it.id == id }
    }
}
