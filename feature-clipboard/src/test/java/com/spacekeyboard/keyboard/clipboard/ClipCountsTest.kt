package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Test

class ClipCountsTest {
    @Test
    fun tabsReportHowManyTextsTheyHold() {
        val book = ClipBook().add("first", 1).add("second", 2).add("third", 3)
        val pinned = book.items.first { it.text == "first" }.id
        val session = ClipSession(book = book.setPinned(pinned, true))
        val counts = session.snapshot(10).counts
        assertEquals(2, counts[ClipTab.CURRENT])
        assertEquals(1, counts[ClipTab.PINNED])
        assertEquals(0, counts[ClipTab.SAVED])
    }
}
