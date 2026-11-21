package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardGeometryEdgeTest {
    private val delta = 0.5f

    private fun layoutOf(vararg rows: List<KeySpec>) = LayoutSpec("t", rows.toList())

    private fun place(layout: LayoutSpec, mode: PanelMode, oneHand: Int = 80) =
        KeyboardGeometry.place(layout, inputOf(mode, oneHand = oneHand))

    @Test
    fun singleKeyRowFillsTheWholeWidth() {
        val keys = place(layoutOf(listOf(letter("a"))), PanelMode.NORMAL).keys
        assertEquals(1, keys.size)
        assertEquals(0f, keys[0].cell.left, delta)
        assertEquals(1000f, keys[0].cell.right, delta)
    }

    @Test
    fun singleKeyRowInSplitLandsInOneHalfWithoutOverflow() {
        val keys = place(layoutOf(listOf(letter("a"))), PanelMode.SPLIT).keys
        assertEquals(1, keys.size)
        assertEquals(400f, keys[0].cell.width, delta)
        assertTrue(keys[0].cell.left >= 600f - delta || keys[0].cell.right <= 400f + delta)
        assertTrue(keys[0].cell.left >= 0f && keys[0].cell.right <= 1000f + delta)
    }

    @Test
    fun singleSpaceKeyRowSplitsIntoTwoEqualHalves() {
        val keys = place(layoutOf(listOf(fixed("space", KeyAction.SPACE, 4f))), PanelMode.SPLIT).keys
            .sortedBy { it.cell.left }
        assertEquals(2, keys.size)
        assertEquals(0f, keys[0].cell.left, delta)
        assertEquals(400f, keys[0].cell.right, delta)
        assertEquals(600f, keys[1].cell.left, delta)
        assertEquals(1000f, keys[1].cell.right, delta)
    }

    @Test
    fun fallbackLayoutSplitsAndKeepsDeleteOnTheRight() {
        val keys = place(LayoutSpec.fallback("x"), PanelMode.SPLIT).keys.sortedBy { it.cell.left }
        assertEquals(3, keys.size)
        assertEquals(2, keys.count { it.key.id == "space" })
        val delete = keys.first { it.key.id == "delete" }
        assertTrue(delete.cell.left >= 600f - delta)
        assertEquals(1000f, delete.cell.right, delta)
    }

    @Test
    fun rowWithoutASplittableKeyOnTheMiddleKeepsEveryKeyWhole() {
        val row = listOf(letter("a"), letter("b", 2f), letter("c"))
        val keys = place(layoutOf(row), PanelMode.SPLIT).keys.sortedBy { it.cell.left }
        assertEquals(listOf("a", "b", "c"), keys.map { it.key.id })
        assertEquals(400f, keys[0].cell.right, delta)
        assertEquals(600f, keys[1].cell.left, delta)
        assertEquals(1000f, keys[2].cell.right, delta)
        assertEquals(400f * 2f / 3f, keys[1].cell.width, delta)
        assertTrue(keys.none { it.cell.right > 400f + delta && it.cell.left < 600f - delta })
    }

    @Test
    fun aWideKeyAcrossTheMiddleGoesToTheSideHoldingMostOfIt() {
        val row = listOf(letter("a", 3f), letter("b"))
        val keys = place(layoutOf(row), PanelMode.SPLIT).keys.sortedBy { it.cell.left }
        assertEquals(0f, keys[0].cell.left, delta)
        assertEquals(400f, keys[0].cell.right, delta)
        assertEquals(600f, keys[1].cell.left, delta)
    }

    @Test
    fun spacerOnlyRowPlacesNothingButKeepsItsHeight() {
        val layout = layoutOf(
            listOf(fixed("p1", KeyAction.SPACER, 1f), fixed("p2", KeyAction.SPACER, 1f)),
            listOf(letter("a")),
        )
        listOf(PanelMode.NORMAL, PanelMode.SPLIT, PanelMode.ONE_HAND_LEFT).forEach { mode ->
            val keys = place(layout, mode).keys.filter { it.slot.row >= 0 }
            assertEquals(listOf("a"), keys.map { it.key.id })
            assertEquals(150f, keys[0].cell.top, delta)
        }
    }

    @Test
    fun oneHandSingleKeyRowStaysInsideTheAreaAndTheRailIsAdded() {
        val placement = place(layoutOf(listOf(letter("a"))), PanelMode.ONE_HAND_LEFT, oneHand = 70)
        val key = placement.keys.first { it.slot.row == 0 }
        assertEquals(0f, key.cell.left, delta)
        assertEquals(700f, key.cell.right, delta)
        assertEquals(3, placement.keys.size)
        assertNull(placement.movableAt(950f, 10f))
    }

    @Test
    fun zeroWidthInputDoesNotCrash() {
        val input = inputOf(PanelMode.SPLIT, width = 0f)
        val placement = KeyboardGeometry.place(sampleLayout(), input)
        assertTrue(placement.keys.all { it.cell.width == 0f || it.cell.width.isNaN() })
    }
}
