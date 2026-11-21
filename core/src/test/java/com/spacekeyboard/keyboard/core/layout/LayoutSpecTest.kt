package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LayoutSpecTest {
    private val layout = sampleLayout()

    @Test
    fun swapExchangesTwoMovableKeys() {
        val order = layout.swapped(SlotRef(0, 0), SlotRef(2, 2))
        assertNotNull(order)
        assertEquals("del", order!![0][0])
        assertEquals("a", order[2][2])
        assertEquals(layout.order()[1], order[1])
    }

    @Test
    fun swapRejectsFixedSameAndMissingSlots() {
        assertNull(layout.swapped(SlotRef(0, 0), SlotRef(0, 0)))
        assertNull(layout.swapped(SlotRef(0, 0), SlotRef(1, 0)))
        assertNull(layout.swapped(SlotRef(2, 1), SlotRef(0, 1)))
        assertNull(layout.swapped(SlotRef(0, 0), SlotRef(9, 9)))
    }

    @Test
    fun arrangedKeepsSlotWeights() {
        val order = layout.swapped(SlotRef(1, 3), SlotRef(0, 0))!!
        val next = layout.arranged(order)!!
        assertEquals("g", next.rows[0][0].id)
        assertEquals(1f, next.rows[0][0].weight, 0f)
        assertEquals("a", next.rows[1][3].id)
        assertEquals(2f, next.rows[1][3].weight, 0f)
    }

    @Test
    fun arrangedRejectsBrokenOrders() {
        val base = layout.order()
        assertNull(layout.arranged(base.dropLast(1)))
        assertNull(layout.arranged(base.mapIndexed { i, row -> if (i == 0) row.dropLast(1) else row }))
        assertNull(layout.arranged(base.mapIndexed { i, row -> if (i == 0) listOf("a", "a", "c", "d") else row }))
        assertNull(layout.arranged(base.mapIndexed { i, row -> if (i == 0) listOf("a", "b", "c", "zzz") else row }))
    }

    @Test
    fun arrangedRejectsMovedFixedKeys() {
        val moved = layout.order().map { it.toMutableList() }
        moved[2][1] = "h"
        moved[2][0] = "space"
        assertNull(layout.arranged(moved))
        val spacer = layout.order().map { it.toMutableList() }
        spacer[1][0] = "e"
        spacer[1][1] = "pad"
        assertNull(layout.arranged(spacer))
    }

    @Test
    fun identityOrderIsAccepted() {
        assertEquals(layout, layout.arranged(layout.order()))
    }
}
