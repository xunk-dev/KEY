package com.spacekeyboard.keyboard.core.sound

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class WavFileTest {
    private fun little(bytes: ByteArray): ByteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

    private fun text(bytes: ByteArray, from: Int): String = String(bytes, from, 4, Charsets.US_ASCII)

    @Test
    fun theHeaderIsFortyFourBytes() {
        assertEquals(44, WavFile.header(0, 44_100).size)
        assertEquals(44, WavFile.header(1_000_000, 8_000, 2).size)
    }

    @Test
    fun theHeaderNamesTheFourChunks() {
        val header = WavFile.header(1_000, 44_100)
        assertEquals("RIFF", text(header, 0))
        assertEquals("WAVE", text(header, 8))
        assertEquals("fmt ", text(header, 12))
        assertEquals("data", text(header, 36))
    }

    @Test
    fun theSizesCountTheData() {
        val header = little(WavFile.header(1_000, 44_100))
        assertEquals(1_036, header.getInt(4))
        assertEquals(1_000, header.getInt(40))
    }

    @Test
    fun theHeaderDescribesMonoSixteenBitPcm() {
        val header = little(WavFile.header(1_000, 44_100))
        assertEquals(16, header.getInt(16))
        assertEquals(1, header.getShort(20).toInt())
        assertEquals(1, header.getShort(22).toInt())
        assertEquals(44_100, header.getInt(24))
        assertEquals(88_200, header.getInt(28))
        assertEquals(2, header.getShort(32).toInt())
        assertEquals(16, header.getShort(34).toInt())
    }

    @Test
    fun numbersAreLittleEndian() {
        val header = WavFile.header(1_000, 44_100)
        assertArrayEquals(byteArrayOf(0x0C, 0x04, 0x00, 0x00), header.copyOfRange(4, 8))
        assertArrayEquals(byteArrayOf(0x44, 0xAC.toByte(), 0x00, 0x00), header.copyOfRange(24, 28))
        assertArrayEquals(byteArrayOf(0xE8.toByte(), 0x03, 0x00, 0x00), header.copyOfRange(40, 44))
    }

    @Test
    fun stereoDoublesTheBlockAlignAndByteRate() {
        val header = little(WavFile.header(400, 44_100, 2))
        assertEquals(2, header.getShort(22).toInt())
        assertEquals(176_400, header.getInt(28))
        assertEquals(4, header.getShort(32).toInt())
    }

    @Test
    fun byteRateFollowsTheSampleRate() {
        assertEquals(16_000, little(WavFile.header(0, 8_000)).getInt(28))
        assertEquals(96_000, little(WavFile.header(0, 48_000)).getInt(28))
        assertEquals(48_000, little(WavFile.header(0, 48_000)).getInt(24))
    }

    @Test
    fun noDataGivesARiffSizeOfThirtySix() {
        val header = little(WavFile.header(0, 44_100))
        assertEquals(36, header.getInt(4))
        assertEquals(0, header.getInt(40))
    }

    @Test
    fun writeAppendsTheSamplesAfterTheHeader() {
        val out = ByteArrayOutputStream()
        WavFile.write(out, shortArrayOf(1, -2, 256, -32_768, 32_767), 22_050)
        val bytes = out.toByteArray()
        assertEquals(54, bytes.size)
        assertEquals(10, little(bytes).getInt(40))
        assertEquals(46, little(bytes).getInt(4))
        assertEquals(22_050, little(bytes).getInt(24))
        val expected = byteArrayOf(
            0x01, 0x00, 0xFE.toByte(), 0xFF.toByte(), 0x00, 0x01,
            0x00, 0x80.toByte(), 0xFF.toByte(), 0x7F,
        )
        assertArrayEquals(expected, bytes.copyOfRange(44, 54))
    }

    @Test
    fun writeOfNoSamplesIsJustTheHeader() {
        val out = ByteArrayOutputStream()
        WavFile.write(out, ShortArray(0), 44_100)
        val bytes = out.toByteArray()
        assertEquals(44, bytes.size)
        assertEquals(0, little(bytes).getInt(40))
        assertEquals(36, little(bytes).getInt(4))
    }

    @Test
    fun writtenHeaderMatchesTheHeaderFunction() {
        val out = ByteArrayOutputStream()
        WavFile.write(out, ShortArray(3), 16_000)
        assertArrayEquals(WavFile.header(6, 16_000), out.toByteArray().copyOfRange(0, 44))
    }
}
