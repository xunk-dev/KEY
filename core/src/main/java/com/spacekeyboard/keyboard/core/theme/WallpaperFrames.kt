package com.spacekeyboard.keyboard.core.theme

import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WallpaperFrames(private val resolver: ContentResolver, private val uri: Uri) : AutoCloseable {
    private val stripPool: ExecutorService = Executors.newSingleThreadExecutor()
    private val previewPool: ExecutorService = Executors.newSingleThreadExecutor()
    @Volatile private var closed = false
    @Volatile private var ticket = 0

    fun strip(durationMs: Long, count: Int, width: Int, onFrame: (Int, Bitmap) -> Unit) {
        stripPool.execute {
            open()?.let { retriever ->
                try {
                    for (index in 0 until count) {
                        if (closed) break
                        val at = durationMs * (2 * index + 1) / (2L * count)
                        grab(retriever, at, width, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let { onFrame(index, it) }
                    }
                } finally {
                    retriever.release()
                }
            }
        }
    }

    fun preview(positionMs: Long, width: Int, exact: Boolean, onFrame: (Bitmap) -> Unit) {
        val mine = ++ticket
        previewPool.execute {
            if (closed || mine != ticket) return@execute
            val option = if (exact) MediaMetadataRetriever.OPTION_CLOSEST else MediaMetadataRetriever.OPTION_PREVIOUS_SYNC
            open()?.let { retriever ->
                try {
                    if (!closed && mine == ticket) grab(retriever, positionMs, width, option)?.let(onFrame)
                } finally {
                    retriever.release()
                }
            }
        }
    }

    override fun close() {
        closed = true
        stripPool.shutdownNow()
        previewPool.shutdownNow()
    }

    private fun open(): MediaMetadataRetriever? {
        val retriever = MediaMetadataRetriever()
        return try {
            resolver.openFileDescriptor(uri, "r")?.use { retriever.setDataSource(it.fileDescriptor) }
            retriever
        } catch (_: RuntimeException) {
            retriever.release()
            null
        } catch (_: java.io.IOException) {
            retriever.release()
            null
        }
    }

    private fun grab(retriever: MediaMetadataRetriever, positionMs: Long, width: Int, option: Int): Bitmap? = try {
        retriever.getFrameAtTime(positionMs * MICROS, option)?.let { shrink(it, width) }
    } catch (_: RuntimeException) {
        null
    }

    private fun shrink(bitmap: Bitmap, side: Int): Bitmap {
        val (w, h) = WallpaperMath.fit(bitmap.width, bitmap.height, side)
        return if (w == bitmap.width) bitmap else Bitmap.createScaledBitmap(bitmap, w, h, true).also { bitmap.recycle() }
    }

    private companion object {
        const val MICROS = 1000L
    }
}
