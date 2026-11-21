package com.spacekeyboard.keyboard.clipboard

import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardTest {
    private val hour = 3_600_000L

    @Test
    fun newestFirstAndDuplicatesMoveUp() {
        val book = ClipBook().add("a", 1).add("b", 2).add("a", 3)
        assertEquals(listOf("a", "b"), book.current.map { it.text })
        assertEquals(2, book.items.size)
    }

    @Test
    fun blankTextIsIgnored() {
        assertEquals(0, ClipBook().add("  ", 1).add("", 2).items.size)
    }

    @Test
    fun currentListIsCappedButPinnedAndSavedAreNot() {
        var book = ClipBook().addSection("work").add("keep", 0)
        val keep = book.items.single().id
        book = book.setPinned(keep, true).add("saved", 1)
        val saved = book.items.first { it.text == "saved" }.id
        book = book.moveToSection(saved, "work")
        (1..80).forEach { book = book.add("e$it", it.toLong()) }
        assertEquals(ClipPolicy.LIMIT, book.current.size)
        assertEquals("e80", book.current.first().text)
        assertEquals(listOf("keep"), book.pinned.map { it.text })
        assertEquals(listOf("saved"), book.saved("work").map { it.text })
    }

    @Test
    fun oldUnpinnedItemsExpireAfterADay() {
        var book = ClipBook().add("old", 0).add("pin", 0)
        book = book.setPinned(book.items.first { it.text == "pin" }.id, true)
        book = book.add("new", 25 * hour)
        assertEquals(listOf("new"), book.current.map { it.text })
        assertEquals(listOf("pin"), book.pinned.map { it.text })
    }

    @Test
    fun unpinningReturnsToCurrent() {
        val book = ClipBook().add("a", 1)
        val id = book.items.single().id
        assertEquals(listOf("a"), book.setPinned(id, true).setPinned(id, false).current.map { it.text })
    }

    @Test
    fun sectionsRejectDuplicatesAndUnknownTargets() {
        val book = ClipBook().addSection(" a ").addSection("a").addSection("")
        assertEquals(listOf("a"), book.sections.map { it.name })
        val withItem = book.add("x", 1)
        assertEquals(withItem, withItem.moveToSection(withItem.items.single().id, "missing"))
    }

    @Test
    fun deletingASectionDeletesItsItemsAndLockIsPerSection() {
        var book = ClipBook().addSection("a").addSection("b").add("x", 1).add("y", 2)
        book = book.moveToSection(book.items[0].id, "a").moveToSection(book.items[1].id, "b")
        book = book.setSectionLocked("a", true).deleteSection("a")
        assertEquals(listOf("b"), book.sections.map { it.name })
        assertEquals(listOf("y"), book.items.map { it.text })
        assertFalse(book.sections.single().locked)
    }

    @Test
    fun clearCurrentKeepsPinnedAndSaved() {
        var book = ClipBook().add("a", 1).add("b", 2)
        book = book.setPinned(book.items[0].id, true).clearCurrent()
        assertEquals(listOf("a"), book.items.map { it.text })
    }

    @Test
    fun codecRoundTripsAwkwardText() {
        val tricky = "line1\nline2\tTab \\ back\\n \r end 😀 مرحبا"
        var book = ClipBook().addSection("sec\ttion").add(tricky, 5).add("plain", 6)
        book = book.moveToSection(book.items[0].id, "sec\ttion").setPinned(book.items[1].id, true)
        book = book.setSectionLocked("sec\ttion", true)
        assertEquals(book, ClipCodec.decode(ClipCodec.encode(book)))
    }

    @Test
    fun codecSkipsGarbageLines() {
        val book = ClipCodec.decode("nonsense\nI\tx\ty\nN\t7\nI\t1\tfalse\t\t5\thello")
        assertEquals(7, book.nextId)
        assertEquals(listOf("hello"), book.items.map { it.text })
    }

    @Test
    fun cipherRoundTripsAndNeverReusesAnIv() {
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val cipher = AesGcmCipher(key)
        val first = cipher.encrypt("secret م")
        assertEquals("secret م", cipher.decrypt(first))
        assertNotEquals(first, cipher.encrypt("secret م"))
        assertFalse(first.contains("secret"))
    }

    @Test
    fun cipherRejectsTamperingAndOtherKeys() {
        val generator = KeyGenerator.getInstance("AES").apply { init(256) }
        val cipher = AesGcmCipher(generator.generateKey())
        val encoded = cipher.encrypt("data")
        val flipped = encoded.dropLast(1) + (if (encoded.last() == '0') '1' else '0')
        assertNull(cipher.decrypt(flipped))
        assertNull(AesGcmCipher(generator.generateKey()).decrypt(encoded))
        assertNull(cipher.decrypt("zz"))
    }

    @Test
    fun unlockWindowsExpire() {
        assertFalse(UnlockWindow.EVERY_TIME.isOpen(100, 100))
        assertFalse(UnlockWindow.FIVE_MINUTES.isOpen(null, 100))
        assertTrue(UnlockWindow.FIVE_MINUTES.isOpen(0, 299_999))
        assertFalse(UnlockWindow.FIVE_MINUTES.isOpen(0, 300_000))
        assertFalse(UnlockWindow.ONE_HOUR.isOpen(1000, 500))
        assertEquals(UnlockWindow.ONE_HOUR, UnlockWindow.byId("1h"))
        assertEquals(UnlockWindow.EVERY_TIME, UnlockWindow.byId("x"))
    }

    @Test
    fun recordingRespectsConsentSensitivityAndSize() {
        assertTrue(ClipFilter.shouldRecord(true, false, "hi"))
        assertFalse(ClipFilter.shouldRecord(false, false, "hi"))
        assertFalse(ClipFilter.shouldRecord(true, true, "hi"))
        assertFalse(ClipFilter.shouldRecord(true, false, " "))
        assertFalse(ClipFilter.shouldRecord(true, false, "x".repeat(ClipPolicy.MAX_TEXT + 1)))
    }
}
