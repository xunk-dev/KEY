package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipSmartTest {
    private fun session(smart: Boolean): ClipSession {
        var s = ClipSession().withSettings(true, UnlockWindow.EVERY_TIME).withSmart(smart).open(0)
        listOf(
            "plain words here",
            "https://example.com",
            "0944123456",
            "@zaid",
            "see https://t.me/zaid now",
            "second plain text",
        ).forEachIndexed { index, text -> s = s.record(text, false, 10L + index) }
        return s
    }

    @Test
    fun smartOffChangesNothing() {
        val off = session(false).snapshot(100)
        assertFalse(off.smart)
        assertTrue(off.kindCounts.isEmpty())
        assertFalse(off.grouped)
        assertFalse(off.showKinds)
        assertEquals(session(false).book.current.map { it.text }, off.items.map { it.text })
    }

    @Test
    fun smartOnGroupsEverythingByKindKeepingNewestFirstInsideGroups() {
        val on = session(true).snapshot(100)
        assertTrue(on.grouped)
        assertTrue(on.showKinds)
        assertEquals(
            listOf("see https://t.me/zaid now", "https://example.com", "0944123456", "@zaid", "second plain text", "plain words here"),
            on.items.map { it.text },
        )
        assertEquals(2, on.kindCounts[ClipKind.LINK])
        assertEquals(1, on.kindCounts[ClipKind.PHONE])
        assertEquals(2, on.kindCounts[ClipKind.TEXT])
    }

    @Test
    fun pickingAKindFiltersAndCountsIncludeEveryKindAnItemHolds() {
        val s = session(true).record("follow @ali_99 at https://x.com/ali", false, 50).selectKind(ClipKind.USERNAME)
        val view = s.snapshot(100)
        assertEquals(ClipKind.USERNAME, view.kind)
        assertEquals(listOf("follow @ali_99 at https://x.com/ali", "@zaid"), view.items.map { it.text })
        assertEquals(3, view.kindCounts[ClipKind.LINK])
        assertFalse(view.grouped)
    }

    @Test
    fun aKindMissingFromTheTabFallsBackToAll() {
        val s = session(true).selectKind(ClipKind.TOKEN).snapshot(100)
        assertNull(s.kind)
        assertEquals(6, s.items.size)
    }

    @Test
    fun turningSmartOffOrOnResetsTheFilter() {
        val picked = session(true).selectKind(ClipKind.LINK)
        assertEquals(ClipKind.LINK, picked.snapshot(100).kind)
        val off = picked.withSmart(false)
        assertNull(off.snapshot(100).kind)
        assertEquals(6, off.snapshot(100).items.size)
        assertNull(off.withSmart(true).snapshot(100).kind)
    }

    @Test
    fun kindSelectionIsIgnoredWhileSmartIsOff() {
        assertNull(session(false).selectKind(ClipKind.LINK).snapshot(100).kind)
    }

    @Test
    fun aSingleKindTabIsNotGrouped() {
        var s = ClipSession().withSettings(true, UnlockWindow.EVERY_TIME).withSmart(true).open(0)
        s = s.record("one", false, 1).record("two", false, 2)
        val view = s.snapshot(10)
        assertFalse(view.grouped)
        assertFalse(view.showKinds)
        assertEquals(listOf("two", "one"), view.items.map { it.text })
    }

    @Test
    fun largeTextsCountAsPlainText() {
        val book = ClipBook().addLarge("https://example.com preview", 200_000, "blob", "digest", 1)
        assertEquals(listOf(ClipKind.TEXT), book.items.single().kinds)
    }

    @Test
    fun openingTheClipboardClearsTheFilter() {
        val s = session(true).selectKind(ClipKind.LINK).close().open(200)
        assertNull(s.snapshot(201).kind)
    }
}
