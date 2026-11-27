package com.spacekeyboard.keyboard.core.sound

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class PcmSampleTest {
    private val pcm8 = 3
    private val pcm16 = 2
    private val pcmFloat = 4
    private val pcm24 = 21
    private val pcm32 = 22

    private fun buffer(size: Int = 8): ByteBuffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)

    @Test
    fun widthsFollowTheEncoding() {
        assertEquals(1, PcmSample.width(pcm8))
        assertEquals(2, PcmSample.width(pcm16))
        assertEquals(3, PcmSample.width(pcm24))
        assertEquals(4, PcmSample.width(pcm32))
        assertEquals(4, PcmSample.width(pcmFloat))
    }

    @Test
    fun unknownEncodingsHaveNoWidth() {
        assertEquals(0, PcmSample.width(0))
        assertEquals(0, PcmSample.width(99))
        assertEquals(0, PcmSample.width(-1))
    }

    @Test
    fun theDefaultIsSixteenBit() {
        assertEquals(pcm16, PcmSample.DEFAULT)
    }

    @Test
    fun sixteenBitSamplesReadAsTheyAre() {
        val data = buffer().putShort(0, -1_234).putShort(2, 32_000)
        assertEquals(-1_234f, PcmSample.read(data, 0, pcm16), 0f)
        assertEquals(32_000f, PcmSample.read(data, 2, pcm16), 0f)
    }

    @Test
    fun eightBitSamplesAreUnsignedAroundTheMiddle() {
        val data = buffer().put(0, 0x80.toByte()).put(1, 0xFF.toByte()).put(2, 0x00)
        assertEquals(0f, PcmSample.read(data, 0, pcm8), 0f)
        assertEquals(32_512f, PcmSample.read(data, 1, pcm8), 0f)
        assertEquals(-32_768f, PcmSample.read(data, 2, pcm8), 0f)
    }

    @Test
    fun twentyFourBitSamplesAreScaledToSixteen() {
        val data = buffer().put(0, 0x00).put(1, 0x00).put(2, 0x40)
        assertEquals(16_384f, PcmSample.read(data, 0, pcm24), 0f)
    }

    @Test
    fun twentyFourBitNegativesKeepTheirSign() {
        val data = buffer().put(0, 0x00).put(1, 0x00).put(2, 0xFF.toByte())
        assertEquals(-256f, PcmSample.read(data, 0, pcm24), 0f)
    }

    @Test
    fun twentyFourBitSamplesUseAllThreeBytes() {
        val data = buffer().put(0, 0x00).put(1, 0x01).put(2, 0x00)
        assertEquals(1f, PcmSample.read(data, 0, pcm24), 0f)
    }

    @Test
    fun thirtyTwoBitSamplesAreScaledToSixteen() {
        val data = buffer().putInt(0, 0x40000000).putInt(4, -0x40000000)
        assertEquals(16_384f, PcmSample.read(data, 0, pcm32), 0f)
        assertEquals(-16_384f, PcmSample.read(data, 4, pcm32), 0f)
    }

    @Test
    fun floatSamplesAreScaledToSixteen() {
        val data = buffer().putFloat(0, 0.5f).putFloat(4, -1f)
        assertEquals(16_383.5f, PcmSample.read(data, 0, pcmFloat), 0f)
        assertEquals(-32_767f, PcmSample.read(data, 4, pcmFloat), 0f)
    }

    @Test
    fun readingAnUnknownEncodingGivesSilence() {
        val data = buffer().putShort(0, 1_000)
        assertEquals(0f, PcmSample.read(data, 0, 99), 0f)
    }
}
