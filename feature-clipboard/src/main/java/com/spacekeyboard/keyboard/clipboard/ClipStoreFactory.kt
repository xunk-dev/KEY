package com.spacekeyboard.keyboard.clipboard

import android.content.Context
import java.io.File

class OpenedStore(val store: ClipStore, val book: ClipBook)

object ClipStoreFactory {
    private const val FILE = "clipboard.vault"
    private const val ALIAS = "space_keyboard_clipboard"

    fun open(context: Context): OpenedStore? = runCatching {
        val file = File(context.applicationContext.filesDir, FILE)
        val first = ClipStore(file, AesGcmCipher(KeystoreKeys.getOrCreate(ALIAS)))
        val book = first.load()
        if (book != null) {
            OpenedStore(first, book)
        } else {
            file.delete()
            OpenedStore(ClipStore(file, AesGcmCipher(KeystoreKeys.reset(ALIAS))), ClipBook())
        }
    }.getOrNull()

    fun wipe(context: Context) {
        runCatching {
            File(context.applicationContext.filesDir, FILE).delete()
            KeystoreKeys.reset(ALIAS)
        }
    }
}
