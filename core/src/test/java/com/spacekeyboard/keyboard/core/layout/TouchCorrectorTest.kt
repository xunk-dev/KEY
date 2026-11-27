package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TouchCorrectorTest {
    private val left = PlacedKey(letter("a"), SlotRef(0, 0), Box(0f, 0f, 100f, 100f), Box(0f, 0f, 100f, 100f))
    private val right = PlacedKey(letter("b"), SlotRef(0, 1), Box(100f, 0f, 200f, 100f), Box(100f, 0f, 200f, 100f))
    private val placement = Placement(listOf(left, right))

    private fun corrector(own: Int, other: Int) = TouchCorrector { _, letter -> if (letter == "a") own else other }

    @Test
    fun likelyNeighbourWinsNearTheEdge() {
        val result = corrector(0, 50).choose(placement, left, 95f, 50f) { "t" }
        assertSame(right, result)
    }

    @Test
    fun touchAwayFromTheEdgeKeepsTheKey() {
        val result = corrector(0, 50).choose(placement, left, 50f, 50f) { "t" }
        assertSame(left, result)
    }

    @Test
    fun similarLikelihoodKeepsTheKey() {
        val result = corrector(40, 50).choose(placement, left, 95f, 50f) { "t" }
        assertSame(left, result)
    }

    @Test
    fun emptyPrefixKeepsTheKey() {
        val result = corrector(0, 50).choose(placement, left, 95f, 50f) { "" }
        assertSame(left, result)
    }

    @Test
    fun nonLetterKeysAreNeverChanged() {
        val delete = PlacedKey(KeySpec("del", "x", KeyAction.DELETE, 1f, true), SlotRef(0, 0), left.cell, left.box)
        assertSame(delete, corrector(0, 50).choose(placement, delete, 95f, 50f) { "t" })
    }

    @Test
    fun numberRowAndMultiCharacterKeysAreNotCandidates() {
        val digits = PlacedKey(KeySpec(LayoutSpec.NUMBER_ROW_PREFIX + "b", "b", KeyAction.COMMIT, 1f, false), right.slot, right.cell, right.box)
        val wide = PlacedKey(KeySpec("ab", "ab", KeyAction.COMMIT, 1f, true), right.slot, right.cell, right.box)
        assertSame(left, corrector(0, 50).choose(Placement(listOf(left, digits)), left, 95f, 50f) { "t" })
        assertEquals(left, corrector(0, 50).choose(Placement(listOf(left, wide)), left, 95f, 50f) { "t" })
    }
}
