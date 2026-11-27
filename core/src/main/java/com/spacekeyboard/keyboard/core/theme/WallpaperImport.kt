package com.spacekeyboard.keyboard.core.theme

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

enum class WallpaperImportResult { OK, TOO_LARGE, UNSUPPORTED }

data class WallpaperProbe(val video: Boolean, val durationMs: Long) {
    val needsTrim: Boolean get() = video && TrimMath.needsTrim(durationMs)
}

class WallpaperImport(private val resolver: ContentResolver, private val store: WallpaperStore) {

    fun probe(uri: Uri): WallpaperProbe {
        if (!isVideo(uri)) return WallpaperProbe(false, 0L)
        return WallpaperProbe(true, videoInfo(uri)?.durationMs ?: 0L)
    }

    fun import(uri: Uri, trim: TrimRange? = null): WallpaperImportResult {
        return try {
            if (isVideo(uri)) importVideo(uri, trim) else importImage(uri)
        } catch (_: IOException) {
            WallpaperImportResult.UNSUPPORTED
        } catch (_: SecurityException) {
            WallpaperImportResult.UNSUPPORTED
        } catch (_: OutOfMemoryError) {
            WallpaperImportResult.TOO_LARGE
        }
    }

    private fun isVideo(uri: Uri): Boolean = resolver.getType(uri).orEmpty().startsWith("video/")

    private fun importImage(uri: Uri): WallpaperImportResult {
        importLive(uri)?.let { return it }
        val upright = decodeUpright { open(uri) } ?: return WallpaperImportResult.UNSUPPORTED
        return if (storeStill(scaled(upright), store.imageFile)) {
            store.commit(WallpaperKind.IMAGE)
            WallpaperImportResult.OK
        } else {
            WallpaperImportResult.UNSUPPORTED
        }
    }

    private fun importLive(uri: Uri): WallpaperImportResult? {
        if (resolver.getType(uri) != JPEG) return null
        val bytes = readCapped(uri) ?: return null
        val range = MotionPhoto.videoRange(bytes) ?: return null
        val still = decodeUpright { ByteArrayInputStream(bytes) } ?: return null
        val copy = File(store.workDir(), VIDEO_TMP)
        copy.outputStream().use { it.write(bytes, range.first, range.last - range.first + 1) }
        return finishVideo(copy, still, true).takeIf { it == WallpaperImportResult.OK }
    }

    private fun importVideo(uri: Uri, trim: TrimRange?): WallpaperImportResult {
        val copy = File(store.workDir(), VIDEO_TMP)
        val range = trim ?: videoInfo(uri)?.takeIf { TrimMath.needsTrim(it.durationMs) }?.let { TrimMath.initial(it.durationMs) }
        val failure = if (range == null) stageWhole(uri, copy) else stageClip(uri, range, copy)
        if (failure != null) {
            copy.delete()
            return failure
        }
        return finishVideo(copy, null, false)
    }

    private fun stageWhole(uri: Uri, copy: File): WallpaperImportResult? =
        if (copyLimited(uri, copy)) null else WallpaperImportResult.TOO_LARGE

    private fun stageClip(uri: Uri, range: TrimRange, copy: File): WallpaperImportResult? {
        val info = videoInfo(uri) ?: return WallpaperImportResult.UNSUPPORTED
        if (info.side > WallpaperMath.MAX_VIDEO_SIDE) return WallpaperImportResult.TOO_LARGE
        val safe = TrimRange(range.startMs.coerceAtLeast(0L), minOf(range.endMs, info.durationMs).coerceAtLeast(1L))
        val clipped = resolver.openFileDescriptor(uri, "r")?.use { VideoClipper.clip(it.fileDescriptor, safe, info.rotation, copy) }
        if (clipped != true) return WallpaperImportResult.UNSUPPORTED
        return if (copy.length() > WallpaperMath.MAX_VIDEO_BYTES) WallpaperImportResult.TOO_LARGE else null
    }

    private fun finishVideo(copy: File, still: Bitmap?, live: Boolean): WallpaperImportResult {
        val poster = File(store.workDir(), POSTER_TMP)
        try {
            val frame = firstFrame(copy) ?: return WallpaperImportResult.UNSUPPORTED
            if (maxOf(frame.width, frame.height, frame.sourceSide) > WallpaperMath.MAX_VIDEO_SIDE) {
                return WallpaperImportResult.TOO_LARGE
            }
            val written = poster.outputStream().use { scaled(still ?: frame.bitmap).compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            if (!written || !poster.renameTo(store.posterFile) || !copy.renameTo(store.videoFile)) {
                return WallpaperImportResult.UNSUPPORTED
            }
            store.commit(WallpaperKind.VIDEO, live)
            return WallpaperImportResult.OK
        } finally {
            copy.delete()
            poster.delete()
        }
    }

    private fun videoInfo(uri: Uri): VideoInfo? = try {
        resolver.openFileDescriptor(uri, "r")?.use { VideoClipper.info(it.fileDescriptor) }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }

    private class Frame(val bitmap: Bitmap, val sourceSide: Int) {
        val width: Int get() = bitmap.width
        val height: Int get() = bitmap.height
    }

    private fun firstFrame(file: File): Frame? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.path)
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != "yes") return null
            val side = maxOf(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0,
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0,
            )
            val bitmap = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: return null
            Frame(bitmap, side)
        } catch (_: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun copyLimited(uri: Uri, target: File): Boolean {
        var total = 0L
        open(uri).use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(BUFFER)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > WallpaperMath.MAX_VIDEO_BYTES) return false
                    output.write(buffer, 0, read)
                }
            }
        }
        return total > 0
    }

    private fun readCapped(uri: Uri): ByteArray? = open(uri).use { input ->
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (out.size() + read > MAX_PHOTO_BYTES) return null
            out.write(buffer, 0, read)
        }
        out.toByteArray()
    }

    private fun decodeUpright(opener: () -> InputStream): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        opener().use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = WallpaperMath.sampleSize(bounds.outWidth, bounds.outHeight, WallpaperMath.MAX_SIDE)
        }
        val decoded = opener().use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        return rotated(decoded, orientation(opener))
    }

    private fun storeStill(bitmap: Bitmap, file: File): Boolean {
        val target = File(store.workDir(), IMAGE_TMP)
        val written = target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        if (!written || !target.renameTo(file)) {
            target.delete()
            return false
        }
        return true
    }

    private fun orientation(opener: () -> InputStream): Int = try {
        opener().use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
    } catch (_: IOException) {
        ExifInterface.ORIENTATION_NORMAL
    }

    private fun rotated(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaled(bitmap: Bitmap): Bitmap {
        val (w, h) = WallpaperMath.fit(bitmap.width, bitmap.height, WallpaperMath.MAX_SIDE)
        return if (w == bitmap.width && h == bitmap.height) bitmap else Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    private fun open(uri: Uri): InputStream = resolver.openInputStream(uri) ?: throw IOException("Cannot open $uri")

    private companion object {
        const val JPEG = "image/jpeg"
        const val JPEG_QUALITY = 88
        const val BUFFER = 64 * 1024
        const val MAX_PHOTO_BYTES = 24 * 1024 * 1024
        const val VIDEO_TMP = "video.mp4.tmp"
        const val POSTER_TMP = "poster.jpg.tmp"
        const val IMAGE_TMP = "image.jpg.tmp"
    }
}
