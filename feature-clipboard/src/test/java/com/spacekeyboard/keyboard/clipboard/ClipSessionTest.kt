package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipSessionTest {
    private fun ready(window: UnlockWindow = UnlockWindow.FIVE_MINUTES) =
        ClipSession().withSettings(true, window).open(0)

    private fun withLockedSection(window: UnlockWindow = UnlockWindow.FIVE_MINUTES): ClipSession {
        var s = ready(window).record("secret", false, 1).record("plain", false, 2)
        s = s.copy(book = s.book.addSection("vault"))
        val id = s.book.items.first { it.text == "secret" }.id
        s = s.startAssign(id, 3).assign("vault")
        return s.copy(book = s.book.setSectionLocked("vault", true))
    }

    @Test
    fun recordingNeedsConsentAllowedFieldAndNonSensitiveText() {
        assertEquals(1, ready().record("a", false, 1).book.items.size)
        assertEquals(0, ClipSession().record("a", false, 1).book.items.size)
        assertEquals(0, ready().record("a", true, 1).book.items.size)
        assertEquals(0, ready().setAllowed(false).record("a", false, 1).book.items.size)
    }

    @Test
    fun lockedSectionIsHiddenUntilUnlocked() {
        val s = withLockedSection().selectTab(ClipTab.SAVED)
        val hidden = s.snapshot(10)
        assertTrue(hidden.locked)
        assertTrue(hidden.items.isEmpty())
        val open = s.finishUnlock(true, 10).snapshot(11)
        assertFalse(open.locked)
        assertEquals(listOf("secret"), open.items.map { it.text })
    }

    @Test
    fun unlockExpiresAfterTheChosenWindow() {
        val s = withLockedSection().selectTab(ClipTab.SAVED).finishUnlock(true, 1000)
        assertFalse(s.snapshot(1000 + 299_999).locked)
        assertTrue(s.snapshot(1000 + 300_000).locked)
        assertEquals(1000 + 300_000L, s.relockAt(1001))
        assertNull(s.relockAt(1000 + 300_000))
    }

    @Test
    fun everyTimeGrantEndsWhenThePanelClosesOrTheViewChanges() {
        val s = withLockedSection(UnlockWindow.EVERY_TIME).selectTab(ClipTab.SAVED)
        assertTrue(s.snapshot(5).locked)
        val granted = s.finishUnlock(true, 5)
        assertFalse(granted.snapshot(6).locked)
        assertTrue(granted.close().open(7).selectTab(ClipTab.SAVED).snapshot(8).locked)
        assertTrue(granted.selectTab(ClipTab.CURRENT).selectTab(ClipTab.SAVED).snapshot(9).locked)
    }

    @Test
    fun failedUnlockKeepsTheSectionClosed() {
        val s = withLockedSection().selectTab(ClipTab.SAVED).beginUnlock()
        assertTrue(s.snapshot(5).unlocking)
        val failed = s.finishUnlock(false, 6)
        assertFalse(failed.snapshot(7).unlocking)
        assertTrue(failed.snapshot(7).locked)
    }

    @Test
    fun pinnedTabDoesNotLeakItemsOfLockedSections() {
        var s = withLockedSection()
        val id = s.book.items.first { it.text == "secret" }.id
        s = s.copy(book = s.book.setPinned(id, true)).selectTab(ClipTab.PINNED)
        assertTrue(s.snapshot(10).items.isEmpty())
        assertEquals(listOf("secret"), s.finishUnlock(true, 10).snapshot(11).items.map { it.text })
    }

    @Test
    fun lockedItemsCannotBeChangedWhileLocked() {
        val s = withLockedSection()
        val id = s.book.items.first { it.text == "secret" }.id
        assertEquals(s, s.togglePin(id, 10))
        assertEquals(s, s.delete(id, 10))
        assertEquals(s, s.startAssign(id, 10))
        assertEquals(s, s.toggleSectionLock("vault", 10))
        val open = s.finishUnlock(true, 10)
        assertTrue(open.togglePin(id, 11).book.items.first { it.id == id }.pinned)
        assertFalse(open.toggleSectionLock("vault", 11).book.sections.single().locked)
    }

    @Test
    fun lockingAnOpenSectionHidesItAtOnce() {
        var s = ready().record("x", false, 1)
        s = s.copy(book = s.book.addSection("a")).startAssign(s.book.items.single().id, 1).assign("a")
        s = s.selectTab(ClipTab.SAVED).finishUnlock(true, 2)
        val locked = s.toggleSectionLock("a", 3)
        assertTrue(locked.snapshot(4).locked)
    }

    @Test
    fun clearAndDeleteSectionAskFirst() {
        var s = ready().record("a", false, 1).askClear()
        assertEquals(1, s.answer(false).book.items.size)
        assertEquals(0, s.answer(true).book.items.size)
        s = ready().copy(book = ClipBook().addSection("a").addSection("b")).selectSection("a").askDeleteSection("a")
        assertEquals(listOf("b"), s.answer(true).book.sections.map { it.name })
        assertEquals("b", s.answer(true).snapshot(1).section)
    }

    @Test
    fun namingBuildsASectionAndSelectsIt() {
        var s = ready().startNaming()
        assertEquals(ClipMode.NAMING, s.mode)
        s = s.type("Work!").backspace().type("s")
        assertEquals("Works", s.naming)
        s = s.confirmName()
        assertEquals(ClipMode.BROWSE, s.mode)
        assertEquals(ClipTab.SAVED, s.tab)
        assertEquals("Works", s.snapshot(1).section)
        assertEquals(listOf("Works"), s.book.sections.map { it.name })
    }

    @Test
    fun suggestingReplacesTheDraftOnlyWhileNaming() {
        val s = ready().startNaming().type("ab").suggest("Work")
        assertEquals("Work", s.naming)
        assertEquals("Work", s.confirmName().snapshot(1).section)
        assertEquals("", ready().suggest("Work").naming)
    }

    @Test
    fun namingRejectsEmptyLongAndTooManySections() {
        assertEquals(ClipMode.BROWSE, ready().startNaming().type("  ").confirmName().mode)
        assertEquals(ClipPolicy.MAX_NAME, ready().startNaming().type("x".repeat(100)).naming.length)
        val full = (1..ClipPolicy.MAX_SECTIONS).fold(ClipBook()) { book, i -> book.addSection("s$i") }
        assertEquals(ClipMode.BROWSE, ready().copy(book = full).startNaming().mode)
    }

    @Test
    fun backspaceRemovesWholeEmoji() {
        val s = ready().startNaming().type("a").type("😀").backspace()
        assertEquals("a", s.naming)
    }

    @Test
    fun assignMovesBetweenSectionsAndBack() {
        var s = ready().record("t", false, 1)
        s = s.copy(book = s.book.addSection("a").addSection("b"))
        val id = s.book.items.single().id
        s = s.startAssign(id, 2).assign("a").startAssign(id, 3).assign("b")
        assertEquals("b", s.book.items.single().section)
        assertNull(s.startAssign(id, 4).assign(null).book.items.single().section)
        assertEquals(s, s.assign("a"))
    }

    @Test
    fun closedOrBlockedFieldsDoNotOpen() {
        assertEquals(ClipMode.CLOSED, ready().close().setAllowed(false).open(1).mode)
        assertEquals(ClipMode.BROWSE, ready().close().setAllowed(true).open(1).mode)
    }

    @Test
    fun openingPrunesExpiredItems() {
        val s = ready().record("old", false, 0).close().open(25 * 3_600_000L)
        assertTrue(s.snapshot(25 * 3_600_000L).items.isEmpty())
    }

    @Test
    fun selectedSectionFallsBackToTheFirstOne() {
        val s = ready().copy(book = ClipBook().addSection("a").addSection("b")).selectTab(ClipTab.SAVED)
        assertEquals("a", s.snapshot(1).section)
        assertEquals(s, s.selectSection("missing"))
        assertEquals("b", s.selectSection("b").snapshot(1).section)
    }

    @Test
    fun opensOnTheFirstTabOfTheOrder() {
        val savedFirst = listOf(ClipTab.SAVED, ClipTab.CURRENT, ClipTab.PINNED)
        val s = ready().withOrder(savedFirst).close().open(1)
        assertEquals(ClipTab.SAVED, s.tab)
        assertEquals(savedFirst, s.snapshot(2).order)
        assertEquals(ClipTab.CURRENT, s.selectTab(ClipTab.CURRENT).close().withOrder(ClipTabOrder.DEFAULT).open(3).tab)
    }

    @Test
    fun reorderingKeepsTheCurrentTabUntilTheNextOpen() {
        val s = ready().selectTab(ClipTab.PINNED).withOrder(listOf(ClipTab.SAVED, ClipTab.PINNED, ClipTab.CURRENT))
        assertEquals(ClipTab.PINNED, s.tab)
        assertEquals(ClipTab.SAVED, s.close().open(5).tab)
    }

    @Test
    fun anIncompleteOrderIsRepairedInsteadOfLosingTabs() {
        val s = ready().withOrder(listOf(ClipTab.SAVED, ClipTab.SAVED))
        assertEquals(listOf(ClipTab.SAVED, ClipTab.CURRENT, ClipTab.PINNED), s.order)
    }
}
