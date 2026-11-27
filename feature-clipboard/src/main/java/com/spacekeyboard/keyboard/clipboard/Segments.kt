package com.spacekeyboard.keyboard.clipboard

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object Segments {
    const val PLAIN = 65_536
    const val TAG = 16
    const val CIPHER = PLAIN + TAG
    const val PREFIX = 7
    const val NONCE = 12
    const val KEY_BYTES = 32
    const val HEADER = NONCE + KEY_BYTES + TAG + PREFIX
    private const val TAG_BITS = 128
    private const val TRANSFORM = "AES/GCM/NoPadding"

    fun cipher(mode: Int, key: SecretKey, iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORM).apply { init(mode, key, GCMParameterSpec(TAG_BITS, iv)) }

    fun segmentIv(prefix: ByteArray, index: Int, last: Boolean): ByteArray {
        val iv = ByteArray(NONCE)
        System.arraycopy(prefix, 0, iv, 0, PREFIX)
        for (i in 0 until 4) iv[PREFIX + i] = (index ushr (24 - 8 * i)).toByte()
        iv[NONCE - 1] = if (last) 1 else 0
        return iv
    }
}

internal class SegmentWriter(private val out: OutputStream, wrapKey: SecretKey, random: SecureRandom) : OutputStream() {
    private val data = SecretKeySpec(ByteArray(Segments.KEY_BYTES).also(random::nextBytes), "AES")
    private val prefix = ByteArray(Segments.PREFIX).also(random::nextBytes)
    private val buffer = ByteArray(Segments.PLAIN)
    private var filled = 0
    private var index = 0
    private var emitted = 0L
    private var closed = false

    val size: Long get() = emitted + filled

    init {
        val nonce = ByteArray(Segments.NONCE).also(random::nextBytes)
        val wrapped = Segments.cipher(Cipher.ENCRYPT_MODE, wrapKey, nonce).doFinal(data.encoded)
        out.write(nonce)
        out.write(wrapped)
        out.write(prefix)
        emitted = Segments.HEADER.toLong()
    }

    override fun write(b: Int) = write(byteArrayOf(b.toByte()), 0, 1)

    override fun write(src: ByteArray, off: Int, len: Int) {
        var from = off
        var left = len
        while (left > 0) {
            if (filled == Segments.PLAIN) seal(false)
            val take = minOf(left, Segments.PLAIN - filled)
            System.arraycopy(src, from, buffer, filled, take)
            filled += take
            from += take
            left -= take
        }
    }

    override fun flush() = out.flush()

    override fun close() {
        if (closed) return
        closed = true
        seal(true)
        out.close()
    }

    private fun seal(last: Boolean) {
        val iv = Segments.segmentIv(prefix, index++, last)
        val sealed = Segments.cipher(Cipher.ENCRYPT_MODE, data, iv).doFinal(buffer, 0, filled)
        out.write(sealed)
        emitted += sealed.size
        filled = 0
    }
}

internal class SegmentReader(private val input: InputStream, wrapKey: SecretKey) : InputStream() {
    private var current = ByteArray(0)
    private var pos = 0
    private var ahead: ByteArray? = null
    private var index = 0
    private var finished = false
    private val data: SecretKey
    private val prefix: ByteArray

    init {
        val header = readBlock(Segments.HEADER)
        if (header.size != Segments.HEADER) throw IOException("truncated")
        val nonce = header.copyOfRange(0, Segments.NONCE)
        val unwrapped = try {
            Segments.cipher(Cipher.DECRYPT_MODE, wrapKey, nonce).doFinal(header, Segments.NONCE, Segments.KEY_BYTES + Segments.TAG)
        } catch (e: GeneralSecurityException) {
            throw IOException(e)
        }
        data = SecretKeySpec(unwrapped, "AES")
        prefix = header.copyOfRange(Segments.HEADER - Segments.PREFIX, Segments.HEADER)
    }

    override fun read(): Int {
        val one = ByteArray(1)
        return if (read(one, 0, 1) < 0) -1 else one[0].toInt() and 0xFF
    }

    override fun read(dst: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        while (pos >= current.size) {
            if (!fill()) return -1
        }
        val count = minOf(len, current.size - pos)
        System.arraycopy(current, pos, dst, off, count)
        pos += count
        return count
    }

    override fun close() = input.close()

    private fun fill(): Boolean {
        if (finished) return false
        val sealed = ahead ?: readBlock(Segments.CIPHER)
        if (sealed.isEmpty()) throw IOException("truncated")
        val next = if (sealed.size == Segments.CIPHER) readBlock(Segments.CIPHER) else ByteArray(0)
        ahead = next
        val last = next.isEmpty()
        val iv = Segments.segmentIv(prefix, index++, last)
        current = try {
            Segments.cipher(Cipher.DECRYPT_MODE, data, iv).doFinal(sealed)
        } catch (e: GeneralSecurityException) {
            throw IOException(e)
        }
        pos = 0
        finished = last
        return true
    }

    private fun readBlock(size: Int): ByteArray {
        val block = ByteArray(size)
        var got = 0
        while (got < size) {
            val n = input.read(block, got, size - got)
            if (n < 0) break
            got += n
        }
        return if (got == size) block else block.copyOf(got)
    }
}
