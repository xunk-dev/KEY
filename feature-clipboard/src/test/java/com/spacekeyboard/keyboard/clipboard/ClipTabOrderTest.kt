package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClipTabOrderTest {
    private val c = ClipTab.CURRENT
    private val p = ClipTab.PINNED
    private val s = ClipTab.SAVED

    @Test
    fun parseKeepsKnownNamesAndAppendsMissingOnes() {
        assertEquals(listOf(s, c, p), ClipTabOrder.parse("SAVED"))
        assertEquals(listOf(s, p, c), ClipTabOrder.parse(" SAVED , PINNED,nope,,SAVED"))
        assertEquals(ClipTabOrder.DEFAULT, ClipTabOrder.parse(null))
        assertEquals(ClipTabOrder.DEFAULT, ClipTabOrder.parse(""))
    }

    @Test
    fun encodeRoundTrips() {
        listOf(listOf(s, p, c), listOf(p, c, s), ClipTabOrder.DEFAULT).forEach {
            assertEquals(it, ClipTabOrder.parse(ClipTabOrder.encode(it)))
        }
    }

    @Test
    fun moveToPutsTheTabInTheSlotAndShiftsTheOthers() {
        assertEquals(listOf(s, c, p), ClipTabOrder.moveTo(listOf(c, p, s), s, 0))
        assertEquals(listOf(p, s, c), ClipTabOrder.moveTo(listOf(c, p, s), c, 2))
        assertEquals(listOf(c, p, s), ClipTabOrder.moveTo(listOf(c, p, s), p, 1))
    }

    @Test
    fun moveToClampsOutOfRangeSlots() {
        assertEquals(listOf(s, c, p), ClipTabOrder.moveTo(listOf(c, p, s), s, -4))
        assertEquals(listOf(p, s, c), ClipTabOrder.moveTo(listOf(c, p, s), c, 99))
    }

    @Test
    fun shiftStopsAtTheEnds() {
        assertEquals(listOf(c, p, s), ClipTabOrder.shift(listOf(c, p, s), c, -1))
        assertEquals(listOf(c, p, s), ClipTabOrder.shift(listOf(c, p, s), s, 1))
        assertEquals(listOf(p, c, s), ClipTabOrder.shift(listOf(c, p, s), c, 1))
    }

    @Test
    fun slotAtFollowsTheReadingDirection() {
        assertEquals(0, ClipTabOrder.slotAt(10f, 300f, 3, rtl = false))
        assertEquals(2, ClipTabOrder.slotAt(290f, 300f, 3, rtl = false))
        assertEquals(2, ClipTabOrder.slotAt(10f, 300f, 3, rtl = true))
        assertEquals(0, ClipTabOrder.slotAt(290f, 300f, 3, rtl = true))
        assertEquals(1, ClipTabOrder.slotAt(150f, 300f, 3, rtl = true))
    }

    @Test
    fun slotAtClampsPointersOutsideTheBar() {
        assertEquals(0, ClipTabOrder.slotAt(-50f, 300f, 3, rtl = false))
        assertEquals(2, ClipTabOrder.slotAt(900f, 300f, 3, rtl = false))
        assertEquals(0, ClipTabOrder.slotAt(5f, 0f, 3, rtl = false))
    }

    @Test
    fun slotLeftIsTheInverseOfSlotAt() {
        for (rtl in listOf(false, true)) {
            for (slot in 0..2) {
                val left = ClipTabOrder.slotLeft(slot, 100f, 3, rtl)
                assertEquals(slot, ClipTabOrder.slotAt(left + 50f, 300f, 3, rtl))
            }
        }
        assertNotEquals(ClipTabOrder.slotLeft(0, 100f, 3, false), ClipTabOrder.slotLeft(0, 100f, 3, true))
    }
}
