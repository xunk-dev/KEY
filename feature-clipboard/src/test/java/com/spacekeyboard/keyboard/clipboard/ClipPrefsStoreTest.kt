package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipPrefsStoreTest {
    @Test
    fun historyIsOffAndEveryTimeByDefault() {
        val store = ClipPrefsStore(FakePreferences().prefs)
        assertFalse(store.enabled())
        assertEquals(UnlockWindow.EVERY_TIME, store.window())
        assertFalse(store.wipeRequested())
    }

    @Test
    fun valuesPersistAndNotifyObservers() {
        val fake = FakePreferences()
        val store = ClipPrefsStore(fake.prefs)
        var calls = 0
        val subscription = store.observe { calls++ }
        store.setEnabled(true)
        store.setWindow(UnlockWindow.FIFTEEN_MINUTES)
        assertTrue(store.enabled())
        assertEquals(UnlockWindow.FIFTEEN_MINUTES, ClipPrefsStore(fake.prefs).window())
        assertEquals(2, calls)
        subscription.close()
        store.setEnabled(false)
        assertEquals(2, calls)
    }

    @Test
    fun wipeRequestIsSetAndCleared() {
        val store = ClipPrefsStore(FakePreferences().prefs)
        store.requestWipe()
        assertTrue(store.wipeRequested())
        store.clearWipeRequest()
        assertFalse(store.wipeRequested())
    }
}
