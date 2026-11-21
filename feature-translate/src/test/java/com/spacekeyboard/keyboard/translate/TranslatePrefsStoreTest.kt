package com.spacekeyboard.keyboard.translate

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslatePrefsStoreTest {
    private fun store(fake: FakePreferences = FakePreferences(), locale: Locale = Locale.US) = TranslatePrefsStore(fake.prefs, locale)

    @Test
    fun defaultsAreDeviceRouteAndNoConsent() {
        val store = store()
        assertFalse(store.consent())
        assertEquals(TranslateRoute.DEVICE, store.route())
        assertEquals(TranslateStyle.NEUTRAL, store.style())
        assertEquals("ar", store.target())
        assertEquals("en", store(locale = Locale.forLanguageTag("ar")).target())
    }

    @Test
    fun aiRouteCannotBeSetWithoutConsent() {
        val store = store()
        store.setRoute(TranslateRoute.AI)
        assertEquals(TranslateRoute.DEVICE, store.route())
        store.grantConsent()
        assertTrue(store.consent())
        assertEquals(TranslateRoute.AI, store.route())
        store.setRoute(TranslateRoute.DEVICE)
        assertEquals(TranslateRoute.DEVICE, store.route())
    }

    @Test
    fun withdrawingResetsRouteAndRotatesInstallId() {
        val store = store()
        store.grantConsent()
        val before = store.installId()
        store.withdrawConsent()
        assertFalse(store.consent())
        assertEquals(TranslateRoute.DEVICE, store.route())
        assertNotEquals(before, store.installId())
    }

    @Test
    fun oldConsentVersionIsNotConsent() {
        val fake = FakePreferences()
        fake.prefs.edit().putInt("consent_version", TranslatePolicy.CONSENT_VERSION + 1).apply()
        assertFalse(store(fake).consent())
        fake.prefs.edit().putInt("consent_version", 0).apply()
        assertFalse(store(fake).consent())
    }

    @Test
    fun installIdIsStableLowercaseUuid() {
        val fake = FakePreferences()
        val id = store(fake).installId()
        assertEquals(id, store(fake).installId())
        assertTrue(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matches(id))
    }

    @Test
    fun targetAndStyleRoundTripAndIgnoreBadValues() {
        val store = store()
        store.setTarget("fr")
        store.setStyle(TranslateStyle.CHAT)
        assertEquals("fr", store.target())
        assertEquals(TranslateStyle.CHAT, store.style())
        store.setTarget("xx")
        assertEquals("fr", store.target())
    }

    @Test
    fun observersAreNotifiedUntilClosed() {
        val store = store()
        var calls = 0
        val handle = store.observe { calls++ }
        store.setStyle(TranslateStyle.FORMAL)
        assertEquals(1, calls)
        handle.close()
        store.setStyle(TranslateStyle.CASUAL)
        assertEquals(1, calls)
    }
}
