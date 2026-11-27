package com.spacekeyboard.keyboard.core.calendar

import com.spacekeyboard.keyboard.core.text.DigitStyle
import com.spacekeyboard.keyboard.core.text.Digits

class DateText private constructor(
    val original: String,
    private val date: CalendarDate,
    private val separator: Char,
    private val yearFirst: Boolean,
    private val padded: Boolean,
    private val marker: Marker?,
) {
    enum class Marker { HIJRI, GREGORIAN }

    val length: Int get() = original.length

    fun isHijri(): Boolean = marker == Marker.HIJRI || (marker == null && date.year in HIJRI_YEARS)

    fun otherCalendar(style: DigitStyle): String? {
        val hijri = isHijri()
        val other = (if (hijri) HijriCalendar.toGregorian(date) else HijriCalendar.fromGregorian(date)) ?: return null
        return Digits.convert(render(other), style) + if (hijri) GREGORIAN_TAG else HIJRI_TAG
    }

    fun withDigits(style: DigitStyle): String {
        val tag = when (marker) {
            Marker.HIJRI -> HIJRI_TAG
            Marker.GREGORIAN -> GREGORIAN_TAG
            null -> ""
        }
        return Digits.convert(render(date), style) + tag
    }

    private fun render(value: CalendarDate): String {
        val year = value.year.toString().padStart(YEAR_DIGITS, '0')
        val day = pad(value.day)
        val month = pad(value.month)
        return if (yearFirst) "$year$separator$month$separator$day" else "$day$separator$month$separator$year"
    }

    private fun pad(number: Int): String = if (padded) number.toString().padStart(2, '0') else number.toString()

    private fun valid(): Boolean = if (isHijri()) HijriCalendar.isValid(date) else Gregorian.isValid(date)

    companion object {
        const val HIJRI_TAG = "هـ"
        const val GREGORIAN_TAG = "م"
        private const val YEAR_DIGITS = 4
        private val HIJRI_YEARS = HijriCalendar.FIRST_YEAR..HijriCalendar.LAST_YEAR
        private const val D = "[0-9٠-٩۰-۹]"
        private val PATTERN = Regex(
            "(?<![\\p{L}\\p{N}])($D{1,4})([/.\\-])($D{1,2})\\2($D{1,4})(?:\\s?(هـ|ه|م))?$",
        )

        fun parseTail(before: String): DateText? {
            val match = PATTERN.find(before) ?: return null
            val (first, separator, middle, last, mark) = match.destructured
            val yearFirst = first.length == YEAR_DIGITS
            if (!yearFirst && last.length != YEAR_DIGITS) return null
            val dayText = if (yearFirst) last else first
            val yearText = if (yearFirst) first else last
            val date = CalendarDate(number(yearText), number(middle), number(dayText))
            val padded = middle.startsWith("0") || dayText.startsWith("0") || (middle.length == 2 && dayText.length == 2)
            val parsed = DateText(match.value, date, separator.single(), yearFirst, padded, markerOf(mark))
            return parsed.takeIf { it.valid() }
        }

        private fun markerOf(mark: String): Marker? = when (mark) {
            "" -> null
            GREGORIAN_TAG -> Marker.GREGORIAN
            else -> Marker.HIJRI
        }

        private fun number(text: String): Int = text.fold(0) { acc, ch -> acc * 10 + Digits.valueOf(ch) }
    }
}
