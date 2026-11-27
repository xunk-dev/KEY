package com.spacekeyboard.keyboard.core.sound

import android.media.AudioFormat
import java.nio.ByteBuffer

internal object PcmSample {
    const val DEFAULT = AudioFormat.ENCODING_PCM_16BIT
    private const val ENCODING_24_PACKED = 21
    private const val ENCODING_32 = 22
    private const val BYTE_RANGE = 256f
    private const val SHORT_RANGE = 65536f
    private const val FULL_SCALE = 32767f

    fun width(encoding: Int): Int = when (encoding) {
        AudioFormat.ENCODING_PCM_8BIT -> 1
        AudioFormat.ENCODING_PCM_16BIT -> 2
        ENCODING_24_PACKED -> 3
        ENCODING_32, AudioFormat.ENCODING_PCM_FLOAT -> 4
        else -> 0
    }

    fun read(buffer: ByteBuffer, at: Int, encoding: Int): Float = when (encoding) {
        AudioFormat.ENCODING_PCM_8BIT -> ((buffer.get(at).toInt() and 0xFF) - 128) * BYTE_RANGE
        AudioFormat.ENCODING_PCM_16BIT -> buffer.getShort(at).toFloat()
        ENCODING_24_PACKED -> packed24(buffer, at) / BYTE_RANGE
        ENCODING_32 -> buffer.getInt(at) / SHORT_RANGE
        AudioFormat.ENCODING_PCM_FLOAT -> buffer.getFloat(at) * FULL_SCALE
        else -> 0f
    }

    private fun packed24(buffer: ByteBuffer, at: Int): Int =
        (buffer.get(at).toInt() and 0xFF) or
            ((buffer.get(at + 1).toInt() and 0xFF) shl 8) or
            (buffer.get(at + 2).toInt() shl 16)
}
