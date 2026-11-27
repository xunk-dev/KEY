package com.spacekeyboard.keyboard.clipboard

import android.content.ClipDescription
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

class ClipBlobProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = PLAIN

    override fun getStreamTypes(uri: Uri, mimeTypeFilter: String): Array<String>? =
        if (ClipDescription.compareMimeTypes(PLAIN, mimeTypeFilter)) arrayOf(PLAIN) else null

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException(mode)
        val id = blobId(uri) ?: throw FileNotFoundException()
        val owner = context ?: throw FileNotFoundException()
        val input = ClipStoreFactory.blobs(owner)?.open(id) ?: throw FileNotFoundException(id)
        val pipe = ParcelFileDescriptor.createPipe()
        val sink = ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
        Thread { pour(input, sink) }.apply { isDaemon = true }.start()
        return pipe[0]
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val id = blobId(uri) ?: return null
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val row = Array<Any?>(columns.size) { if (columns[it] == OpenableColumns.DISPLAY_NAME) ClipBlobs.nameFor(id) else null }
        return MatrixCursor(columns).apply { addRow(row) }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    private fun blobId(uri: Uri): String? = uri.pathSegments.firstOrNull()?.takeIf { ClipBlobs.isId(it) }

    private fun pour(input: InputStream, sink: OutputStream) {
        runCatching { input.use { source -> sink.use { source.copyTo(it, COPY_BUFFER) } } }
    }

    companion object {
        private const val PLAIN = "text/plain"
        private const val COPY_BUFFER = 65_536

        fun uri(context: Context, blob: String): Uri =
            Uri.Builder().scheme("content").authority(context.packageName + ".clipblob")
                .appendPath(blob).appendPath(ClipBlobs.nameFor(blob)).build()
    }
}
