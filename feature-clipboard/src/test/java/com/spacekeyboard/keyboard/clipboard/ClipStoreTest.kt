package com.spacekeyboard.keyboard.clipboard

import java.io.File
import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipStoreTest {
    private fun cipher() = AesGcmCipher(KeyGenerator.getInstance("AES").apply { init(256) }.generateKey())

    private fun tempFile(): File = File.createTempFile("clip", ".vault").also { it.delete(); it.deleteOnExit() }

    @Test
    fun missingFileLoadsAnEmptyBook() {
        assertEquals(ClipBook(), ClipStore(tempFile(), cipher()).load())
    }

    @Test
    fun savedBookRoundTripsAndNeverContainsPlainText() {
        val file = tempFile()
        val aes = cipher()
        var book = ClipBook().addSection("vault").add("كلمة سر 123", 5).add("second", 6)
        book = book.moveToSection(book.items[0].id, "vault").setSectionLocked("vault", true)
        assertTrue(ClipStore(file, aes).save(book))
        val raw = file.readText(Charsets.US_ASCII)
        assertTrue(raw.matches(Regex("[0-9a-f]+")))
        assertEquals(book, ClipStore(file, aes).load())
        file.delete()
    }

    @Test
    fun wrongKeyOrDamagedFileLoadsNull() {
        val file = tempFile()
        assertTrue(ClipStore(file, cipher()).save(ClipBook().add("x", 1)))
        assertNull(ClipStore(file, cipher()).load())
        file.writeText("not hex at all")
        assertNull(ClipStore(file, cipher()).load())
        file.writeText("")
        assertNull(ClipStore(file, cipher()).load())
        file.delete()
    }

    @Test
    fun savingTwiceReplacesTheFileAndLeavesNoTempFile() {
        val file = tempFile()
        val store = ClipStore(file, cipher())
        assertTrue(store.save(ClipBook().add("one", 1)))
        assertTrue(store.save(ClipBook().add("two", 2)))
        assertEquals(listOf("two"), store.load()?.items?.map { it.text })
        assertFalse(File(file.path + ".tmp").exists())
        file.delete()
    }

    @Test
    fun savingIntoAMissingFolderFailsQuietly() {
        val file = File(tempFile().parentFile, "missing-folder/clip.vault")
        assertFalse(ClipStore(file, cipher()).save(ClipBook().add("x", 1)))
    }
}
