package com.spacekeyboard.keyboard.core.sound

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavFile {
    private const val HEADER_BYTES = 44
    private const val BITS = 16
    private const val PCM = 1

    fun header(dataBytes: Int, sampleRate: Int, channels: Int = 1): ByteArray {
        val blockAlign = channels * BITS / 8
        return ByteBuffer.allocate(HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(HEADER_BYTES - 8 + dataBytes)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(PCM.toShort())
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(sampleRate * blockAlign)
            putShort(blockAlign.toShort())
            putShort(BITS.toShort())
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataBytes)
        }.array()
    }

    fun write(out: OutputStream, samples: ShortArray, sampleRate: Int) {
        out.write(header(samples.size * 2, sampleRate))
        val body = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { body.putShort(it) }
        out.write(body.array())
    }
}
