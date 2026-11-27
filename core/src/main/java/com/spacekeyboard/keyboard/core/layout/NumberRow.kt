package com.spacekeyboard.keyboard.core.layout

object NumberRow {
    private val latin = "1234567890".map { it.toString() }
    private val arabicIndic = "١٢٣٤٥٦٧٨٩٠".map { it.toString() }
    private val persian = "۱۲۳۴۵۶۷۸۹۰".map { it.toString() }

    fun hasNative(layoutId: String): Boolean = layoutId == "ar" || layoutId == "fa"

    fun alternates(layoutId: String, native: Boolean): List<String> = when {
        native -> latin
        layoutId == "ar" -> arabicIndic
        layoutId == "fa" -> persian
        else -> emptyList()
    }

    fun extra(layoutId: String, firstRowKeys: Int): KeySpec? =
        if (firstRowKeys == latin.size + 1 && (layoutId == "ar" || layoutId == "fa")) {
            KeySpec(LayoutSpec.NUMBER_ROW_PREFIX + "q", "\u061F", KeyAction.COMMIT, 1f, false, listOf("@", "?"))
        } else {
            null
        }

    fun digits(layoutId: String, native: Boolean): List<String> = when {
        !native -> latin
        layoutId == "ar" -> arabicIndic
        layoutId == "fa" -> persian
        else -> latin
    }
}
