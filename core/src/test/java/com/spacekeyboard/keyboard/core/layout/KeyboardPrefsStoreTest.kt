package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardPrefsStoreTest {
    private val fake = FakePreferences()
    private val store = KeyboardPrefsStore(fake.prefs)

    private fun raw(block: android.content.SharedPreferences.Editor.() -> Unit) {
        fake.prefs.edit().apply(block).apply()
    }

    @Test
    fun emptyStoreReadsTheDefaults() {
        assertEquals(KeyboardPrefs(), store.read())
    }

    @Test
    fun everyValueRoundTrips() {
        store.setKeySize(110)
        store.setHeight(90)
        store.setOneHand(70)
        store.setSplitGap(12)
        store.setLabelScale(120)
        store.setMode(PanelMode.SPLIT)
        store.setFont(LabelFont.MONO)
        store.setBold(true)
        store.setItalic(true)
        store.setLanguage("en")
        store.setToolbar(listOf(ToolbarItem.SYMBOLS, ToolbarItem.MODE, ToolbarItem.THEME))
        val read = store.read()
        assertEquals(110, read.keySizePercent)
        assertEquals(90, read.heightPercent)
        assertEquals(70, read.oneHandPercent)
        assertEquals(12, read.splitGapPercent)
        assertEquals(PanelMode.SPLIT, read.mode)
        assertEquals(LabelStyle(LabelFont.MONO, bold = true, italic = true, scalePercent = 120), read.label)
        assertEquals("en", read.language)
        assertEquals(listOf(ToolbarItem.SYMBOLS, ToolbarItem.MODE, ToolbarItem.THEME, ToolbarItem.EMOJI, ToolbarItem.DECORATE, ToolbarItem.CLIPBOARD, ToolbarItem.TRANSLATE, ToolbarItem.IMAGES, ToolbarItem.VOICE, ToolbarItem.EDIT, ToolbarItem.INCOGNITO, ToolbarItem.SETTINGS), read.toolbar)
    }

    @Test
    fun feedbackDefaultsAndRoundTrips() {
        assertEquals(FeedbackPrefs(vibrate = true, sound = false, preview = true, clipKeepOpen = false), store.read().feedback)
        store.setVibrate(false)
        store.setSound(true)
        store.setPreview(false)
        store.setClipKeepOpen(true)
        assertEquals(FeedbackPrefs(vibrate = false, sound = true, preview = false, clipKeepOpen = true), store.read().feedback)
    }

    @Test
    fun resetAllAlsoRestoresFeedback() {
        store.setVibrate(false)
        store.setSound(true)
        store.setPreview(false)
        store.setClipKeepOpen(true)
        store.resetAll()
        assertEquals(FeedbackPrefs(), store.read().feedback)
        assertEquals(emptySet<String>(), fake.keys())
    }

    @Test
    fun aNewStoreOnTheSameFileSeesTheSavedValues() {
        store.setHeight(130)
        store.setMode(PanelMode.ONE_HAND_LEFT)
        val restarted = KeyboardPrefsStore(fake.prefs).read()
        assertEquals(130, restarted.heightPercent)
        assertEquals(PanelMode.ONE_HAND_LEFT, restarted.mode)
    }

    @Test
    fun writesAreClampedToTheLimits() {
        store.setKeySize(500)
        store.setHeight(1)
        store.setOneHand(0)
        store.setSplitGap(99)
        store.setLabelScale(1000)
        val read = store.read()
        assertEquals(Limits.KEY_MAX, read.keySizePercent)
        assertEquals(Limits.HEIGHT_MIN, read.heightPercent)
        assertEquals(Limits.ONE_HAND_MIN, read.oneHandPercent)
        assertEquals(Limits.SPLIT_MAX, read.splitGapPercent)
        assertEquals(Limits.LABEL_MAX, read.label.scalePercent)
    }

    @Test
    fun outOfRangeAndUnknownRawValuesAreSanitizedOnRead() {
        raw {
            putInt("key_size", 5)
            putString("mode", "ghost")
            putString("label_font", "ghost")
            putString("language", "xx")
        }
        val read = store.read()
        assertEquals(Limits.KEY_MIN, read.keySizePercent)
        assertEquals(PanelMode.NORMAL, read.mode)
        assertEquals(LabelFont.PLEX, read.label.font)
        assertEquals(LayoutCatalog.default, read.language)
    }

    @Test
    fun unknownLanguageIsNeverSaved() {
        store.setLanguage("xx")
        assertEquals(LayoutCatalog.default, store.read().language)
    }

    @Test
    fun ordersAreKeptPerLayoutAndSurviveLanguageSwitching() {
        val ar = listOf(listOf("a", "b"), listOf("c"))
        val en = listOf(listOf("x"), listOf("y", "z"))
        store.setOrder("ar", ar)
        store.setOrder("en", en)
        store.setLanguage("en")
        store.setLanguage("ar")
        val read = store.read()
        assertEquals(ar, read.orders["ar"])
        assertEquals(en, read.orders["en"])
        assertEquals("ar", read.language)
    }

    @Test
    fun aSwappedLayoutSurvivesARestart() {
        val layout = sampleLayout()
        val swapped = layout.swapped(SlotRef(0, 0), SlotRef(2, 2))!!
        store.setOrder("ar", swapped)
        val saved = KeyboardPrefsStore(fake.prefs).read().orders.getValue("ar")
        assertEquals(swapped, layout.arranged(saved)!!.order())
    }

    @Test
    fun garbageOrderStillReadsAndIsRejectedByTheLayout() {
        raw { putString("order.ar", ";;,") }
        val order = store.read().orders["ar"]
        assertNotNull(order)
        assertNull(sampleLayout().arranged(order!!))
    }

    @Test
    fun resetOrderTouchesOnlyThatLayout() {
        store.setOrder("ar", listOf(listOf("a")))
        store.setOrder("en", listOf(listOf("b")))
        store.resetOrder("ar")
        val read = store.read()
        assertNull(read.orders["ar"])
        assertEquals(listOf(listOf("b")), read.orders["en"])
    }

    @Test
    fun resetToolbarRestoresTheDefaultOrder() {
        store.setToolbar(listOf(ToolbarItem.SYMBOLS, ToolbarItem.MODE, ToolbarItem.THEME))
        store.resetToolbar()
        assertEquals(ToolbarItem.entries, store.read().toolbar)
    }

    @Test
    fun resetAllClearsCustomizationButKeepsTheLanguage() {
        store.setKeySize(90)
        store.setHeight(120)
        store.setOneHand(70)
        store.setSplitGap(10)
        store.setLabelScale(90)
        store.setMode(PanelMode.SPLIT)
        store.setFont(LabelFont.SERIF)
        store.setBold(true)
        store.setItalic(true)
        store.setToolbar(listOf(ToolbarItem.SYMBOLS, ToolbarItem.MODE, ToolbarItem.THEME))
        store.setOrder("ar", listOf(listOf("a")))
        store.setOrder("en", listOf(listOf("b")))
        store.setLanguage("en")
        store.resetAll()
        assertEquals(KeyboardPrefs(language = "en"), store.read())
        assertEquals(setOf("language"), fake.keys())
    }

    @Test
    fun observersHearChangesUntilClosed() {
        var calls = 0
        val subscription = store.observe { calls++ }
        store.setBold(true)
        assertEquals(1, calls)
        subscription.close()
        store.setBold(false)
        assertEquals(1, calls)
    }

    @Test
    fun resettingAnEmptyStoreNotifiesNobody() {
        var calls = 0
        store.observe { calls++ }
        store.resetAll()
        assertEquals(0, calls)
    }

    @Test
    fun touchHardwareAndRememberSwitchesDefaultOnAndRoundTrip() {
        val typing = store.read().typing
        assertEquals(true, typing.touchAssist)
        assertEquals(true, typing.hardwareKeys)
        assertEquals(true, typing.rememberLanguage)
        assertEquals(true, typing.autoLanguage)
        store.setAutoLanguage(false)
        assertEquals(false, store.read().typing.autoLanguage)
        store.setTouchAssist(false)
        store.setHardwareKeys(false)
        store.setRememberLanguage(false)
        val changed = store.read().typing
        assertEquals(false, changed.touchAssist)
        assertEquals(false, changed.hardwareKeys)
        assertEquals(false, changed.rememberLanguage)
        store.resetAll()
        assertEquals(TypingPrefs(), store.read().typing)
    }

    @Test
    fun feedbackLevelsRoundTripAreClampedAndReset() {
        assertEquals(Limits.STRENGTH_DEFAULT, store.read().feedback.vibrateStrength)
        assertEquals(Limits.VOLUME_DEFAULT, store.read().feedback.soundVolume)
        store.setVibrateStrength(80)
        store.setSoundVolume(40)
        assertEquals(80, store.read().feedback.vibrateStrength)
        assertEquals(40, store.read().feedback.soundVolume)
        store.setVibrateStrength(1)
        store.setSoundVolume(900)
        assertEquals(Limits.STRENGTH_MIN, store.read().feedback.vibrateStrength)
        assertEquals(Limits.VOLUME_MAX, store.read().feedback.soundVolume)
        store.resetAll()
        assertEquals(FeedbackPrefs(), store.read().feedback)
        assertEquals(emptySet<String>(), fake.keys())
    }
}
