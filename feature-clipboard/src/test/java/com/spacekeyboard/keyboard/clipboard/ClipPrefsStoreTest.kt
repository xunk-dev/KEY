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

    @Test
    fun tabOrderDefaultsAndPersists() {
        val fake = FakePreferences()
        val store = ClipPrefsStore(fake.prefs)
        assertEquals(ClipTabOrder.DEFAULT, store.tabOrder())
        store.setTabOrder(listOf(ClipTab.SAVED, ClipTab.CURRENT, ClipTab.PINNED))
        assertEquals(listOf(ClipTab.SAVED, ClipTab.CURRENT, ClipTab.PINNED), ClipPrefsStore(fake.prefs).tabOrder())
        store.resetTabOrder()
        assertEquals(ClipTabOrder.DEFAULT, ClipPrefsStore(fake.prefs).tabOrder())
    }

    @Test
    fun savingAnOrderCountsAsKnowingTheGesture() {
        val store = ClipPrefsStore(FakePreferences().prefs)
        assertFalse(store.tabHintShown())
        store.setTabOrder(ClipTabOrder.DEFAULT)
        assertTrue(store.tabHintShown())
    }

    @Test
    fun aDamagedStoredOrderFallsBackSafely() {
        val fake = FakePreferences()
        fake.prefs.edit().putString("tab_order", "SAVED,bogus,,SAVED").apply()
        assertEquals(listOf(ClipTab.SAVED, ClipTab.CURRENT, ClipTab.PINNED), ClipPrefsStore(fake.prefs).tabOrder())
    }
}
