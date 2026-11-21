package com.spacekeyboard.keyboard.emoji.data

import com.spacekeyboard.keyboard.emoji.model.Recents
import com.spacekeyboard.keyboard.emoji.model.SkinTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiPrefsStoreTest {
    private val fake = FakePreferences()
    private val store = EmojiPrefsStore(fake.prefs)

    @Test
    fun defaultsAreEmptyNoneAndNoto() {
        assertTrue(store.recents().isEmpty())
        assertEquals(SkinTone.NONE, store.tone())
        assertEquals(EmojiSet.NOTO, store.set())
    }

    @Test
    fun recentsKeepNewestFirstAndSurviveANewStore() {
        store.record("a")
        store.record("b")
        store.record("a")
        assertEquals(listOf("a", "b"), EmojiPrefsStore(fake.prefs).recents())
    }

    @Test
    fun recentsAreCappedAtTheLimit() {
        (1..80).forEach { store.record("e$it") }
        assertEquals(Recents.LIMIT, store.recents().size)
        assertEquals("e80", store.recents().first())
    }

    @Test
    fun clearRecentsRemovesEverythingButKeepsOtherSettings() {
        store.setTone(SkinTone.DARK)
        store.setSet(EmojiSet.SYSTEM)
        store.record("a")
        store.clearRecents()
        assertTrue(store.recents().isEmpty())
        assertEquals(SkinTone.DARK, store.tone())
        assertEquals(EmojiSet.SYSTEM, store.set())
    }

    @Test
    fun unknownSavedValuesFallBack() {
        store.setTone(SkinTone.MEDIUM)
        assertEquals(SkinTone.MEDIUM, store.tone())
        assertEquals(EmojiSet.NOTO, EmojiSet.byId("gone"))
        assertEquals(EmojiSet.SYSTEM, EmojiSet.byId("system"))
    }

    @Test
    fun observersHearEveryChange() {
        var calls = 0
        val subscription = store.observe { calls++ }
        store.record("a")
        store.setTone(SkinTone.LIGHT)
        store.clearRecents()
        subscription.close()
        store.record("b")
        assertEquals(3, calls)
    }
}
