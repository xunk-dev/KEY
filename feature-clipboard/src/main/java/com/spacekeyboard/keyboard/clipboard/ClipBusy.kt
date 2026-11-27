package com.spacekeyboard.keyboard.clipboard

import java.io.FilterInputStream
import java.io.InputStream
import java.io.Reader

enum class BusyKind { SAVING, PASTING }

data class ClipBusy(val kind: BusyKind, val percent: Int, val chars: Long)

internal class Meter(val kind: BusyKind, total: Long = 0L) {
    @Volatile
    var expected = total

    @Volatile
    var done = 0L

    @Volatile
    var stopped = false

    fun percent(): Int = if (expected <= 0L) UNKNOWN else (done * 100 / expected).toInt().coerceIn(0, 100)

    fun busy(): ClipBusy = ClipBusy(kind, percent(), done)

    companion object {
        const val UNKNOWN = -1
    }
}

internal class CountingReader(private val inner: Reader, private val meter: Meter) : Reader() {
    override fun read(buffer: CharArray, offset: Int, length: Int): Int {
        val got = inner.read(buffer, offset, length)
        if (got > 0) meter.done += got
        return got
    }

    override fun close() = inner.close()
}

internal class CountingStream(source: InputStream, private val meter: Meter) : FilterInputStream(source) {
    override fun read(): Int {
        val value = super.read()
        if (value >= 0) meter.done += 1
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val got = super.read(buffer, offset, length)
        if (got > 0) meter.done += got
        return got
    }
}
