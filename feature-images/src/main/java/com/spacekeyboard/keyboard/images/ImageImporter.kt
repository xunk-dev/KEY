package com.spacekeyboard.keyboard.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal class PreparedImage(val mime: String, val bytes: ByteArray)

internal class ImageImporter(private val resolver: ContentResolver) {
    private companion object {
        const val BUFFER = 8192
    }

    fun prepare(source: Uri): PreparedImage? {
        val type = resolver.getType(source).orEmpty()
        if (!type.startsWith("image/")) return null
        return try {
            when (type) {
                ImagePolicy.GIF -> rawGif(source)
                ImagePolicy.WEBP -> animatedWebp(source) ?: rendered(source)
                else -> rendered(source)
            }
        } catch (error: Exception) {
            null
        } catch (error: OutOfMemoryError) {
            null
        }
    }

    private fun rawGif(source: Uri): PreparedImage? {
        val bytes = resolver.openInputStream(source)?.use { readLimited(it, ImagePolicy.MAX_GIF_BYTES) } ?: return null
        if (!ImagePolicy.isGif(bytes) || ImagePolicy.gifSide(bytes) > ImagePolicy.MAX_GIF_SIDE) return null
        return PreparedImage(ImagePolicy.GIF, bytes)
    }

    private fun animatedWebp(source: Uri): PreparedImage? {
        val bytes = resolver.openInputStream(source)?.use { readLimited(it, ImagePolicy.MAX_GIF_BYTES) } ?: return null
        if (!ImagePolicy.isAnimatedWebp(bytes)) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || maxOf(bounds.outWidth, bounds.outHeight) > ImagePolicy.MAX_GIF_SIDE) return null
        return PreparedImage(ImagePolicy.WEBP, bytes)
    }

    private fun readLimited(input: InputStream, limit: Long): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) return out.toByteArray()
            total += count
            if (total > limit) return null
            out.write(buffer, 0, count)
        }
    }

    private fun rendered(source: Uri): PreparedImage? {
        val bitmap = decode(source, ImagePolicy.MAX_SIDE) ?: return null
        val upright = rotate(bitmap, orientation(source))
        val sized = fit(upright, ImagePolicy.MAX_SIDE)
        val prepared = ImageEncoder.encode(sized)
        listOf(bitmap, upright, sized).distinct().forEach { it.recycle() }
        return prepared
    }

    private fun decode(source: Uri, target: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = ImagePolicy.sampleSize(bounds.outWidth, bounds.outHeight, target)
        }
        return resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun orientation(source: Uri): Int =
        runCatching {
            resolver.openInputStream(source)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

    private fun rotate(bitmap: Bitmap, exif: Int): Bitmap {
        val degrees = ImagePolicy.rotation(exif)
        val flip = ImagePolicy.flipped(exif)
        if (degrees == 0 && !flip) return bitmap
        val matrix = Matrix().apply {
            if (flip) postScale(-1f, 1f)
            postRotate(degrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun fit(bitmap: Bitmap, max: Int): Bitmap {
        val (width, height) = ImagePolicy.fitted(bitmap.width, bitmap.height, max)
        if (width == bitmap.width && height == bitmap.height) return bitmap
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}

internal object ImageEncoder {
    private const val JPEG_QUALITY = 90
    private const val THUMB_QUALITY = 80

    fun encode(bitmap: Bitmap): PreparedImage? {
        val alpha = bitmap.hasAlpha()
        val format = if (alpha) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val mime = if (alpha) ImagePolicy.PNG else ImagePolicy.JPEG
        val bytes = compress(bitmap, format, JPEG_QUALITY) ?: return null
        return PreparedImage(mime, bytes)
    }

    fun thumbnail(bitmap: Bitmap): ByteArray? {
        val (width, height) = ImagePolicy.fitted(bitmap.width, bitmap.height, ImagePolicy.THUMB_SIDE)
        val small = if (width == bitmap.width && height == bitmap.height) bitmap else Bitmap.createScaledBitmap(bitmap, width, height, true)
        val format = if (small.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val bytes = compress(small, format, THUMB_QUALITY)
        if (small !== bitmap) small.recycle()
        return bytes
    }

    private fun compress(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        return if (bitmap.compress(format, quality, out)) out.toByteArray() else null
    }
}
