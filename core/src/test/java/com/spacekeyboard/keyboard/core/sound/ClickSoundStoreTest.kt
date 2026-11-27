package com.spacekeyboard.keyboard.core.sound

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClickSoundStoreTest {
    private val fake = FakePreferences()
    private val root: File = Files.createTempDirectory("clicksound").toFile()
    private val dir = File(root, "click_sound")
    private val store = ClickSoundStore(fake.prefs, dir)

    private fun putClip() {
        store.workDir()
        store.clipFile.writeText("wav")
    }

    @Test
    fun anEmptyStoreUsesTheSystemSound() {
        val choice = store.read()
        assertEquals(ClickSoundChoice.SYSTEM, choice)
        assertFalse(choice.custom)
        assertEquals(0L, store.activeStamp())
    }

    @Test
    fun theClipFileIsNamedAndPlacedInTheFolder() {
        assertEquals("click.wav", store.clipFile.name)
        assertEquals(dir, store.clipFile.parentFile)
    }

    @Test
    fun workDirCreatesTheFolder() {
        assertFalse(dir.exists())
        assertEquals(dir, store.workDir())
        assertTrue(dir.isDirectory)
    }

    @Test
    fun aCommittedClipWithItsFileIsCustom() {
        putClip()
        store.commit("Door knock", 480L)
        val choice = store.read()
        assertTrue(choice.custom)
        assertEquals("Door knock", choice.name)
        assertEquals(480L, choice.durationMs)
        assertNotEquals(0L, choice.stamp)
        assertEquals(choice.stamp, store.activeStamp())
    }

    @Test
    fun aCommitWithoutTheFileFallsBackToTheSystemSound() {
        store.commit("Gone", 300L)
        assertEquals(ClickSoundChoice.SYSTEM, store.read())
        assertNotEquals(0L, store.activeStamp())
    }

    @Test
    fun aMissingFileMakesTheChoiceSystemAndItsReturnMakesItCustomAgain() {
        putClip()
        store.commit("Tap", 120L)
        store.clipFile.delete()
        assertEquals(ClickSoundChoice.SYSTEM, store.read())
        putClip()
        assertEquals("Tap", store.read().name)
    }

    @Test
    fun aSecondCommitReplacesTheFirst() {
        putClip()
        store.commit("First", 100L)
        store.commit("Second", 250L)
        val choice = store.read()
        assertEquals("Second", choice.name)
        assertEquals(250L, choice.durationMs)
    }

    @Test
    fun clearRemovesTheChoiceAndTheFile() {
        putClip()
        store.commit("Tap", 120L)
        store.clear()
        assertEquals(ClickSoundChoice.SYSTEM, store.read())
        assertEquals(0L, store.activeStamp())
        assertFalse(store.clipFile.exists())
    }

    @Test
    fun clearOnAnEmptyStoreIsHarmless() {
        store.clear()
        assertEquals(ClickSoundChoice.SYSTEM, store.read())
        assertEquals(0L, store.activeStamp())
    }

    @Test
    fun aClearedStoreCanBeCommittedAgain() {
        putClip()
        store.commit("One", 100L)
        store.clear()
        putClip()
        store.commit("Two", 200L)
        assertEquals("Two", store.read().name)
    }

    @Test
    fun observersHearCommitsUntilTheyClose() {
        var calls = 0
        val subscription = store.observe { calls++ }
        putClip()
        store.commit("Tap", 120L)
        assertTrue(calls > 0)
        subscription.close()
        val before = calls
        store.commit("Other", 90L)
        assertEquals(before, calls)
    }

    @Test
    fun observersHearClear() {
        putClip()
        store.commit("Tap", 120L)
        var calls = 0
        val subscription = store.observe { calls++ }
        store.clear()
        assertTrue(calls > 0)
        subscription.close()
    }

    @Test
    fun installCopiesTheClipAndMakesItTheChoice() {
        assertTrue(store.install("RIFFdata".byteInputStream(), "Marimba", 130L))
        assertEquals("RIFFdata", store.clipFile.readText())
        val choice = store.read()
        assertTrue(choice.custom)
        assertEquals("Marimba", choice.name)
        assertEquals(130L, choice.durationMs)
        assertFalse(File(dir, "preset.tmp").exists())
    }

    @Test
    fun installReplacesAnEarlierClip() {
        putClip()
        store.commit("Old", 100L)
        assertTrue(store.install("new".byteInputStream(), "New", 90L))
        assertEquals("new", store.clipFile.readText())
        assertEquals("New", store.read().name)
    }

    @Test
    fun aFailedInstallKeepsTheEarlierChoice() {
        putClip()
        store.commit("Old", 100L)
        val broken = object : InputStream() {
            override fun read(): Int = throw IOException("boom")
        }
        assertFalse(store.install(broken, "New", 90L))
        assertEquals("Old", store.read().name)
        assertEquals("wav", store.clipFile.readText())
        assertFalse(File(dir, "preset.tmp").exists())
    }
}
