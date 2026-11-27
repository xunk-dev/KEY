package com.spacekeyboard.keyboard.core.sound

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object StagedPeaks {
    fun sidecar(file: File): File = File(file.path + ".peaks")

    fun write(file: File, peaks: FloatArray) {
        val body = ByteBuffer.allocate(peaks.size * BYTES).order(ByteOrder.LITTLE_ENDIAN)
        peaks.forEach { body.putFloat(it) }
        try {
            sidecar(file).writeBytes(body.array())
        } catch (_: IOException) {
            delete(file)
        }
    }

    fun read(file: File): FloatArray? {
        val target = sidecar(file)
        if (!target.isFile) return null
        return try {
            val data = ByteBuffer.wrap(target.readBytes()).order(ByteOrder.LITTLE_ENDIAN)
            if (data.remaining() == 0 || data.remaining() % BYTES != 0) null else FloatArray(data.remaining() / BYTES) { data.getFloat() }
        } catch (_: IOException) {
            null
        }
    }

    fun delete(file: File) {
        sidecar(file).delete()
    }

    private const val BYTES = 4
}
