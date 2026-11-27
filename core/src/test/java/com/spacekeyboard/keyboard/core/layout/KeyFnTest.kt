package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyFnTest {
    private val fake = FakePreferences()
    private val store = KeyboardPrefsStore(fake.prefs)

    @Test
    fun aChosenFunctionReplacesTheKeyAndKeepsItsPlace() {
        val layout = sampleLayout().withFunctions(mapOf("h" to "emoji"), false)
        val key = layout.key(SlotRef(2, 0))!!
        assertEquals("h", key.id)
        assertEquals(KeyAction.EMOJI, key.action)
        assertEquals(layout.order(), sampleLayout().order())
    }

    @Test
    fun theSpaceBarAndFixedKeysIgnoreAChoice() {
        val base = sampleLayout()
        val layout = base.withFunctions(mapOf("space" to "emoji", "pad" to "emoji"), false)
        assertEquals(base, layout)
    }

    @Test
    fun anUnknownFunctionChangesNothing() {
        val base = sampleLayout()
        assertSame(base, base.withFunctions(emptyMap(), false))
        assertEquals(base, base.withFunctions(mapOf("h" to "nope"), false))
    }

    @Test
    fun theCommaFollowsTheDirectionOfTheLanguage() {
        assertEquals("،", KeyFn.COMMA.label(true))
        assertEquals(",", KeyFn.COMMA.label(false))
    }

    @Test
    fun choicesSurviveRoundTripAndReset() {
        store.setKeyFn("ar", "num", KeyFn.EMOJI)
        store.setKeyFn("ar", "sym_open", KeyFn.CLIPBOARD)
        assertEquals(mapOf("num" to "emoji", "sym_open" to "clipboard"), store.read().keyFns["ar"])
        store.setKeyFn("ar", "num", null)
        assertEquals(mapOf("sym_open" to "clipboard"), store.read().keyFns["ar"])
        store.resetOrder("ar")
        assertTrue(store.read().keyFns.isEmpty())
    }

    @Test
    fun resetAllClearsEveryLayout() {
        store.setKeyFn("en", "num", KeyFn.TRANSLATE)
        store.resetAll()
        assertTrue(store.read().keyFns.isEmpty())
    }
}
