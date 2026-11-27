package com.spacekeyboard.keyboard.clipboard

import java.io.File
import java.io.StringReader
import java.nio.file.Files
import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipLargeTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val dir: File = Files.createTempDirectory("ingest").toFile().also { it.deleteOnExit() }
    private val blobs = ClipBlobs(dir, key)
    private val roomy = 1L shl 40

    private fun ingest(text: String, budget: Long = roomy): Intake = ClipIngest.run(StringReader(text), blobs, budget)

    private fun stored(large: Intake.Large): String = checkNotNull(blobs.reader(large.blob)).use { it.readText() }

    @Test
    fun smallTextStaysInline() {
        assertEquals(Intake.Inline("hello"), ingest("hello"))
        assertEquals(Intake.Inline("x".repeat(ClipPolicy.MAX_TEXT)), ingest("x".repeat(ClipPolicy.MAX_TEXT)))
        assertEquals(Intake.Blank, ingest("   \n "))
    }

    @Test
    fun bigTextGoesToAnEncryptedBlobWithAPreview() {
        val text = "مرحبا abc ".repeat(2000)
        val result = ingest(text) as Intake.Large
        assertEquals(text.take(ClipPolicy.MAX_TEXT), result.preview)
        assertEquals(text.length.toLong(), result.total)
        assertEquals(64, result.digest.length)
        assertEquals(text, stored(result))
    }

    @Test
    fun sameTextGivesTheSameDigest() {
        val text = "line of text\n".repeat(3000)
        val first = ingest(text) as Intake.Large
        val second = ingest(text) as Intake.Large
        assertEquals(first.digest, second.digest)
        assertEquals(first.total, second.total)
        assertFalse(first.blob == second.blob)
    }

    @Test
    fun surrogatePairsSurviveEveryBoundary() {
        val text = "a".repeat(ClipPolicy.MAX_TEXT - 1) + "😀" + "b".repeat(40_000) + "😀"
        val result = ingest(text) as Intake.Large
        assertEquals(ClipPolicy.MAX_TEXT - 1, result.preview.length)
        assertEquals(text, stored(result))
    }

    @Test
    fun textBeyondTheBudgetLeavesNothingBehind() {
        assertEquals(Intake.TooLarge, ingest("مرحبا abc ".repeat(2000), 1000L))
        assertEquals(0, dir.listFiles()?.size ?: 0)
    }

    @Test
    fun whitespaceOnlyBigTextIsDropped() {
        assertEquals(Intake.Blank, ingest(" ".repeat(ClipPolicy.MAX_TEXT * 3)))
        assertEquals(0, dir.listFiles()?.size ?: 0)
    }

    @Test
    fun chunksNeverSplitASurrogatePair() {
        val text = "123456789😀" + "x".repeat(25) + "😀😀"
        val chunks = ClipChunks(StringReader(text), 10)
        val parts = ArrayList<String>()
        while (true) parts += chunks.next() ?: break
        assertEquals(text, parts.joinToString(""))
        assertTrue(parts.none { it.last().isHighSurrogate() })
    }

    @Test
    fun largeItemsRoundTripThroughTheVaultCodec() {
        val book = ClipBook().add("small", 1).addLarge("preview", 5_000_000_000L, "abc123", "d".repeat(64), 2)
        val decoded = ClipCodec.decode(ClipCodec.encode(book))
        assertEquals(book, decoded)
        assertEquals(5_000_000_000L, decoded.items.first { it.blob != null }.length)
    }

    @Test
    fun oldVaultLinesStillLoad() {
        val item = ClipCodec.decode("N\t2\nI\t1\tfalse\t\t5\thello").items.single()
        assertEquals("hello", item.text)
        assertNull(item.blob)
        assertEquals(5L, item.length)
    }

    @Test
    fun repeatedBigCopyRefreshesTheSameItem() {
        val first = ClipBook().addLarge("p", 100, "b1", "dg", 1)
        val again = first.addLarge("p", 100, "b2", "dg", 5)
        assertEquals(1, again.items.size)
        assertEquals(5L, again.items.single().copiedAt)
        assertEquals("b1", again.items.single().blob)
    }

    @Test
    fun oldestBigItemsLeaveWhenTheTotalIsTooHigh() {
        var book = ClipBook().addLarge("p", ClipPolicy.TOTAL_CHARS, "b1", "d1", 1)
        book = book.addLarge("p", ClipPolicy.TOTAL_CHARS, "b2", "d2", 2)
        assertEquals(listOf("b2"), book.items.map { it.blob })
    }

    @Test
    fun bigItemsCannotBeEditedOrOverwritten() {
        val book = ClipBook().addLarge("preview", 99_999, "b1", "dg", 1)
        val id = book.items.single().id
        assertEquals(EditStatus.UNAVAILABLE, book.checkEdit(id, "other"))
        assertEquals(book, book.edit(id, "other"))
        val session = ClipSession(book = book, enabled = true)
        assertNull(session.startEdit(id, 1).editing)
        val payload = session.payload(id, 1)
        assertNotNull(payload)
        assertEquals("b1", payload?.blob)
        assertEquals(99_999L, payload?.length)
    }

    @Test
    fun recordLargeHonoursTheEnabledSwitch() {
        assertTrue(ClipSession(enabled = false).recordLarge("p", 10, "b", "d", 1).book.items.isEmpty())
        assertEquals(1, ClipSession(enabled = true).recordLarge("p", 10, "b", "d", 1).book.items.size)
    }
}
