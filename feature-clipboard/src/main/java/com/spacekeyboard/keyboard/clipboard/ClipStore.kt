package com.spacekeyboard.keyboard.clipboard

import com.spacekeyboard.keyboard.core.vault.AesGcmCipher
import java.io.File

class ClipStore(private val file: File, private val cipher: AesGcmCipher) {
    fun load(): ClipBook? {
        if (!file.exists()) return ClipBook()
        val plain = runCatching { cipher.decrypt(file.readText(Charsets.US_ASCII)) }.getOrNull() ?: return null
        return ClipCodec.decode(plain)
    }

    fun save(book: ClipBook): Boolean = runCatching {
        val temp = File(file.parentFile, file.name + TEMP_SUFFIX)
        temp.writeText(cipher.encrypt(ClipCodec.encode(book)), Charsets.US_ASCII)
        if (!temp.renameTo(file)) {
            file.delete()
            check(temp.renameTo(file))
        }
    }.isSuccess

    private companion object {
        const val TEMP_SUFFIX = ".tmp"
    }
}
