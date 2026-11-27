package com.spacekeyboard.keyboard.clipboard

import com.spacekeyboard.keyboard.clipboard.ui.ClipLabels
import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringReader
import java.nio.file.Files
import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipBusyTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val dir: File = Files.createTempDirectory("busy").toFile().also { it.deleteOnExit() }
    private val blobs = ClipBlobs(dir, key)
    private val roomy = 1L shl 40

    private fun session(): ClipSession =
        ClipSession().withSettings(true, UnlockWindow.FIVE_MINUTES).open(0).recordLarge("preview", 250_000, "b1", "d1", 1)

    private fun bigId(s: ClipSession): Long = s.book.items.single { it.blob != null }.id

    @Test
    fun meterReportsUnknownUntilTheTotalIsKnown() {
        val meter = Meter(BusyKind.SAVING)
        meter.done = 40
        assertEquals(Meter.UNKNOWN, meter.percent())
        meter.expected = 80
        assertEquals(50, meter.percent())
        meter.done = 500
        assertEquals(100, meter.percent())
    }

    @Test
    fun meterHandlesBillionsWithoutOverflow() {
        val meter = Meter(BusyKind.PASTING, ClipPolicy.TOTAL_CHARS)
        meter.done = ClipPolicy.TOTAL_CHARS / 4
        assertEquals(25, meter.percent())
        assertEquals(BusyKind.PASTING, meter.busy().kind)
        assertEquals(ClipPolicy.TOTAL_CHARS / 4, meter.busy().chars)
    }

    @Test
    fun countingReaderCountsEveryCharacter() {
        val meter = Meter(BusyKind.SAVING)
        val text = "مرحبا ".repeat(5000)
        val read = CountingReader(StringReader(text), meter).use { it.readText() }
        assertEquals(text, read)
        assertEquals(text.length.toLong(), meter.done)
    }

    @Test
    fun countingStreamCountsSingleAndBulkReads() {
        val meter = Meter(BusyKind.SAVING)
        val stream = CountingStream(ByteArrayInputStream(ByteArray(100) { it.toByte() }), meter)
        stream.read()
        stream.read(ByteArray(30), 0, 30)
        stream.readBytes()
        assertEquals(100L, meter.done)
    }

    @Test
    fun ingestReachesOneHundredPercentOnTheTextPath() {
        val text = "line of text\n".repeat(20_000)
        val meter = Meter(BusyKind.SAVING, text.length.toLong())
        val result = ClipIngest.run(CountingReader(StringReader(text), meter), blobs, roomy, meter)
        assertTrue(result is Intake.Large)
        assertEquals(100, meter.percent())
    }

    @Test
    fun stoppingTheMeterCancelsAndLeavesNoFiles() {
        val meter = Meter(BusyKind.SAVING)
        meter.stopped = true
        val result = ClipIngest.run(StringReader("x".repeat(100_000)), blobs, roomy, meter)
        assertEquals(Intake.Cancelled, result)
        assertEquals(0, dir.listFiles()?.size ?: 0)
    }

    @Test
    fun compactLabelsStayShort() {
        assertEquals("999", ClipLabels.compact(999))
        assertEquals("1K", ClipLabels.compact(1_000))
        assertEquals("1.5K", ClipLabels.compact(1_500))
        assertEquals("2.3M", ClipLabels.compact(2_345_678))
        assertEquals("250M", ClipLabels.compact(250_000_000))
        assertEquals("8B", ClipLabels.compact(8_000_000_000L))
    }

    @Test
    fun sharedFilesGetATextName() {
        val id = blobs.newId()
        assertTrue(ClipBlobs.isId(id))
        assertEquals("clip-" + id.take(6) + ".txt", ClipBlobs.nameFor(id))
        assertFalse(ClipBlobs.isId("../etc/passwd"))
        assertFalse(ClipBlobs.isId(id + "00"))
        assertFalse(ClipBlobs.isId(null))
    }

    @Test
    fun askingToPasteABigItemOpensAConfirmation() {
        val s = session()
        val asked = s.startView(bigId(s), 2).askPaste(bigId(s), 3)
        assertEquals(ClipConfirm.PasteLarge(bigId(s), 250_000), asked.confirm)
        assertNull(asked.viewing)
        assertEquals(s.book, asked.answer(false).book)
        assertNull(asked.answer(false).confirm)
        assertNull(asked.answer(true).confirm)
    }

    @Test
    fun smallItemsAndHiddenItemsNeverAskToPaste() {
        val small = session().record("plain", false, 2)
        val plain = small.book.items.single { it.blob == null }.id
        assertNull(small.askPaste(plain, 3).confirm)
        assertNull(small.askPaste(999, 3).confirm)
    }

    @Test
    fun lockedBigItemsCannotBeAskedAbout() {
        var s = session()
        val id = bigId(s)
        s = s.copy(book = s.book.addSection("vault"))
        s = s.startAssign(id, 3).assign("vault")
        s = s.copy(book = s.book.setSectionLocked("vault", true))
        assertNull(s.askPaste(id, 4).confirm)
    }

    @Test
    fun busyStateReachesTheSnapshotAndSurvivesClosing() {
        val busy = ClipBusy(BusyKind.SAVING, 40, 1234)
        val s = session().withBusy(busy)
        assertEquals(busy, s.snapshot(5).busy)
        assertEquals(busy, s.close().snapshot(5).busy)
        assertNull(s.withBusy(null).snapshot(5).busy)
        assertSame(s, s.withBusy(busy))
    }
}
