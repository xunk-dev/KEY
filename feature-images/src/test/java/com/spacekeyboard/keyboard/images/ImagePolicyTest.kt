package com.spacekeyboard.keyboard.images

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImagePolicyTest {
    @Test
    fun mimeAndExtensionRoundTrip() {
        ImagePolicy.STORED.forEach { mime ->
            val extension = ImagePolicy.extension(mime)!!
            assertEquals(mime, ImagePolicy.mimeOf(extension))
        }
        assertNull(ImagePolicy.extension("image/heic"))
        assertNull(ImagePolicy.mimeOf("tmp"))
        assertEquals(ImagePolicy.JPEG, ImagePolicy.mimeOf("JPG"))
    }

    @Test
    fun sampleSizeKeepsLongestSideAtLeastTarget() {
        assertEquals(1, ImagePolicy.sampleSize(800, 600, 1024))
        assertEquals(1, ImagePolicy.sampleSize(1500, 1500, 1024))
        assertEquals(2, ImagePolicy.sampleSize(2048, 2048, 1024))
        assertEquals(4, ImagePolicy.sampleSize(4096, 3000, 750))
        assertEquals(2, ImagePolicy.sampleSize(4000, 500, 1024))
        assertEquals(64, ImagePolicy.sampleSize(100000, 1024, 1024))
    }

    @Test
    fun gifNeedsItsSignature() {
        assertEquals(true, ImagePolicy.isGif("GIF89a....".toByteArray()))
        assertEquals(true, ImagePolicy.isGif("GIF87a".toByteArray()))
        assertEquals(false, ImagePolicy.isGif("GIF88a".toByteArray()))
        assertEquals(false, ImagePolicy.isGif("<html>".toByteArray()))
        assertEquals(false, ImagePolicy.isGif(ByteArray(3)))
    }

    @Test
    fun fittedShrinksLongestSideOnly() {
        assertEquals(1000 to 500, ImagePolicy.fitted(1000, 500, 1024))
        assertEquals(1024 to 512, ImagePolicy.fitted(2000, 1000, 1024))
        assertEquals(512 to 1024, ImagePolicy.fitted(1000, 2000, 1024))
        assertEquals(1024 to 1, ImagePolicy.fitted(10000, 1, 1024))
    }

    @Test
    fun rotationReadsExifOrientation() {
        assertEquals(0, ImagePolicy.rotation(1))
        assertEquals(180, ImagePolicy.rotation(3))
        assertEquals(90, ImagePolicy.rotation(6))
        assertEquals(270, ImagePolicy.rotation(8))
        assertEquals(0, ImagePolicy.rotation(0))
        assertEquals(270, ImagePolicy.rotation(5))
        assertEquals(90, ImagePolicy.rotation(7))
        assertEquals(180, ImagePolicy.rotation(4))
    }

    @Test
    fun exifMirrorsAreFlipped() {
        listOf(2, 4, 5, 7).forEach { assertEquals(true, ImagePolicy.flipped(it)) }
        listOf(0, 1, 3, 6, 8).forEach { assertEquals(false, ImagePolicy.flipped(it)) }
    }

    @Test
    fun gifSideReadsLogicalScreenSize() {
        val header = "GIF89a".toByteArray() + byteArrayOf(0x00, 0x02, 0x40, 0x01)
        assertEquals(512, ImagePolicy.gifSide(header))
        assertEquals(0, ImagePolicy.gifSide("GIF89a".toByteArray()))
        assertEquals(0, ImagePolicy.gifSide("<html>....".toByteArray()))
    }

    @Test
    fun digestIsStableAndPartOfTheId() {
        val digest = ImageNames.digest("abc".toByteArray())
        assertEquals("ba7816bf8f01", digest)
        assertEquals(digest, ImageNames.digest("abc".toByteArray()))
        assertEquals(digest, ImageNames.digestOf(ImageNames.newId(1L, 2, digest)))
    }

    @Test
    fun namesParseWhatTheStoreWrites() {
        val id = ImageNames.newId(1700000000000L, 3, "ba7816bf8f01")
        val name = ImageNames.fileName(id, ImagePolicy.PNG)!!
        assertEquals("1700000000000-3-ba7816bf8f01.png", name)
        assertEquals(ImageItem(id, ImagePolicy.PNG, 5L), ImageNames.parse(name, 5L))
    }

    @Test
    fun namesIgnoreForeignAndTemporaryFiles() {
        assertNull(ImageNames.parse("1700000000000-3.tmp", 1L))
        assertNull(ImageNames.parse("noextension", 1L))
        assertNull(ImageNames.parse(".png", 1L))
        assertNull(ImageNames.parse("name.", 1L))
        assertNull(ImageNames.fileName("x", "image/heic"))
    }

    @Test
    fun newestFirstBreaksTiesByIdDescending() {
        val old = ImageItem("a-1", ImagePolicy.PNG, 1L)
        val tieLow = ImageItem("b-1", ImagePolicy.PNG, 5L)
        val tieHigh = ImageItem("b-2", ImagePolicy.GIF, 5L)
        assertEquals(listOf(tieHigh, tieLow, old), ImageNames.newestFirst(listOf(old, tieLow, tieHigh)))
    }

    @Test
    fun webpIsAnimatedOnlyWithTheAnimationFlag() {
        fun webp(flags: Int, chunk: String = "VP8X"): ByteArray {
            val head = "RIFF".toByteArray() + ByteArray(4) + "WEBP".toByteArray() + chunk.toByteArray()
            return head + ByteArray(4) + byteArrayOf(flags.toByte()) + ByteArray(8)
        }
        assertEquals(true, ImagePolicy.isAnimatedWebp(webp(0x02)))
        assertEquals(true, ImagePolicy.isAnimatedWebp(webp(0x12)))
        assertEquals(false, ImagePolicy.isAnimatedWebp(webp(0x10)))
        assertEquals(false, ImagePolicy.isAnimatedWebp(webp(0x02, "VP8 ")))
        assertEquals(false, ImagePolicy.isAnimatedWebp("RIFF".toByteArray()))
        assertEquals(false, ImagePolicy.isAnimatedWebp("GIF89a".toByteArray() + ByteArray(20)))
    }

    @Test
    fun webpIsStoredAndParsed() {
        assertEquals("webp", ImagePolicy.extension(ImagePolicy.WEBP))
        assertEquals(ImagePolicy.WEBP, ImagePolicy.mimeOf("WEBP"))
        assertEquals(ImageItem("a-1", ImagePolicy.WEBP, 7L), ImageNames.parse("a-1.webp", 7L))
    }

    @Test
    fun kindsFollowTheStoredFormat() {
        assertEquals(ImageKind.GIF, ImageItem("a", ImagePolicy.GIF, 1L).kind)
        assertEquals(ImageKind.STICKER, ImageItem("a", ImagePolicy.PNG, 1L).kind)
        assertEquals(ImageKind.STICKER, ImageItem("a", ImagePolicy.WEBP, 1L).kind)
        assertEquals(ImageKind.PHOTO, ImageItem("a", ImagePolicy.JPEG, 1L).kind)
        assertEquals(listOf(false, false, true, true), listOf(ImagePolicy.JPEG, ImagePolicy.PNG, ImagePolicy.GIF, ImagePolicy.WEBP).map { ImageItem("a", it, 1L).animated })
    }

    @Test
    fun filtersPickTheirOwnKind() {
        val items = listOf(
            ImageItem("a", ImagePolicy.GIF, 1L),
            ImageItem("b", ImagePolicy.PNG, 2L),
            ImageItem("c", ImagePolicy.JPEG, 3L),
            ImageItem("d", ImagePolicy.WEBP, 4L),
        )
        assertEquals(items, ImagesSnapshot(items = items).shown)
        assertEquals(listOf(items[0]), ImagesSnapshot(items = items, filter = ImageFilter.GIF).shown)
        assertEquals(listOf(items[1], items[3]), ImagesSnapshot(items = items, filter = ImageFilter.STICKER).shown)
    }
}
