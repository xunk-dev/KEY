package com.spacekeyboard.keyboard.clipboard

import android.content.Context
import com.spacekeyboard.keyboard.core.vault.AesGcmCipher
import com.spacekeyboard.keyboard.core.vault.KeystoreKeys
import java.io.File

class OpenedStore(val store: ClipStore, val book: ClipBook, val blobs: ClipBlobs)

object ClipStoreFactory {
    private const val FILE = "clipboard.vault"
    private const val BLOBS = "clipblobs"
    private const val ALIAS = "space_keyboard_clipboard"

    fun open(context: Context): OpenedStore? = runCatching {
        val file = File(context.applicationContext.filesDir, FILE)
        val key = KeystoreKeys.getOrCreate(ALIAS)
        val first = ClipStore(file, AesGcmCipher(key))
        val book = first.load()
        if (book != null) {
            OpenedStore(first, book, ClipBlobs(blobDir(context), key))
        } else {
            file.delete()
            blobDir(context).deleteRecursively()
            val fresh = KeystoreKeys.reset(ALIAS)
            OpenedStore(ClipStore(file, AesGcmCipher(fresh)), ClipBook(), ClipBlobs(blobDir(context), fresh))
        }
    }.getOrNull()

    fun blobs(context: Context): ClipBlobs? = runCatching { ClipBlobs(blobDir(context), KeystoreKeys.getOrCreate(ALIAS)) }.getOrNull()

    fun wipe(context: Context) {
        runCatching {
            File(context.applicationContext.filesDir, FILE).delete()
            blobDir(context).deleteRecursively()
            KeystoreKeys.reset(ALIAS)
        }
    }

    private fun blobDir(context: Context): File = File(context.applicationContext.filesDir, BLOBS)
}
