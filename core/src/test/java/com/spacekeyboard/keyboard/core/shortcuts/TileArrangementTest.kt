package com.spacekeyboard.keyboard.core.shortcuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TileArrangementTest {
    @Test
    fun defaultShowsEveryTileOnceAndHidesNone() {
        val arrangement = TileArrangement.DEFAULT
        assertEquals(ShortcutTile.entries, arrangement.visible)
        assertTrue(arrangement.hidden.isEmpty())
    }

    @Test
    fun removedTilesComeBackAtTheEndAndNothingIsLost() {
        val removed = TileArrangement.DEFAULT.remove(ShortcutTile.DIM).remove(ShortcutTile.FLOAT)
        assertEquals(listOf(ShortcutTile.DIM, ShortcutTile.FLOAT), removed.hidden)
        val back = removed.add(ShortcutTile.DIM)
        assertEquals(ShortcutTile.DIM, back.visible.last())
        assertEquals(ShortcutTile.entries.toSet(), (back.visible + back.hidden).toSet())
        assertEquals(ShortcutTile.entries.size, back.visible.size + back.hidden.size)
    }

    @Test
    fun settingsCanNeverBeRemovedOrLostFromStorage() {
        assertSame(TileArrangement.DEFAULT, TileArrangement.DEFAULT.remove(ShortcutTile.SETTINGS))
        val resolved = TileArrangement.resolve("mode", "settings,theme")
        assertTrue(ShortcutTile.SETTINGS in resolved.visible)
        assertFalse(ShortcutTile.SETTINGS in resolved.hidden)
    }

    @Test
    fun resolveDropsUnknownAndDuplicateIdsAndAppendsNewTiles() {
        val resolved = TileArrangement.resolve("dim,bogus,dim,mode", "theme")
        assertEquals(listOf(ShortcutTile.DIM, ShortcutTile.MODE), resolved.visible.take(2))
        assertEquals(listOf(ShortcutTile.THEME), resolved.hidden)
        assertEquals(ShortcutTile.entries.size, resolved.visible.size + resolved.hidden.size)
    }

    @Test
    fun moveSwapsWithNeighbourAndStopsAtTheEdges() {
        val first = TileArrangement.DEFAULT
        assertSame(first, first.move(first.visible.first(), -1))
        val moved = first.move(ShortcutTile.MODE, 1)
        assertEquals(listOf(ShortcutTile.THEME, ShortcutTile.MODE), moved.visible.take(2))
        assertFalse(first.canMove(ShortcutTile.MODE, -1))
        assertTrue(first.canMove(ShortcutTile.MODE, 1))
    }

    @Test
    fun moveSkipsTilesThatAreNotOnScreen() {
        val moved = TileArrangement.DEFAULT.move(ShortcutTile.THEME, 1) { it != ShortcutTile.DECORATE }
        assertEquals(ShortcutTile.EDIT, moved.visible[1])
        assertEquals(ShortcutTile.DECORATE, moved.visible[2])
        assertEquals(ShortcutTile.THEME, moved.visible[3])
    }
}
