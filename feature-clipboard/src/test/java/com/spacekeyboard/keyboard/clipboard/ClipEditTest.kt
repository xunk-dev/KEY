package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipEditTest {
    private fun bookWith(vararg texts: String): ClipBook =
        texts.foldIndexed(ClipBook()) { index, book, text -> book.add(text, index + 1L) }

    private fun idOf(book: ClipBook, text: String) = book.items.first { it.text == text }.id

    @Test
    fun editReplacesTheTextAndKeepsPinSectionAndTime() {
        var book = bookWith("old").addSection("a")
        val id = idOf(book, "old")
        book = book.setPinned(id, true).moveToSection(id, "a").edit(id, "new")
        val item = book.items.single()
        assertEquals(ClipItem(id, "new", true, "a", 1), item)
    }

    @Test
    fun editRejectsEmptyTooLongUnchangedAndMissing() {
        val book = bookWith("x")
        val id = idOf(book, "x")
        assertEquals(EditStatus.EMPTY, book.checkEdit(id, "  \n"))
        assertEquals(EditStatus.TOO_LONG, book.checkEdit(id, "y".repeat(ClipPolicy.MAX_TEXT + 1)))
        assertEquals(EditStatus.UNCHANGED, book.checkEdit(id, "x"))
        assertEquals(EditStatus.MISSING, book.checkEdit(99, "z"))
        assertEquals(EditStatus.OK, book.checkEdit(id, "y".repeat(ClipPolicy.MAX_TEXT)))
        listOf("", "x", "y".repeat(ClipPolicy.MAX_TEXT + 1)).forEach { assertEquals(book, book.edit(id, it)) }
        assertEquals(book, book.edit(99, "z"))
    }

    @Test
    fun editingToAnExistingTextMergesWithoutDuplicates() {
        var book = bookWith("a", "b").addSection("s")
        val a = idOf(book, "a")
        val b = idOf(book, "b")
        book = book.setPinned(b, true).moveToSection(b, "s")
        val merged = book.edit(a, "b")
        val item = merged.items.single()
        assertEquals(a, item.id)
        assertEquals("b", item.text)
        assertTrue(item.pinned)
        assertEquals("s", item.section)
        assertEquals(2L, item.copiedAt)
    }

    @Test
    fun mergeKeepsTheEditedItemsOwnSectionAndPinWhenItHasOne() {
        var book = bookWith("a", "b").addSection("s").addSection("t")
        val a = idOf(book, "a")
        val b = idOf(book, "b")
        book = book.moveToSection(a, "s").moveToSection(b, "t").setPinned(a, true)
        val item = book.edit(a, "b").items.single()
        assertEquals("s", item.section)
        assertTrue(item.pinned)
    }

    @Test
    fun editNeverMergesWithAnItemInAnotherLockedSection() {
        var book = bookWith("a", "b").addSection("vault")
        val a = idOf(book, "a")
        book = book.moveToSection(idOf(book, "b"), "vault").setSectionLocked("vault", true)
        assertNull(book.mergeTarget(a, "b"))
        assertEquals(listOf("b", "b"), book.edit(a, "b").items.map { it.text })
    }

    @Test
    fun mergeInsideTheSameLockedSectionIsAllowed() {
        var book = bookWith("a", "b").addSection("vault")
        book = book.moveToSection(idOf(book, "a"), "vault").moveToSection(idOf(book, "b"), "vault")
        book = book.setSectionLocked("vault", true)
        assertEquals(1, book.edit(idOf(book, "a"), "b").items.size)
    }

    @Test
    fun editedMultilineArabicTextSurvivesStorage() {
        val book = bookWith("x").let { it.edit(it.items.single().id, "سطر ١\n\tسطر ٢ \\ 😀") }
        assertEquals(book, ClipCodec.decode(ClipCodec.encode(book)))
    }

    @Test
    fun rerecordingACopiedTextOnlyRefreshesItsTime() {
        var book = bookWith("a", "b").addSection("s")
        val a = idOf(book, "a")
        book = book.setPinned(a, true).moveToSection(a, "s")
        val again = book.add("a", 50)
        assertEquals(book.items.size, again.items.size)
        val item = again.items.first { it.text == "a" }
        assertEquals(50L, item.copiedAt)
        assertTrue(item.pinned)
        assertEquals("s", item.section)
    }

    @Test
    fun editingHelpersAgree() {
        assertEquals(EditStatus.EMPTY, ClipEditing.problem(" "))
        assertEquals(EditStatus.TOO_LONG, ClipEditing.problem("x".repeat(ClipPolicy.MAX_TEXT + 1)))
        assertNull(ClipEditing.problem("ok"))
        assertTrue(ClipEditing.canSave("new", "old"))
        assertFalse(ClipEditing.canSave("same", "same"))
    }
}
