package com.spacekeyboard.keyboard.clipboard

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Reader
import java.security.SecureRandom
import javax.crypto.SecretKey

class BlobWriter internal constructor(
    private val segments: SegmentWriter,
    private val part: File,
    private val target: File,
) {
    val stream: OutputStream get() = segments

    val bytes: Long get() = segments.size

    fun commit(): Boolean = runCatching {
        segments.close()
        check(part.renameTo(target))
    }.isSuccess

    fun abort() {
        runCatching { segments.close() }
        part.delete()
    }
}

class ClipBlobs(
    private val dir: File,
    private val key: SecretKey,
    private val random: SecureRandom = SecureRandom(),
) {
    fun newId(): String = ByteArray(ID_BYTES).also(random::nextBytes).joinToString("") { "%02x".format(it) }

    fun file(id: String): File = File(dir, id + BLOB)

    fun begin(id: String): BlobWriter? = runCatching {
        check(dir.isDirectory || dir.mkdirs())
        val part = File(dir, id + PART)
        val sink = BufferedOutputStream(FileOutputStream(part), BUFFER)
        BlobWriter(SegmentWriter(sink, key, random), part, file(id))
    }.getOrNull()

    fun open(id: String): InputStream? {
        val raw = runCatching { BufferedInputStream(FileInputStream(file(id)), BUFFER) }.getOrNull() ?: return null
        return try {
            SegmentReader(raw, key)
        } catch (_: Exception) {
            raw.close()
            null
        }
    }

    fun reader(id: String): Reader? = open(id)?.let { InputStreamReader(it, Charsets.UTF_8) }

    fun readText(id: String, limit: Int): String? {
        val source = reader(id) ?: return null
        return runCatching { source.use { readUpTo(it, limit) } }.getOrNull()
    }

    fun delete(id: String) {
        file(id).delete()
    }

    fun sweep(keep: Set<String>, now: Long) {
        dir.listFiles()?.forEach { entry ->
            val stale = now - entry.lastModified() > ClipPolicy.BLOB_GRACE_MS
            val orphan = entry.name.endsWith(BLOB) && entry.name.removeSuffix(BLOB) !in keep
            if (stale && (orphan || entry.name.endsWith(PART))) entry.delete()
        }
    }

    fun clear() {
        dir.deleteRecursively()
    }

    private fun readUpTo(source: Reader, limit: Int): String {
        val buffer = CharArray(limit)
        var count = 0
        while (count < limit) {
            val got = source.read(buffer, count, limit - count)
            if (got < 0) break
            count += got
        }
        return String(buffer, 0, count)
    }

    companion object {
        private const val ID_BYTES = 16
        private const val BUFFER = 65_536
        private const val BLOB = ".blob"
        private const val PART = ".part"
        private const val NAME_ID = 6
        private val ID_FORMAT = Regex("[0-9a-f]{${ID_BYTES * 2}}")

        fun isId(value: String?): Boolean = value != null && ID_FORMAT.matches(value)

        fun nameFor(id: String): String = "clip-" + id.take(NAME_ID) + ".txt"
    }
}
