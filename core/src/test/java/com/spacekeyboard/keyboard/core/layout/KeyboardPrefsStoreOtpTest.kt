package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardPrefsStoreOtpTest {
    private val fake = FakePreferences()
    private val store = KeyboardPrefsStore(fake.prefs)

    @Test
    fun otpOffersDefaultOnRoundTripAndReset() {
        assertEquals(true, store.read().typing.otpOffers)
        store.setOtpOffers(false)
        assertEquals(false, store.read().typing.otpOffers)
        assertEquals(true, store.read().typing.copyOffers)
        store.resetAll()
        assertEquals(true, store.read().typing.otpOffers)
    }

    @Test
    fun otpNotificationsDefaultOffRoundTripAndReset() {
        assertEquals(false, store.read().typing.otpNotifications)
        store.setOtpNotifications(true)
        assertEquals(true, store.read().typing.otpNotifications)
        assertEquals(true, store.read().typing.otpOffers)
        store.resetAll()
        assertEquals(false, store.read().typing.otpNotifications)
    }
}
