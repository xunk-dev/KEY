package com.spacekeyboard.keyboard.core.calendar

data class CalendarDate(val year: Int, val month: Int, val day: Int)

object Gregorian {
    fun isLeap(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    fun monthLength(year: Int, month: Int): Int = when (month) {
        2 -> if (isLeap(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    fun isValid(date: CalendarDate): Boolean =
        date.year in 1..9999 && date.month in 1..12 && date.day in 1..monthLength(date.year, date.month)

    fun toEpochDay(date: CalendarDate): Long {
        val y = (if (date.month <= 2) date.year - 1 else date.year).toLong()
        val era = Math.floorDiv(y, 400L)
        val yoe = y - era * 400
        val shifted = if (date.month > 2) date.month - 3 else date.month + 9
        val doy = (153 * shifted + 2) / 5 + date.day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    fun fromEpochDay(epochDay: Long): CalendarDate {
        val z = epochDay + 719468
        val era = Math.floorDiv(z, 146097L)
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val day = (doy - (153 * mp + 2) / 5 + 1).toInt()
        val month = (if (mp < 10) mp + 3 else mp - 9).toInt()
        val year = (yoe + era * 400).toInt() + if (month <= 2) 1 else 0
        return CalendarDate(year, month, day)
    }
}
