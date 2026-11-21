package com.spacekeyboard.keyboard.decorate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DecorPrefsStoreTest {
    private val store = DecorPrefsStore(FakePreferences().prefs)

    @Test
    fun favoritesToggleOnAndOff() {
        assertTrue(store.favorites().isEmpty())
        store.toggleFavorite("en_bold")
        store.toggleFavorite("ar_t1_s0_m0")
        assertEquals(setOf("en_bold", "ar_t1_s0_m0"), store.favorites())
        store.toggleFavorite("en_bold")
        assertEquals(setOf("ar_t1_s0_m0"), store.favorites())
    }

    @Test
    fun unknownIdsAreDropped() {
        store.toggleFavorite("ghost")
        assertTrue(store.favorites().isEmpty())
        store.setActive("ghost")
        assertNull(store.active())
    }

    @Test
    fun activeStyleSetsAndClears() {
        store.setActive("en_mono")
        assertEquals("en_mono", store.active())
        store.setActive(null)
        assertNull(store.active())
    }
}
