package com.spacekeyboard.keyboard.core.shortcuts

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutVarsTest {
    private val now: Date = Calendar.getInstance(TimeZone.getDefault(), Locale.US).apply { clear(); set(2026, Calendar.NOVEMBER, 26, 14, 5) }.time

    private fun run(text: String, clip: () -> String = { "" }) = ShortcutVars.expand(text, now, Locale.US, clip)

    @Test
    fun textWithoutTokensIsUntouched() {
        assertEquals("plain {other} text", run("plain {other} text"))
        assertFalse(ShortcutVars.has("no braces"))
        assertTrue(ShortcutVars.has("a {date} b"))
    }

    @Test
    fun dateTimeAndDayAreFilledIn() {
        assertEquals("Nov 26, 2026", run("{date}"))
        assertEquals("Thursday", run("{day}"))
        assertTrue(run("{time}").startsWith("2:05"))
        assertEquals("Regards, Nov 26, 2026", run("Regards, {DATE}"))
    }

    @Test
    fun clipboardIsReadOnceAndOnlyWhenUsed() {
        var reads = 0
        assertEquals("abc and abc", run("{clipboard} and {clipboard}") { reads++; "abc" })
        assertEquals(1, reads)
        run("no tokens") { reads++; "x" }
        assertEquals(1, reads)
    }

    @Test
    fun emptyClipboardLeavesNothing() {
        assertEquals("[]", run("[{clipboard}]"))
    }
}
