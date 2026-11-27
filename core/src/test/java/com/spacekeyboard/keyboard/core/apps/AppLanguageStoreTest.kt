package com.spacekeyboard.keyboard.core.apps

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageStoreTest {
    private val fake = FakePreferences()
    private val store = AppLanguageStore(fake.prefs)

    @Test
    fun emptyStoreKnowsNothing() {
        assertNull(store.get("com.example"))
        assertEquals(0, store.size())
    }

    @Test
    fun languageIsRememberedPerPackage() {
        store.put("com.a", "ar")
        store.put("com.b", "en")
        assertEquals("ar", store.get("com.a"))
        assertEquals("en", store.get("com.b"))
        assertEquals(2, store.size())
    }

    @Test
    fun newChoiceReplacesTheOldOne() {
        store.put("com.a", "ar")
        store.put("com.a", "fa")
        assertEquals("fa", store.get("com.a"))
        assertEquals(1, store.size())
    }

    @Test
    fun invalidEntriesAreIgnored() {
        store.put("", "ar")
        store.put("com.a", "")
        store.put("x".repeat(161), "ar")
        assertEquals(0, store.size())
    }

    @Test
    fun oldestEntriesAreDroppedAtTheLimit() {
        repeat(AppLanguageStore.MAX_ENTRIES + 1) { store.put("app$it", "ar") }
        assertEquals(AppLanguageStore.MAX_ENTRIES, store.size())
        assertNull(store.get("app0"))
        assertEquals("ar", store.get("app${AppLanguageStore.MAX_ENTRIES}"))
    }

    @Test
    fun touchingAnEntryProtectsItFromEviction() {
        store.put("keep", "ar")
        repeat(AppLanguageStore.MAX_ENTRIES - 1) { store.put("app$it", "en") }
        store.put("keep", "fa")
        store.put("extra", "en")
        assertEquals("fa", store.get("keep"))
        assertNull(store.get("app0"))
    }

    @Test
    fun clearForgetsEverything() {
        store.put("com.a", "ar")
        store.clear()
        assertEquals(0, store.size())
        assertNull(store.get("com.a"))
    }
}
