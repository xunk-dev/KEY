package com.spacekeyboard.keyboard.core.calendar

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HijriCalendarTest {
    @Test
    fun knownDates() {
        assertEquals(CalendarDate(1445, 1, 1), HijriCalendar.fromGregorian(CalendarDate(2023, 7, 19)))
        assertEquals(CalendarDate(1444, 9, 1), HijriCalendar.fromGregorian(CalendarDate(2023, 3, 23)))
        assertEquals(CalendarDate(2024, 4, 10), HijriCalendar.toGregorian(CalendarDate(1445, 10, 1)))
    }

    @Test
    fun matchesTheJdkTableForEveryDay() {
        var day = LocalDate.of(1900, 6, 1)
        val end = LocalDate.of(2077, 10, 1)
        while (day.isBefore(end)) {
            val expected = HijrahDate.from(day)
            val actual = HijriCalendar.fromGregorian(CalendarDate(day.year, day.monthValue, day.dayOfMonth))
            assertEquals(CalendarDate(expected.get(java.time.temporal.ChronoField.YEAR), expected.get(java.time.temporal.ChronoField.MONTH_OF_YEAR), expected.get(java.time.temporal.ChronoField.DAY_OF_MONTH)), actual)
            day = day.plusDays(1)
        }
    }

    @Test
    fun roundTripsEveryHijriDay() {
        for (year in HijriCalendar.FIRST_YEAR..HijriCalendar.LAST_YEAR) {
            for (month in 1..12) {
                for (day in 1..HijriCalendar.monthLength(year, month)) {
                    val hijri = CalendarDate(year, month, day)
                    val civil = HijriCalendar.toGregorian(hijri)!!
                    assertEquals(hijri, HijriCalendar.fromGregorian(civil))
                    val jdk = LocalDate.from(HijrahChronology.INSTANCE.date(year, month, day))
                    assertEquals(CalendarDate(jdk.year, jdk.monthValue, jdk.dayOfMonth), civil)
                }
            }
        }
    }

    @Test
    fun refusesWhatItCannotAnswer() {
        assertNull(HijriCalendar.fromGregorian(CalendarDate(1800, 1, 1)))
        assertNull(HijriCalendar.fromGregorian(CalendarDate(2023, 2, 30)))
        assertNull(HijriCalendar.toGregorian(CalendarDate(1445, 13, 1)))
        assertNull(HijriCalendar.toGregorian(CalendarDate(1445, 2, 31)))
        assertNull(HijriCalendar.toGregorian(CalendarDate(1200, 1, 1)))
    }

    @Test
    fun gregorianEpochDays() {
        assertEquals(0L, Gregorian.toEpochDay(CalendarDate(1970, 1, 1)))
        assertEquals(-25448L - 0, Gregorian.toEpochDay(Gregorian.fromEpochDay(-25448L)))
        assertEquals(CalendarDate(2000, 2, 29), Gregorian.fromEpochDay(Gregorian.toEpochDay(CalendarDate(2000, 2, 29))))
        assertEquals(false, Gregorian.isValid(CalendarDate(1900, 2, 29)))
    }
}
