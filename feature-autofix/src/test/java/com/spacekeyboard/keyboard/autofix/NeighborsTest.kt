package com.spacekeyboard.keyboard.autofix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NeighborsTest {
    private fun rows(vararg rows: Triple<String, Float, Float>): KeyMap = KeyMap(
        rows.flatMap { (letters, y, shift) -> letters.mapIndexed { i, ch -> KeyPoint(ch, i + shift, y, 1f, 1f) } },
    )

    private val qwerty = rows(Triple("qwertyuiop", 0f, 0f), Triple("asdfghjkl", 1f, 0.5f), Triple("zxcvbnm", 2f, 1.5f))
    private val azerty = rows(Triple("azertyuiop", 0f, 0f), Triple("qsdfghjklm", 1f, 0f), Triple("wxcvbn", 2f, 0.5f))

    @Test
    fun neighboursComeFromTheRealPositions() {
        assertTrue(qwerty.adjacent('i', 'o'))
        assertTrue(qwerty.adjacent('o', 'i'))
        assertTrue(qwerty.adjacent('a', 'w'))
        assertTrue(qwerty.adjacent('s', 'x'))
        assertFalse(qwerty.adjacent('a', 'p'))
        assertFalse(qwerty.adjacent('q', 's'))
        assertFalse(qwerty.adjacent('z', 'o'))
    }

    @Test
    fun sameLettersAreNeighboursOnlyWhereTheLayoutPutsThemTogether() {
        assertTrue(qwerty.adjacent('a', 'w'))
        assertFalse(azerty.adjacent('a', 'w'))
        assertTrue(azerty.adjacent('a', 'z'))
        assertTrue(qwerty.adjacent('a', 'z'))
    }

    @Test
    fun capitalsAreTheSameKeys() {
        assertTrue(qwerty.adjacent('I', 'o'))
        assertTrue(qwerty.adjacent('i', 'O'))
    }

    @Test
    fun unknownKeysAreNeverBlocked() {
        assertTrue(qwerty.adjacent('i', 'ب'))
        assertEquals(Zone.NONE, qwerty.toward('i', 'ب'))
        assertTrue(KeyNeighbors.Unknown.adjacent('a', 'p'))
    }

    @Test
    fun directionFacesTheIntendedKey() {
        assertEquals(5, qwerty.toward('i', 'o'))
        assertEquals(3, qwerty.toward('o', 'i'))
        assertEquals(0, qwerty.toward('s', 'w'))
        assertEquals(8, qwerty.toward('w', 's'))
    }

    @Test
    fun driftMustLeanTowardTheIntendedKey() {
        assertTrue(qwerty.aligned('i', 'o', 5))
        assertTrue(qwerty.aligned('i', 'o', 2))
        assertTrue(qwerty.aligned('i', 'o', 8))
        assertFalse(qwerty.aligned('i', 'o', 4))
        assertFalse(qwerty.aligned('i', 'o', 3))
        assertTrue(qwerty.aligned('i', 'o', Zone.NONE))
    }

    private fun slip(neighbors: KeyNeighbors, wrong: Char, zone: Int, model: ErrorModel): Typing {
        val session = Typing(AutoFixConfig(live = false, words = false), model)
        session.engine.neighbors = neighbors
        return session.type("hell").zones(wrong to zone).back().type("o").space()
    }

    @Test
    fun nearKeySlipIsLearnedAsLetterAndTouch() {
        val model = ErrorModel()
        slip(qwerty, 'i', 5, model)
        assertEquals(1, model.contexts.rule(Spot("hell", 'i'), 'o')?.fixed)
        assertEquals(1, model.zones.rule(Cell('i', 5), 'o')?.fixed)
        assertEquals(1, model.words.rule("helli", "hello")?.fixed)
    }

    @Test
    fun farKeyChangeIsAWordNotAFingerSlip() {
        val model = ErrorModel()
        slip(qwerty, 'z', 4, model)
        assertNull(model.contexts.rule(Spot("hell", 'z'), 'o'))
        assertNull(model.zones.rule(Cell('z', 4), 'o'))
        assertNotNull(model.words.rule("hellz", "hello"))
    }

    @Test
    fun touchIsLearnedOnlyWhenTheDriftPointsAtTheIntendedKey() {
        val model = ErrorModel()
        slip(qwerty, 'i', 4, model)
        assertEquals(1, model.contexts.rule(Spot("hell", 'i'), 'o')?.fixed)
        assertNull(model.zones.rule(Cell('i', 4), 'o'))
    }

    @Test
    fun withoutLayoutInfoBehaviourIsUnchanged() {
        val model = ErrorModel()
        slip(KeyNeighbors.Unknown, 'z', 4, model)
        assertEquals(1, model.contexts.rule(Spot("hell", 'z'), 'o')?.fixed)
        assertEquals(1, model.zones.rule(Cell('z', 4), 'o')?.fixed)
    }
}
