package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipUndoTest {
    private fun ready() = ClipSession().withSettings(true, UnlockWindow.FIVE_MINUTES).open(0)

    private fun idOf(s: ClipSession, text: String) = s.book.items.first { it.text == text }.id

    @Test
    fun deletingKeepsTheItemForUndoAndRestoreBringsItBack() {
        val s = ready().record("a", false, 1).record("b", false, 2)
        val id = idOf(s, "a")
        val deleted = s.delete(id, 3)
        assertEquals(listOf("b"), deleted.book.items.map { it.text })
        assertEquals(id, deleted.snapshot(3).undo?.id)
        val back = deleted.restore()
        assertEquals(setOf("a", "b"), back.book.items.map { it.text }.toSet())
        assertNull(back.snapshot(4).undo)
    }

    @Test
    fun restoreKeepsPinSectionAndTime() {
        var s = ready().record("a", false, 5)
        s = s.copy(book = s.book.addSection("s"))
        val id = idOf(s, "a")
        s = s.copy(book = s.book.setPinned(id, true)).startAssign(id, 6).assign("s")
        val item = s.book.items.single()
        assertEquals(item, s.delete(id, 7).restore().book.items.single())
    }

    @Test
    fun restoreDropsASectionThatNoLongerExists() {
        var s = ready().record("a", false, 1)
        s = s.copy(book = s.book.addSection("s"))
        val id = idOf(s, "a")
        s = s.startAssign(id, 2).assign("s").delete(id, 3)
        s = s.copy(book = s.book.deleteSection("s"))
        assertNull(s.restore().book.items.single().section)
    }

    @Test
    fun undoEndsOnDismissCloseAndRestore() {
        val s = ready().record("a", false, 1).delete(1, 2)
        assertTrue(s.undo != null)
        assertNull(s.dismissUndo().undo)
        assertNull(s.close().undo)
        assertEquals(s.dismissUndo().book, s.dismissUndo().restore().book)
    }

    @Test
    fun restoreNeverDuplicatesAnItem() {
        val s = ready().record("a", false, 1)
        val id = idOf(s, "a")
        val twice = s.delete(id, 2).restore()
        assertEquals(1, twice.book.items.size)
        assertEquals(1, twice.copy(undo = s.book.items.single()).restore().book.items.size)
    }

    @Test
    fun namingFromTheSaveDialogPutsTheTextInTheNewSection() {
        val s = ready().record("a", false, 1)
        val id = idOf(s, "a")
        val named = s.startAssign(id, 2).startNaming().type("Work").confirmName()
        assertEquals("Work", named.book.items.single().section)
        assertEquals("Work", named.snapshot(3).section)
        assertEquals(ClipTab.SAVED, named.snapshot(3).tab)
        assertNull(named.snapshot(3).assigning)
    }

    @Test
    fun cancellingTheNameKeepsTheSaveDialogOpen() {
        val s = ready().record("a", false, 1)
        val id = idOf(s, "a")
        val back = s.startAssign(id, 2).startNaming().type("x").cancelNaming()
        assertEquals(ClipMode.BROWSE, back.mode)
        assertEquals(id, back.snapshot(3).assigning?.id)
    }
}
