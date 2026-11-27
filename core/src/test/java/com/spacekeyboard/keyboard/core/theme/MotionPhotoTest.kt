package com.spacekeyboard.keyboard.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MotionPhotoTest {
    private fun clip(size: Int): ByteArray = ByteArray(size).also { "ftyp".toByteArray().copyInto(it, 4) }

    private fun photo(xmp: String, video: ByteArray): ByteArray =
        byteArrayOf(0xFF.toByte(), 0xD8.toByte()) + xmp.toByteArray(Charsets.ISO_8859_1) + ByteArray(2000) + video

    private fun container(length: Int): String =
        "<Container:Item Item:Mime=\"image/jpeg\" Item:Semantic=\"Primary\" Item:Length=\"0\"/>" +
            "<Container:Item Item:Mime=\"video/mp4\" Item:Semantic=\"MotionPhoto\" Item:Length=\"$length\"/>"

    @Test
    fun findsTheClipFromTheContainerDirectory() {
        val video = clip(500)
        val bytes = photo(container(video.size), video)
        assertEquals(bytes.size - 500 until bytes.size, MotionPhoto.videoRange(bytes))
    }

    @Test
    fun findsTheClipFromTheOlderMicroVideoOffset() {
        val video = clip(300)
        val bytes = photo("<rdf GCamera:MicroVideo=\"1\" GCamera:MicroVideoOffset=\"300\"/>", video)
        assertEquals(bytes.size - 300 until bytes.size, MotionPhoto.videoRange(bytes))
    }

    @Test
    fun plainPhotoHasNoClip() {
        assertNull(MotionPhoto.videoRange(photo("<x:xmpmeta/>", ByteArray(300))))
    }

    @Test
    fun rejectsALengthThatDoesNotPointAtAClip() {
        val video = ByteArray(400)
        assertNull(MotionPhoto.videoRange(photo(container(400), video)))
        assertNull(MotionPhoto.videoRange(photo(container(999_999), clip(400))))
    }

    @Test
    fun rejectsDataThatIsNotAJpeg() {
        val video = clip(400)
        val bytes = photo(container(400), video)
        bytes[0] = 0
        assertNull(MotionPhoto.videoRange(bytes))
    }
}
