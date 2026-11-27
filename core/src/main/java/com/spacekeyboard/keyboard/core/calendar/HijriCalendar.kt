package com.spacekeyboard.keyboard.core.calendar

object HijriCalendar {
    const val FIRST_YEAR = HijriTable.FIRST_YEAR
    const val LAST_YEAR = HijriTable.LAST_YEAR

    private val yearStarts: LongArray = LongArray(LAST_YEAR - FIRST_YEAR + 2).also { starts ->
        starts[0] = HijriTable.START_DAY
        for (index in 0..LAST_YEAR - FIRST_YEAR) {
            starts[index + 1] = starts[index] + yearLength(HijriTable.MASKS[index])
        }
    }

    fun monthLength(year: Int, month: Int): Int {
        val mask = HijriTable.MASKS[year - FIRST_YEAR]
        return if ((mask shr (month - 1)) and 1 == 1) 30 else 29
    }

    fun isValid(date: CalendarDate): Boolean =
        date.year in FIRST_YEAR..LAST_YEAR && date.month in 1..12 && date.day in 1..monthLength(date.year, date.month)

    fun toGregorian(date: CalendarDate): CalendarDate? {
        if (!isValid(date)) return null
        return Gregorian.fromEpochDay(toEpochDay(date))
    }

    fun fromGregorian(date: CalendarDate): CalendarDate? {
        if (!Gregorian.isValid(date)) return null
        val epochDay = Gregorian.toEpochDay(date)
        if (epochDay < yearStarts.first() || epochDay >= yearStarts.last()) return null
        var index = 0
        while (yearStarts[index + 1] <= epochDay) index++
        val year = FIRST_YEAR + index
        var left = epochDay - yearStarts[index]
        var month = 1
        while (left >= monthLength(year, month)) {
            left -= monthLength(year, month)
            month++
        }
        return CalendarDate(year, month, left.toInt() + 1)
    }

    private fun toEpochDay(date: CalendarDate): Long {
        var days = yearStarts[date.year - FIRST_YEAR]
        for (month in 1 until date.month) days += monthLength(date.year, month)
        return days + date.day - 1
    }

    private fun yearLength(mask: Int): Int = 348 + Integer.bitCount(mask and 0xFFF)
}
