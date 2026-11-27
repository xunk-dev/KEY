package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardPrefsStoreConvertTest {
    private val fake = FakePreferences()
    private val store = KeyboardPrefsStore(fake.prefs)

    @Test
    fun convertAndSpellSwitchesDefaultOnRoundTripAndReset() {
        assertEquals(true, store.read().typing.convertOffers)
        assertEquals(true, store.read().typing.spellFix)
        store.setConvertOffers(false)
        store.setSpellFix(false)
        assertEquals(false, store.read().typing.convertOffers)
        assertEquals(false, store.read().typing.spellFix)
        store.resetAll()
        assertEquals(true, store.read().typing.convertOffers)
        assertEquals(true, store.read().typing.spellFix)
    }
}
