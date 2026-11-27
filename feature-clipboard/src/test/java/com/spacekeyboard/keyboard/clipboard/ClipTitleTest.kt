package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipTitleTest {
    private fun ready() = ClipSession().withSettings(true, UnlockWindow.FIVE_MINUTES).open(0).record("85780", false, 1)

    private fun idOf(s: ClipSession) = s.book.items.first().id

    @Test
    fun namingFlowSetsTheTitleAndReturnsToBrowse() {
        var s = ready()
        val id = idOf(s)
        s = s.startTitle(id, 2)
        assertEquals(ClipMode.NAMING, s.mode)
        assertEquals(id, s.snapshot(2).titling?.id)
        s = s.type("Wifi").confirmName()
        assertEquals(ClipMode.BROWSE, s.mode)
        assertEquals("Wifi", s.book.items.first().title)
        assertNull(s.snapshot(3).titling)
        assertEquals(ClipTab.CURRENT, s.tab)
    }

    @Test
    fun emptyConfirmRemovesTheTitleAndCancelKeepsIt() {
        var s = ready()
        val id = idOf(s)
        s = s.startTitle(id, 2).type("Code").confirmName()
        assertEquals("Code", s.book.items.first().title)
        assertEquals("Code", s.startTitle(id, 3).naming)
        assertEquals("Code", s.startTitle(id, 3).type("x").cancelNaming().book.items.first().title)
        assertNull(s.startTitle(id, 3).suggest("").confirmName().book.items.first().title)
    }

    @Test
    fun titleIsTrimmedAndLimited() {
        val s = ready()
        val id = idOf(s)
        val long = "a".repeat(100)
        assertEquals(ClipPolicy.MAX_TITLE, s.startTitle(id, 2).type(long).naming.length)
        assertEquals("x", s.copy(book = s.book.setTitle(id, "  x  ")).book.items.first().title)
        assertNull(s.copy(book = s.book.setTitle(id, "   ")).book.items.first().title)
    }

    @Test
    fun titleSurvivesPersistenceAndOldRecordsStillLoad() {
        val s = ready()
        val id = idOf(s)
        val titled = s.book.setTitle(id, "عنوان\tمع\nرموز")
        assertEquals("عنوان\tمع\nرموز", ClipCodec.decode(ClipCodec.encode(titled)).items.first().title)
        val legacy = "N\t5\nI\t1\tfalse\t\t10\thello\t\t0\t"
        val old = ClipCodec.decode(legacy).items.first()
        assertEquals("hello", old.text)
        assertNull(old.title)
    }

    @Test
    fun pinKeepsTheTitleAndSameTextKeepsIt() {
        var s = ready()
        val id = idOf(s)
        s = s.startTitle(id, 2).type("T").confirmName().togglePin(id, 3).record("85780", false, 4)
        assertEquals("T", s.book.items.first().title)
        assertEquals(true, s.book.items.first().pinned)
    }
}
