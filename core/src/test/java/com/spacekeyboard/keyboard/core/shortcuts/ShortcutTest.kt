package com.spacekeyboard.keyboard.core.shortcuts

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutTest {
    private val store = ShortcutStore(FakePreferences().prefs)

    @Test
    fun bookExpandsTheLastWordOnly() {
        val book = ShortcutBook(mapOf("omw" to "on my way"))
        assertEquals(Expansion(3, "on my way"), book.expansionFor("hello omw"))
        assertNull(book.expansionFor("hello omwx"))
        assertNull(book.expansionFor("omw "))
        assertNull(book.expansionFor(""))
    }

    @Test
    fun bookFallsBackToLowercase() {
        val book = ShortcutBook(mapOf("brb" to "be right back"))
        assertEquals(Expansion(3, "be right back"), book.expansionFor("BRB"))
    }

    @Test
    fun storeRoundTripsAndRemoves() {
        assertTrue(store.add("omw", "on my way\nsoon"))
        assertTrue(store.add("ar", "مرحبا بك"))
        assertEquals(mapOf("omw" to "on my way\nsoon", "ar" to "مرحبا بك"), store.read())
        store.remove("omw")
        assertEquals(mapOf("ar" to "مرحبا بك"), store.read())
        store.clear()
        assertTrue(store.read().isEmpty())
    }

    @Test
    fun storeRejectsBadEntries() {
        assertFalse(store.add("", "x"))
        assertFalse(store.add("a b", " "))
        assertFalse(store.add("x".repeat(ShortcutStore.MAX_TRIGGER + 1), "x"))
        assertFalse(store.add("ok", "x".repeat(ShortcutStore.MAX_TEXT + 1)))
        assertTrue(store.read().isEmpty())
    }

    @Test
    fun triggersLoseWhitespace() {
        assertTrue(store.add(" a b ", "x"))
        assertEquals(setOf("ab"), store.read().keys)
    }

    @Test
    fun checkNamesTheReasonAnEntryIsRefused() {
        assertEquals(ShortcutCheck.NO_TRIGGER, store.check(" ", "x"))
        assertEquals(ShortcutCheck.TRIGGER_TOO_LONG, store.check("x".repeat(ShortcutStore.MAX_TRIGGER + 1), "x"))
        assertEquals(ShortcutCheck.NO_TEXT, store.check("ok", "  "))
        assertEquals(ShortcutCheck.TEXT_TOO_LONG, store.check("ok", "x".repeat(ShortcutStore.MAX_TEXT + 1)))
        assertEquals(ShortcutCheck.OK, store.check("ok", "x".repeat(ShortcutStore.MAX_TEXT)))
    }

    @Test
    fun checkReportsFullOnlyForNewTriggers() {
        repeat(ShortcutStore.MAX_ENTRIES) { assertTrue(store.add("k$it", "v")) }
        assertEquals(ShortcutCheck.FULL, store.check("new", "v"))
        assertEquals(ShortcutCheck.OK, store.check("k0", "changed"))
        assertTrue(store.add("k0", "changed"))
        assertEquals("changed", store.read().getValue("k0"))
    }

    @Test
    fun hasAndTriggerForFindSavedEntries() {
        assertTrue(store.add("addr", "12 Main Street\nCairo"))
        assertTrue(store.has(" addr "))
        assertFalse(store.has("other"))
        assertEquals("addr", store.triggerFor("12 Main Street\nCairo"))
        assertNull(store.triggerFor("nothing"))
    }

    @Test
    fun longRepliesRoundTripWithNewlines() {
        val reply = "Hello,\nThanks for writing.\n" + "x".repeat(1500)
        assertTrue(store.add("thx", reply))
        assertEquals(reply, store.book().expansionFor("thx")?.insert)
    }
}
