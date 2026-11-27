package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipSessionActionsTest {
    private fun ready(window: UnlockWindow = UnlockWindow.FIVE_MINUTES) =
        ClipSession().withSettings(true, window).open(0)

    private fun withLockedSection(): ClipSession {
        var s = ready().record("secret", false, 1).record("plain", false, 2)
        s = s.copy(book = s.book.addSection("vault"))
        s = s.startAssign(idOf(s, "secret"), 3).assign("vault")
        return s.copy(book = s.book.setSectionLocked("vault", true))
    }

    private fun idOf(s: ClipSession, text: String) = s.book.items.first { it.text == text }.id

    @Test
    fun copyAndShareGetTextOnlyWhenReachable() {
        val s = withLockedSection()
        assertEquals(ClipPayload("plain", false), s.payload(idOf(s, "plain"), 10))
        assertNull(s.payload(idOf(s, "secret"), 10))
        assertEquals(ClipPayload("secret", true), s.finishUnlock(true, 10).payload(idOf(s, "secret"), 11))
        assertNull(s.finishUnlock(true, 10).payload(idOf(s, "secret"), 10 + 300_000))
        assertNull(s.payload(404, 10))
    }

    @Test
    fun fullViewIsBlockedForLockedTextAndHidesWhenItRelocks() {
        val s = withLockedSection()
        val id = idOf(s, "secret")
        assertEquals(s, s.startView(id, 10))
        val open = s.finishUnlock(true, 10).selectTab(ClipTab.SAVED).finishUnlock(true, 10)
        val viewing = open.startView(id, 11)
        assertEquals("secret", viewing.snapshot(12).viewing?.text)
        assertNull(viewing.snapshot(10 + 300_000).viewing)
    }

    @Test
    fun fullViewClosesWithPanelTabChangeAndDelete() {
        val s = ready().record("a", false, 1)
        val id = s.book.items.single().id
        val viewing = s.startView(id, 2)
        assertEquals("a", viewing.snapshot(3).viewing?.text)
        assertNull(viewing.closeView().snapshot(3).viewing)
        assertNull(viewing.close().open(4).snapshot(5).viewing)
        assertNull(viewing.selectTab(ClipTab.PINNED).snapshot(5).viewing)
        assertNull(viewing.delete(id, 3).snapshot(4).viewing)
    }

    @Test
    fun editNeedsALeaseAndTheLeaseNeedsReachability() {
        val s = withLockedSection()
        val secret = idOf(s, "secret")
        assertEquals(s, s.startEdit(secret, 10))
        assertEquals(EditStatus.LOCKED, s.checkEdit(secret, "x"))
        assertEquals(s, s.edit(secret, "x"))
        assertNull(s.editableText(secret))
        val open = s.finishUnlock(true, 10).startEdit(secret, 11)
        assertEquals("secret", open.editableText(secret))
        assertEquals("changed", open.edit(secret, "changed").book.items.first { it.id == secret }.text)
    }

    @Test
    fun leaseOutlivesTheUnlockWindowButOnlyForItsOwnItem() {
        val s = withLockedSection()
        val secret = idOf(s, "secret")
        val plain = idOf(s, "plain")
        val leased = s.finishUnlock(true, 10).startEdit(secret, 11).close()
        assertEquals(EditStatus.OK, leased.checkEdit(secret, "later"))
        assertEquals(EditStatus.LOCKED, leased.checkEdit(plain, "other"))
        assertEquals(EditStatus.LOCKED, leased.endEdit(secret).checkEdit(secret, "later"))
        assertEquals(EditStatus.MISSING, leased.delete(secret, 12).checkEdit(secret, "later"))
    }

    @Test
    fun editKeepsPinAndSectionThroughTheSession() {
        var s = ready().record("a", false, 1)
        val id = s.book.items.single().id
        s = s.copy(book = s.book.addSection("w").setPinned(id, true)).startAssign(id, 2).assign("w")
        val edited = s.startEdit(id, 3).edit(id, "b")
        val item = edited.book.items.single()
        assertEquals("b", item.text)
        assertTrue(item.pinned)
        assertEquals("w", item.section)
    }

    @Test
    fun editedTextFromLockedSectionStaysHiddenUntilUnlocked() {
        val s = withLockedSection().finishUnlock(true, 10)
        val id = idOf(s, "secret")
        val edited = s.startEdit(id, 11).edit(id, "new secret").selectTab(ClipTab.SAVED)
        assertEquals(listOf("new secret"), edited.snapshot(12).items.map { it.text })
        assertTrue(edited.snapshot(10 + 300_000).locked)
        assertFalse(edited.snapshot(10 + 300_000).items.any { it.text == "new secret" })
    }

    @Test
    fun selfCopyGuardSkipsOnlyTheMarkedTextOnceAndForABriefTime() {
        var now = 0L
        val guard = SelfCopyGuard { now }
        guard.mark("a")
        assertFalse(guard.consume("b"))
        assertTrue(guard.consume("a"))
        assertFalse(guard.consume("a"))
        guard.mark("a")
        now = 5_000
        assertFalse(guard.consume("a"))
        guard.mark("c")
        guard.clear()
        assertFalse(guard.consume("c"))
    }
}
