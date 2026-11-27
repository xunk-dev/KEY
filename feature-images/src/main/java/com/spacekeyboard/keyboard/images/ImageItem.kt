package com.spacekeyboard.keyboard.images

import java.security.MessageDigest

enum class ImageKind { PHOTO, STICKER, GIF }

enum class ImageFilter {
    ALL,
    GIF,
    STICKER;

    fun accepts(item: ImageItem): Boolean = this == ALL || item.kind.name == name
}

data class ImageItem(val id: String, val mime: String, val addedAt: Long) {
    val kind: ImageKind
        get() = when (mime) {
            ImagePolicy.GIF -> ImageKind.GIF
            ImagePolicy.PNG, ImagePolicy.WEBP -> ImageKind.STICKER
            else -> ImageKind.PHOTO
        }

    val animated: Boolean get() = mime == ImagePolicy.GIF || mime == ImagePolicy.WEBP
}

object ImagePolicy {
    const val LIMIT = 200
    const val MAX_SIDE = 1024
    const val THUMB_SIDE = 192
    const val MAX_GIF_BYTES = 3L * 1024 * 1024
    const val MAX_GIF_SIDE = 2048
    const val MAX_TOTAL_BYTES = 150L * 1024 * 1024
    const val PNG = "image/png"
    const val JPEG = "image/jpeg"
    const val GIF = "image/gif"
    const val WEBP = "image/webp"
    val STORED = listOf(PNG, JPEG, GIF, WEBP)

    fun extension(mime: String): String? = when (mime) {
        PNG -> "png"
        JPEG -> "jpg"
        GIF -> "gif"
        WEBP -> "webp"
        else -> null
    }

    fun mimeOf(extension: String): String? = when (extension.lowercase()) {
        "png" -> PNG
        "jpg" -> JPEG
        "gif" -> GIF
        "webp" -> WEBP
        else -> null
    }

    fun sampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        val longest = maxOf(width, height)
        while (longest / (sample * 2) >= target) sample *= 2
        return sample
    }

    fun isGif(bytes: ByteArray): Boolean {
        if (bytes.size < GIF_MAGIC) return false
        val head = String(bytes, 0, GIF_MAGIC, Charsets.ISO_8859_1)
        return head == "GIF87a" || head == "GIF89a"
    }

    fun isAnimatedWebp(bytes: ByteArray): Boolean {
        if (bytes.size <= WEBP_FLAGS) return false
        val riff = String(bytes, 0, RIFF_LENGTH, Charsets.ISO_8859_1)
        val webp = String(bytes, WEBP_TAG, RIFF_LENGTH, Charsets.ISO_8859_1)
        val extended = String(bytes, WEBP_CHUNK, RIFF_LENGTH, Charsets.ISO_8859_1)
        return riff == "RIFF" && webp == "WEBP" && extended == "VP8X" && (bytes[WEBP_FLAGS].toInt() and WEBP_ANIMATION) != 0
    }

    fun fitted(width: Int, height: Int, max: Int): Pair<Int, Int> {
        val longest = maxOf(width, height)
        if (longest <= max || longest <= 0) return width to height
        val scale = max.toFloat() / longest
        return maxOf(1, Math.round(width * scale)) to maxOf(1, Math.round(height * scale))
    }

    fun gifSide(bytes: ByteArray): Int {
        if (!isGif(bytes) || bytes.size < GIF_HEADER) return 0
        val width = (bytes[6].toInt() and BYTE) or ((bytes[7].toInt() and BYTE) shl 8)
        val height = (bytes[8].toInt() and BYTE) or ((bytes[9].toInt() and BYTE) shl 8)
        return maxOf(width, height)
    }

    fun rotation(exifOrientation: Int): Int = when (exifOrientation) {
        3, 4 -> 180
        6, 7 -> 90
        5, 8 -> 270
        else -> 0
    }

    fun flipped(exifOrientation: Int): Boolean = exifOrientation in setOf(2, 4, 5, 7)

    private const val GIF_MAGIC = 6
    private const val GIF_HEADER = 10
    private const val RIFF_LENGTH = 4
    private const val WEBP_TAG = 8
    private const val WEBP_CHUNK = 12
    private const val WEBP_FLAGS = 20
    private const val WEBP_ANIMATION = 0x02
    private const val BYTE = 0xFF
}

object ImageNames {
    private const val DIGEST_CHARS = 12

    fun newId(now: Long, sequence: Int, digest: String): String = "$now-$sequence-$digest"

    fun digest(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }.take(DIGEST_CHARS)

    fun digestOf(id: String): String = id.substringAfterLast('-')

    fun fileName(id: String, mime: String): String? = ImagePolicy.extension(mime)?.let { "$id.$it" }

    fun parse(fileName: String, modified: Long): ImageItem? {
        val dot = fileName.lastIndexOf('.')
        if (dot <= 0 || dot == fileName.length - 1) return null
        val mime = ImagePolicy.mimeOf(fileName.substring(dot + 1)) ?: return null
        return ImageItem(fileName.substring(0, dot), mime, modified)
    }

    fun newestFirst(items: List<ImageItem>): List<ImageItem> =
        items.sortedWith(compareByDescending<ImageItem> { it.addedAt }.thenByDescending { it.id })
}

data class ImportResult(val added: Int, val failed: Int, val full: Boolean)
