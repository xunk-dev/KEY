package com.spacekeyboard.keyboard.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ImageStore(context: Context, private val clock: () -> Long = System::currentTimeMillis) {
    private val app = context.applicationContext
    private val images = File(app.filesDir, IMAGES_DIR)
    private val thumbs = File(app.filesDir, THUMBS_DIR)
    private val importer = ImageImporter(app.contentResolver)

    fun list(): List<ImageItem> {
        val files = images.listFiles().orEmpty()
        return ImageNames.newestFirst(files.mapNotNull { ImageNames.parse(it.name, it.lastModified()) })
    }

    fun uri(item: ImageItem): Uri = FileProvider.getUriForFile(app, "${app.packageName}.images", file(item))

    fun thumbnail(item: ImageItem): Bitmap? =
        BitmapFactory.decodeFile(thumbFile(item).path) ?: decodeSmall(file(item))

    fun remove(id: String) {
        images.listFiles().orEmpty().filter { it.name.substringBeforeLast('.') == id }.forEach { it.delete() }
        File(thumbs, id).delete()
    }

    fun clear() {
        images.listFiles().orEmpty().forEach { it.delete() }
        thumbs.listFiles().orEmpty().forEach { it.delete() }
    }

    fun addAsync(sources: List<Uri>, done: (ImportResult) -> Unit) {
        val copy = sources.toList()
        EXECUTOR.execute {
            val result = addAll(copy)
            MAIN.post { done(result) }
        }
    }

    private fun addAll(sources: List<Uri>): ImportResult {
        images.listFiles().orEmpty().filter { it.name.endsWith(TEMP) }.forEach { it.delete() }
        var added = 0
        var failed = 0
        var full = false
        for (source in sources) {
            if (list().size >= ImagePolicy.LIMIT || usedBytes() >= ImagePolicy.MAX_TOTAL_BYTES) {
                full = true
                break
            }
            if (add(source)) added++ else failed++
        }
        return ImportResult(added, failed, full)
    }

    private fun add(source: Uri): Boolean {
        val prepared = importer.prepare(source) ?: return false
        val digest = ImageNames.digest(prepared.bytes)
        if (list().any { ImageNames.digestOf(it.id) == digest }) return true
        val id = ImageNames.newId(clock(), SEQUENCE.incrementAndGet(), digest)
        val name = ImageNames.fileName(id, prepared.mime) ?: return false
        images.mkdirs()
        val target = File(images, name)
        val temp = File(images, "$id$TEMP")
        val saved = runCatching {
            temp.writeBytes(prepared.bytes)
            temp.renameTo(target)
        }.getOrDefault(false)
        if (!saved) {
            temp.delete()
            return false
        }
        writeThumb(id, target)
        return true
    }

    private fun writeThumb(id: String, source: File) {
        val bitmap = decodeSmall(source) ?: return
        val bytes = ImageEncoder.thumbnail(bitmap)
        bitmap.recycle()
        if (bytes == null) return
        thumbs.mkdirs()
        runCatching { File(thumbs, id).writeBytes(bytes) }
    }

    private fun decodeSmall(source: File): Bitmap? {
        if (!source.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = ImagePolicy.sampleSize(bounds.outWidth, bounds.outHeight, ImagePolicy.THUMB_SIDE)
        }
        return try {
            BitmapFactory.decodeFile(source.path, options)
        } catch (error: OutOfMemoryError) {
            null
        }
    }

    fun exists(item: ImageItem): Boolean = file(item).isFile

    fun fileOf(item: ImageItem): File = file(item)

    private fun usedBytes(): Long = images.listFiles().orEmpty().sumOf { it.length() }

    private fun file(item: ImageItem): File = File(images, ImageNames.fileName(item.id, item.mime).orEmpty())

    private fun thumbFile(item: ImageItem): File = File(thumbs, item.id)

    private companion object {
        const val IMAGES_DIR = "images"
        const val THUMBS_DIR = "image_thumbs"
        const val TEMP = ".tmp"
        val SEQUENCE = AtomicInteger()
        val EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()
        val MAIN = Handler(Looper.getMainLooper())
    }
}
