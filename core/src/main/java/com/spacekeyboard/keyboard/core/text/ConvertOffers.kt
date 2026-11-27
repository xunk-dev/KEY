package com.spacekeyboard.keyboard.core.text

import com.spacekeyboard.keyboard.core.calendar.DateText

data class Conversion(val text: String, val remove: Int)

object ConvertOffers {
    const val LIMIT = 3
    private const val D = "[0-9٠-٩۰-۹]"
    private val NUMBER = Regex("(?<![\\p{L}\\p{N}])$D+(?:[.,:/\\-٫٬]$D+)*$")
    private val TAIL_MARKS = setOf('ـ', 'ه', 'م')

    fun forTail(before: String): List<Conversion> {
        val last = before.lastOrNull() ?: return emptyList()
        if (!Digits.isDigit(last) && last !in TAIL_MARKS) return emptyList()
        val date = DateText.parseTail(before)
        val offers = if (date != null) dateOffers(date) else numberOffers(before)
        return offers.filter { it.text != before.takeLast(it.remove) }.distinctBy { it.text }.take(LIMIT)
    }

    private fun numberOffers(before: String): List<Conversion> {
        val token = NUMBER.find(before)?.value ?: return emptyList()
        return Digits.targetsFor(token).map { Conversion(Digits.convert(token, it), token.length) }
    }

    private fun dateOffers(date: DateText): List<Conversion> {
        val current = Digits.styles(date.original).singleOrNull() ?: DigitStyle.WESTERN
        val other = if (current == DigitStyle.WESTERN) DigitStyle.ARABIC_INDIC else DigitStyle.WESTERN
        val calendars = listOf(current, other).mapNotNull(date::otherCalendar)
        val swaps = Digits.targetsFor(date.original).map(date::withDigits)
        return (calendars + swaps).map { Conversion(it, date.length) }
    }
}
