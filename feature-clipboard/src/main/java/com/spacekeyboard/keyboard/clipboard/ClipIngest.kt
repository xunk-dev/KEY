package com.spacekeyboard.keyboard.clipboard

import java.io.OutputStreamWriter
import java.io.Reader
import java.security.DigestOutputStream
import java.security.MessageDigest

internal sealed interface Intake {
    data class Inline(val text: String) : Intake

    class Large(val preview: String, val total: Long, val blob: String, val digest: String) : Intake

    data object Blank : Intake

    data object TooLarge : Intake

    data object Cancelled : Intake

    data object Failed : Intake
}

internal object ClipIngest {
    private const val CHUNK = 16_384

    fun run(source: Reader, blobs: ClipBlobs, budget: Long, meter: Meter? = null): Intake =
        runCatching { source.use { read(it, blobs, budget, meter) } }.getOrDefault(Intake.Failed)

    private fun read(source: Reader, blobs: ClipBlobs, budget: Long, meter: Meter?): Intake {
        val head = StringBuilder()
        val chunk = CharArray(CHUNK)
        while (head.length <= ClipPolicy.MAX_TEXT) {
            val n = source.read(chunk)
            if (n < 0) return if (head.isBlank()) Intake.Blank else Intake.Inline(head.toString())
            head.append(chunk, 0, n)
        }
        return spill(head, source, chunk, blobs, budget, meter)
    }

    private fun spill(head: StringBuilder, source: Reader, chunk: CharArray, blobs: ClipBlobs, budget: Long, meter: Meter?): Intake {
        val id = blobs.newId()
        val writer = blobs.begin(id) ?: return Intake.Failed
        return try {
            store(head, source, chunk, writer, budget, id, meter)
        } catch (_: Exception) {
            writer.abort()
            Intake.Failed
        }
    }

    private fun store(head: StringBuilder, source: Reader, chunk: CharArray, writer: BlobWriter, budget: Long, id: String, meter: Meter?): Intake {
        val digest = MessageDigest.getInstance("SHA-256")
        val sink = OutputStreamWriter(DigestOutputStream(writer.stream, digest), Charsets.UTF_8)
        var total = head.length.toLong()
        var visible = head.any { !it.isWhitespace() }
        sink.write(head.toString())
        var n = source.read(chunk)
        while (n >= 0) {
            sink.write(chunk, 0, n)
            total += n
            visible = visible || (0 until n).any { !chunk[it].isWhitespace() }
            if (writer.bytes > budget) return abandon(writer, Intake.TooLarge)
            if (meter?.stopped == true) return abandon(writer, Intake.Cancelled)
            n = source.read(chunk)
        }
        sink.close()
        if (writer.bytes > budget) return abandon(writer, Intake.TooLarge)
        if (!visible) return abandon(writer, Intake.Blank)
        if (!writer.commit()) return abandon(writer, Intake.Failed)
        return Intake.Large(cut(head), total, id, hex(digest.digest()))
    }

    private fun abandon(writer: BlobWriter, result: Intake): Intake {
        writer.abort()
        return result
    }

    private fun cut(head: StringBuilder): String {
        val max = ClipPolicy.MAX_TEXT
        return head.substring(0, if (head[max - 1].isHighSurrogate()) max - 1 else max)
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }
}

internal class ClipChunks(private val source: Reader, private val size: Int) {
    private val buffer = CharArray(size)
    private var carry = ""

    fun next(): String? {
        var count = 0
        carry.forEach { buffer[count++] = it }
        carry = ""
        while (count < size) {
            val got = source.read(buffer, count, size - count)
            if (got < 0) break
            count += got
        }
        if (count == 0) return null
        if (count == size && buffer[count - 1].isHighSurrogate()) {
            carry = buffer[count - 1].toString()
            count--
        }
        return String(buffer, 0, count)
    }
}
