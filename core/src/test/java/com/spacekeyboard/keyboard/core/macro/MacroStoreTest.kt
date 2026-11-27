package com.spacekeyboard.keyboard.core.macro

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import com.spacekeyboard.keyboard.core.vault.AesGcmCipher
import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroStoreTest {
    private val fake = FakePreferences()
    private val cipher = AesGcmCipher(KeyGenerator.getInstance("AES").apply { init(256) }.generateKey())
    private val store = MacroStore(fake.prefs, cipher)
    private val steps = listOf(MacroStep.Text("مرحبا, hi+there\n\t%"), MacroStep.Enter, MacroStep.Delete, MacroStep.Text("😀😀"))

    @Test
    fun codecRoundTripsAwkwardText() {
        assertEquals(steps, MacroCodec.decode(MacroCodec.encode(steps)))
    }

    @Test
    fun codecSkipsBrokenTokens() {
        assertEquals(listOf(MacroStep.Enter), MacroCodec.decode("x,t,e,%"))
    }

    @Test
    fun addedRecordingsComeBackInOrderWithRisingNumbers() {
        val first = store.add(steps)!!
        val second = store.add(listOf(MacroStep.Enter))!!
        assertEquals(listOf(first.id, second.id), store.all().map { it.id })
        assertEquals(steps, store.all().first().steps)
        assertTrue(second.id > first.id)
    }

    @Test
    fun deletedNumbersAreNotReused() {
        val first = store.add(steps)!!
        store.delete(first.id)
        assertEquals(emptyList<Long>(), store.all().map { it.id })
        assertTrue(store.add(steps)!!.id > first.id)
    }

    @Test
    fun emptyRecordingIsNotSaved() {
        assertNull(store.add(emptyList()))
        assertEquals(0, store.all().size)
    }

    @Test
    fun libraryIsCapped() {
        repeat(MacroStore.MAX_MACROS) { store.add(steps) }
        assertNull(store.add(steps))
        assertEquals(MacroStore.MAX_MACROS, store.all().size)
    }

    @Test
    fun speedIsRemembered() {
        assertEquals(MacroSpeed.NORMAL, store.speed)
        store.speed = MacroSpeed.TURBO
        assertEquals(MacroSpeed.TURBO, store.speed)
    }

    @Test
    fun previewShowsTextAndKeys() {
        val macro = Macro(1, listOf(MacroStep.Text("hi"), MacroStep.Enter, MacroStep.Text("yo")))
        assertEquals("hi ⏎ yo", macro.preview())
    }

    @Test
    fun liveClipStepRoundTripsThroughTheCodec() {
        val mixed = listOf(MacroStep.Text("a"), MacroStep.LiveClip, MacroStep.Enter)
        assertEquals(mixed, MacroCodec.decode(MacroCodec.encode(mixed)))
    }

    @Test
    fun nameIsOptionalTrimmedAndLimited() {
        val plain = store.add(steps)!!
        val named = store.add(steps, "  Work reply  ")!!
        val blank = store.add(steps, "   ")!!
        val long = store.add(steps, "x".repeat(100))!!
        assertNull(plain.name)
        assertEquals("Work reply", named.name)
        assertNull(blank.name)
        assertEquals(MacroStore.MAX_NAME, long.name!!.length)
        assertEquals(listOf(null, "Work reply", null, "x".repeat(MacroStore.MAX_NAME)), store.all().map { it.name })
    }

    @Test
    fun renameKeepsStepsAndBlankGoesBackToTheNumber() {
        val macro = store.add(steps, "Old")!!
        store.rename(macro.id, "رد سريع")
        assertEquals("رد سريع", store.all().single().name)
        assertEquals(steps, store.all().single().steps)
        store.rename(macro.id, "  ")
        assertNull(store.all().single().name)
    }

    @Test
    fun savedDataHoldsNoReadableText() {
        store.add(listOf(MacroStep.Text("secret-phrase")), "Private-name")
        val stored = fake.prefs.all.values.joinToString("|")
        assertFalse(stored.contains("secret"))
        assertFalse(stored.contains("Private"))
        assertEquals("secret-phrase", (store.all().single().steps.single() as MacroStep.Text).text)
    }

    @Test
    fun anotherKeyCannotReadTheVault() {
        store.add(steps, "Mine")
        val other = AesGcmCipher(KeyGenerator.getInstance("AES").apply { init(256) }.generateKey())
        assertEquals(emptyList<Macro>(), MacroStore(fake.prefs, other).all())
        assertEquals(1, store.all().size)
    }

    @Test
    fun oldPlainRecordingsMoveIntoTheVaultWithoutLoss() {
        val legacy = "3\t" + MacroCodec.encode(steps) + "\n7\t" + MacroCodec.encode(listOf(MacroStep.Enter))
        fake.prefs.edit().putString("items", legacy).putLong("seq", 7L).apply()
        val first = store.all()
        assertEquals(listOf(3L, 7L), first.map { it.id })
        assertEquals(steps, first.first().steps)
        assertTrue("items" !in fake.keys())
        assertTrue("vault" in fake.keys())
        assertEquals(first.map { it.id to it.steps }, MacroStore(fake.prefs, cipher).all().map { it.id to it.steps })
        assertEquals(8L, store.add(steps)!!.id)
    }

    @Test
    fun oldRecordingsSurviveWhenNoKeyIsAvailable() {
        val legacy = "3\t" + MacroCodec.encode(steps)
        fake.prefs.edit().putString("items", legacy).apply()
        val locked = MacroStore(fake.prefs, null)
        assertEquals(listOf(3L), locked.all().map { it.id })
        assertTrue("items" in fake.keys())
        assertNull(locked.add(steps))
        assertEquals(listOf(3L), store.all().map { it.id })
    }

    @Test
    fun favoriteIsOneRecordingAndGoesWhenItIsDeleted() {
        val first = store.add(steps)!!
        val second = store.add(steps)!!
        assertNull(store.favorite())
        store.setFavorite(first.id)
        store.setFavorite(second.id)
        assertEquals(second.id, store.favorite()!!.id)
        store.setFavorite(99L)
        assertNull(store.favorite())
        store.setFavorite(second.id)
        store.delete(second.id)
        assertNull(store.favoriteId)
        assertEquals(listOf(first.id), store.all().map { it.id })
    }

    @Test
    fun clearRemovesRecordingsAndFavoriteButKeepsSettings() {
        val macro = store.add(steps)!!
        store.setFavorite(macro.id)
        store.repeat = MacroRepeat.FIVE
        store.clear()
        assertEquals(emptyList<Macro>(), store.all())
        assertNull(store.favorite())
        assertEquals(MacroRepeat.FIVE, store.repeat)
    }

    @Test
    fun repeatIsRememberedAndDefaultsToNoEnd() {
        assertEquals(MacroRepeat.FOREVER, store.repeat)
        store.repeat = MacroRepeat.THREE
        assertEquals(MacroRepeat.THREE, store.repeat)
        assertEquals(MacroRepeat.FOREVER, MacroRepeat.byId("bogus"))
    }
}
