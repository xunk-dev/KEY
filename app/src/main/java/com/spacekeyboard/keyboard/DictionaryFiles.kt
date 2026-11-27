package com.spacekeyboard.keyboard

import android.content.Context
import android.net.Uri
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore

internal object DictionaryFiles {
    const val LIMIT = 300_000

    fun write(context: Context, uri: Uri, store: SuggestionStore): String = runCatching {
        val words = store.words()
        val stream = context.contentResolver.openOutputStream(uri, "wt") ?: error("no stream")
        stream.use { it.write(store.exportText().toByteArray(Charsets.UTF_8)) }
        context.getString(R.string.dictionary_exported, words.size)
    }.getOrElse { DebugLog.failure(context, "dictionary write", it); context.getString(R.string.dictionary_file_failed) }

    fun read(context: Context, uri: Uri, store: SuggestionStore): String = runCatching {
        val stream = context.contentResolver.openInputStream(uri) ?: error("no stream")
        val bytes = stream.use { BackupFiles.readLimited(it, LIMIT) }
        if (bytes == null) {
            context.getString(R.string.dictionary_file_large)
        } else {
            val added = store.importText(bytes.toString(Charsets.UTF_8))
            if (added == 0) context.getString(R.string.dictionary_imported_none) else context.getString(R.string.dictionary_imported, added)
        }
    }.getOrElse { DebugLog.failure(context, "dictionary read", it); context.getString(R.string.dictionary_file_failed) }
}
