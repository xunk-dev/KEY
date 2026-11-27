package com.spacekeyboard.keyboard.clipboard.ui

internal object ClipMenuMetrics {
    const val ICON_ROW = 48
    const val ROW = 44
    const val MARGIN = 4
    const val BELOW_CARD = 36

    fun fullHeight(rows: Int): Int = ICON_ROW + ROW * rows

    fun height(rows: Int, available: Int): Int = minOf(fullHeight(rows), maxOf(available - MARGIN * 2, 0))

    fun top(wanted: Int, height: Int, available: Int): Int =
        wanted.coerceIn(MARGIN, maxOf(MARGIN, available - height - MARGIN))
}
