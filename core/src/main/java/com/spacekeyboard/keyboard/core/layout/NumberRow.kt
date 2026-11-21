package com.spacekeyboard.keyboard.core.layout

object NumberRow {
    private val latin = "1234567890".map { it.toString() }
    private val arabicIndic = "١٢٣٤٥٦٧٨٩٠".map { it.toString() }
    private val persian = "۱۲۳۴۵۶۷۸۹۰".map { it.toString() }

    fun digits(layoutId: String, native: Boolean): List<String> = when {
        !native -> latin
        layoutId == "ar" -> arabicIndic
        layoutId == "fa" -> persian
        else -> latin
    }
}
