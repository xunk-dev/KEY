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
}
