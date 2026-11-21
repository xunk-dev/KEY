package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardGeometryTest {
    private val layout = sampleLayout()
    private val delta = 0.01f

    private fun rowOf(placement: Placement, row: Int) =
        placement.keys.filter { it.slot.row == row }.sortedBy { it.cell.left }

    @Test
    fun normalRowsFillTheWidthWithoutOverlap() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.NORMAL))
        for (row in 0..2) {
            val cells = rowOf(placement, row)
            val sum = cells.sumOf { it.cell.width.toDouble() }.toFloat()
            assertEquals(if (row == 1) 800f else 1000f, sum, 0.5f)
            cells.zipWithNext().forEach { (a, b) -> assertEquals(a.cell.right, b.cell.left, delta) }
        }
    }

    @Test
    fun spacersTakeSpaceButAreNotReturned() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.NORMAL))
        assertTrue(placement.keys.none { it.key.id == "pad" })
        assertEquals(200f, rowOf(placement, 1).first().cell.left, 0.5f)
    }

    @Test
    fun rowsShareTheHeightEvenly() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.NORMAL, height = 300f))
        listOf(0, 1, 2).forEach { row ->
            val cell = rowOf(placement, row).first().cell
            assertEquals(row * 100f, cell.top, delta)
            assertEquals(100f, cell.height, delta)
        }
    }

    @Test
    fun gapShrinksTheDrawnBoxOnly() {
        val input = inputOf(PanelMode.NORMAL).copy(gap = 10f)
        val key = KeyboardGeometry.place(layout, input).keys.first()
        assertEquals(key.cell.width - 10f, key.box.width, delta)
        assertEquals(key.cell.height - 10f, key.box.height, delta)
    }

    @Test
    fun oneHandRightKeepsKeysOnTheRightAndAddsRail() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.ONE_HAND_RIGHT))
        val keys = placement.keys.filter { it.slot.row >= 0 }
        assertTrue(keys.all { it.cell.left >= 200f - delta && it.cell.right <= 1000f + delta })
        val rail = placement.keys.filter { it.slot.row < 0 }
        assertEquals(listOf(KeyAction.EXPAND, KeyAction.FLIP), rail.map { it.key.action })
        assertTrue(rail.all { it.cell.right <= 200f + delta })
        assertEquals("‹", rail.first().key.label)
    }

    @Test
    fun oneHandLeftKeepsKeysOnTheLeftAndAddsRail() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.ONE_HAND_LEFT, oneHand = 70))
        val keys = placement.keys.filter { it.slot.row >= 0 }
        assertTrue(keys.all { it.cell.right <= 700f + delta })
        val rail = placement.keys.filter { it.slot.row < 0 }
        assertTrue(rail.all { it.cell.left >= 700f - delta })
        assertEquals("›", rail.first().key.label)
        assertEquals(300f, rail.sumOf { it.cell.height.toDouble() }.toFloat(), delta)
    }

    @Test
    fun splitLeavesACenterGapInEveryRow() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.SPLIT, splitGap = 20))
        for (row in 0..2) {
            val cells = rowOf(placement, row)
            val gap = cells.zipWithNext().maxOf { (a, b) -> b.cell.left - a.cell.right }
            assertEquals(200f, gap, 0.5f)
            assertEquals(1000f, cells.last().cell.right, 0.5f)
        }
    }

    @Test
    fun splitDividesTheSpaceKeyIntoTwoHalvesWithTheSameId() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.SPLIT, splitGap = 20))
        val halves = placement.keys.filter { it.key.id == "space" }.sortedBy { it.cell.left }
        assertEquals(2, halves.size)
        assertEquals(200f, halves[1].cell.left - halves[0].cell.right, 0.5f)
        assertFalse(halves[0].cell.width <= 0f || halves[1].cell.width <= 0f)
    }

    @Test
    fun hitFindsTheKeyUnderThePoint() {
        val placement = KeyboardGeometry.place(layout, inputOf(PanelMode.NORMAL))
        assertEquals("a", placement.hit(10f, 10f)?.key?.id)
        assertEquals("d", placement.hit(990f, 50f)?.key?.id)
        assertEquals(null, placement.hit(10f, 120f))
        assertEquals(null, placement.movableAt(500f, 250f))
    }
}
