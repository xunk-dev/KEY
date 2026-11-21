package com.spacekeyboard.keyboard.core.apps

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageMemoryTest {
    private val store = AppLanguageStore(FakePreferences().prefs)
    private var language = "ar"
    private var enabled = true
    private val memory = LanguageMemory(store, { language }, { enabled }, ::choose)

    private fun choose(id: String): Boolean {
        language = id
        memory.onLanguage(id)
        return true
    }

    private fun switchTo(id: String) {
        language = id
        memory.onLanguage(id)
    }

    @Test
    fun switchingInAnAppIsRecorded() {
        memory.begin("com.chat", true)
        switchTo("en")
        assertEquals("en", store.get("com.chat"))
    }

    @Test
    fun theSavedLanguageIsRestoredWhenTheAppStartsAgain() {
        store.put("com.chat", "fa")
        memory.begin("com.chat", true)
        memory.restore()
        assertEquals("fa", language)
        assertEquals("fa", store.get("com.chat"))
    }

    @Test
    fun restoringDoesNotRecordOverAnotherApp() {
        store.put("com.a", "en")
        store.put("com.b", "fa")
        memory.begin("com.a", true)
        memory.restore()
        memory.end()
        memory.begin("com.b", true)
        memory.restore()
        assertEquals("en", store.get("com.a"))
        assertEquals("fa", store.get("com.b"))
    }

    @Test
    fun nothingIsRecordedWhenTheFieldIsSensitiveOrIncognito() {
        memory.begin("com.bank", false)
        switchTo("en")
        assertNull(store.get("com.bank"))
    }

    @Test
    fun nothingIsRecordedOrRestoredWhenTheSwitchIsOff() {
        store.put("com.chat", "fa")
        enabled = false
        memory.begin("com.chat", true)
        memory.restore()
        assertEquals("ar", language)
        switchTo("en")
        assertEquals("fa", store.get("com.chat"))
    }

    @Test
    fun nothingIsRecordedAfterTheFieldEnds() {
        memory.begin("com.chat", true)
        memory.end()
        switchTo("en")
        assertNull(store.get("com.chat"))
    }

    @Test
    fun unknownPackageKeepsTheCurrentLanguage() {
        memory.begin(null, true)
        memory.restore()
        switchTo("en")
        assertEquals("en", language)
        assertEquals(0, store.size())
    }
}
