package com.spacekeyboard.keyboard.core.suggest

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScopedStoreTest {
    private val store = ScopedStore(FakePreferences().prefs)

    @Test
    fun emptyStoreLoadsNothing() {
        assertFalse(store.has())
        assertTrue(store.load().scopes.isEmpty())
    }

    @Test
    fun savedStatsLoadBack() {
        store.save(ScopedStats().also { it.learn("app:a", "hello") })
        assertTrue(store.has())
        assertEquals(1, store.load().counts("app:a")["hello"])
    }

    @Test
    fun clearForgetsEverythingAndObserversHearIt() {
        var calls = 0
        val watch = store.observe { calls++ }
        store.save(ScopedStats().also { it.learn("app:a", "hello") })
        store.clear()
        assertFalse(store.has())
        assertTrue(store.load().scopes.isEmpty())
        assertEquals(2, calls)
        watch.close()
    }
}
