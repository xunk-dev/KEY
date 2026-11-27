package com.spacekeyboard.keyboard.emoji.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentsTest {
    @Test
    fun newestComesFirstAndDuplicatesMoveUp() {
        var list = emptyList<String>()
        list = Recents.record(list, "a")
        list = Recents.record(list, "b")
        list = Recents.record(list, "c")
        list = Recents.record(list, "a")
        assertEquals(listOf("a", "c", "b"), list)
    }

    @Test
    fun stopsAtSixty() {
        var list = emptyList<String>()
        (1..75).forEach { list = Recents.record(list, "e$it") }
        assertEquals(Recents.LIMIT, list.size)
        assertEquals("e75", list.first())
        assertEquals("e16", list.last())
    }

    @Test
    fun emptyTextIsIgnored() {
        assertEquals(listOf("a"), Recents.record(listOf("a"), ""))
    }

    @Test
    fun encodeAndDecodeRoundTripWithSequences() {
        val items = listOf("👩‍💻", "❤️", "0️⃣")
        assertEquals(items, Recents.decode(Recents.encode(items)))
    }

    @Test
    fun decodeToleratesGarbage() {
        assertTrue(Recents.decode(null).isEmpty())
        assertTrue(Recents.decode("").isEmpty())
        assertEquals(listOf("a", "b"), Recents.decode("a\n\nb\na"))
        assertEquals(Recents.LIMIT, Recents.decode((1..90).joinToString("\n") { "e$it" }).size)
    }

    @Test
    fun removeDropsOnlyThatItemAndKeepsOrder() {
        assertEquals(listOf("a", "c"), Recents.remove(listOf("a", "b", "c"), "b"))
        assertEquals(listOf("a"), Recents.remove(listOf("a"), "x"))
    }

    @Test
    fun pickingFromTheShownRecentsListKeepsItsOrder() {
        val list = listOf("a", "b", "c")
        assertTrue(Recents.keepsOrder(true, list, "c"))
        assertTrue(!Recents.keepsOrder(false, list, "c"))
        assertTrue(!Recents.keepsOrder(true, list, "z"))
    }
}
