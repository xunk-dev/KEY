package com.spacekeyboard.keyboard.clipboard

import java.io.File
import java.io.IOException
import java.nio.file.Files
import javax.crypto.KeyGenerator
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipBlobsTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    private fun folder(): File = Files.createTempDirectory("blobs").toFile().also { it.deleteOnExit() }

    private fun sample(size: Int) = ByteArray(size) { (it * 31 + 7).toByte() }

    private fun put(blobs: ClipBlobs, data: ByteArray): String {
        val id = blobs.newId()
        val writer = checkNotNull(blobs.begin(id))
        writer.stream.write(data)
        assertTrue(writer.commit())
        return id
    }

    private fun readAll(blobs: ClipBlobs, id: String): ByteArray = checkNotNull(blobs.open(id)).use { it.readBytes() }

    @Test
    fun roundTripsAcrossSegmentSizes() {
        val blobs = ClipBlobs(folder(), key)
        val size = Segments.PLAIN
        listOf(0, 1, size - 1, size, size + 1, size * 3, size * 2 + 123).forEach { length ->
            val data = sample(length)
            assertArrayEquals("size $length", data, readAll(blobs, put(blobs, data)))
        }
    }

    @Test
    fun fileNeverHoldsPlainText() {
        val blobs = ClipBlobs(folder(), key)
        val id = put(blobs, "TOP-SECRET-MARKER".repeat(50).toByteArray())
        assertFalse(String(blobs.file(id).readBytes(), Charsets.ISO_8859_1).contains("TOP-SECRET"))
    }

    @Test
    fun droppedOrChangedSegmentsAreDetected() {
        val blobs = ClipBlobs(folder(), key)
        val id = put(blobs, sample(Segments.PLAIN * 3))
        val file = blobs.file(id)
        val whole = file.readBytes()
        file.writeBytes(whole.copyOf(whole.size - Segments.CIPHER))
        assertThrows(IOException::class.java) { readAll(blobs, id) }
        val flipped = whole.copyOf()
        flipped[whole.size / 2] = (flipped[whole.size / 2] + 1).toByte()
        file.writeBytes(flipped)
        assertThrows(IOException::class.java) { readAll(blobs, id) }
    }

    @Test
    fun wrongKeyOpensNothing() {
        val dir = folder()
        val id = put(ClipBlobs(dir, key), sample(100))
        val other = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        assertNull(ClipBlobs(dir, other).open(id))
    }

    @Test
    fun readTextStopsAtTheLimit() {
        val blobs = ClipBlobs(folder(), key)
        val id = put(blobs, "abcdefghij".repeat(100).toByteArray())
        assertEquals("abcdefghijabc", blobs.readText(id, 13))
        assertEquals(1000, blobs.readText(id, 5000)?.length)
    }

    @Test
    fun sweepRemovesOnlyOldUnreferencedFiles() {
        val blobs = ClipBlobs(folder(), key)
        val keep = put(blobs, sample(10))
        val drop = put(blobs, sample(10))
        blobs.sweep(setOf(keep), System.currentTimeMillis())
        assertTrue(blobs.file(drop).exists())
        blobs.sweep(setOf(keep), System.currentTimeMillis() + 3_600_000L)
        assertTrue(blobs.file(keep).exists())
        assertFalse(blobs.file(drop).exists())
    }
}
