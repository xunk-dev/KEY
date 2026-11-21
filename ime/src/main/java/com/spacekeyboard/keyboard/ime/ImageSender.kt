package com.spacekeyboard.keyboard.ime

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.spacekeyboard.keyboard.images.ImageItem
import com.spacekeyboard.keyboard.images.ImageStore
import com.spacekeyboard.keyboard.images.R as ImagesR

class ImageSender(private val context: Context, private val store: ImageStore) {
    fun send(item: ImageItem, connection: InputConnection?, editor: EditorInfo?) {
        connection ?: return
        editor ?: return
        val uri = store.uri(item)
        if (accepts(editor, item.mime) && commit(uri, item, connection, editor)) return
        copy(uri)
    }

    private fun accepts(editor: EditorInfo, mime: String): Boolean =
        EditorInfoCompat.getContentMimeTypes(editor).any { ClipDescription.compareMimeTypes(mime, it) }

    private fun commit(uri: Uri, item: ImageItem, connection: InputConnection, editor: EditorInfo): Boolean {
        val description = ClipDescription(context.getString(ImagesR.string.images_label), arrayOf(item.mime))
        val info = InputContentInfoCompat(uri, description, null)
        return InputConnectionCompat.commitContent(connection, editor, info, grantFlags(uri, editor), null)
    }

    private fun grantFlags(uri: Uri, editor: EditorInfo): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        }
        editor.packageName?.let { context.grantUriPermission(it, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        Handler(Looper.getMainLooper()).postDelayed(
            { context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) },
            REVOKE_DELAY_MS,
        )
        return 0
    }

    private fun copy(uri: Uri) {
        val manager = context.getSystemService(ClipboardManager::class.java) ?: return
        val label = context.getString(ImagesR.string.images_label)
        manager.setPrimaryClip(ClipData.newUri(context.contentResolver, label, uri))
        Toast.makeText(context, ImagesR.string.images_copied, Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val REVOKE_DELAY_MS = 120_000L
    }
}
