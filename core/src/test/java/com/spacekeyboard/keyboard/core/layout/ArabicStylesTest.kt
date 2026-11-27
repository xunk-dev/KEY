package com.spacekeyboard.keyboard.core.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicStylesTest {
    private val files = mapOf(
        ArabicStyle.ELEVEN to "layout_ar",
        ArabicStyle.TEN to "layout_ar_ten",
        ArabicStyle.IOS to "layout_ar_ios",
        ArabicStyle.PAIRED to "layout_ar_pair",
        ArabicStyle.PLAIN to "layout_ar_plain",
    )

    private fun load(style: ArabicStyle): LayoutSpec =
        LayoutParser.parse(File("src/main/res/raw/${files.getValue(style)}.json").readText(Charsets.UTF_8))

    private val alphabet = "ابتثجحخدذرزسشصضطظعغفقكلمنهويءئؤةى".map { it.toString() }

    @Test
    fun everyStyleKeepsTheEssentialKeysOnceAndEvenRows() {
        ArabicStyle.entries.forEach { style ->
            val layout = load(style)
            assertEquals(style.name, "ar", layout.id)
            val keys = layout.rows.flatten()
            listOf(KeyAction.DELETE, KeyAction.SPACE, KeyAction.ENTER, KeyAction.OPEN_SYMBOLS, KeyAction.NUMBERS).forEach { action ->
                assertEquals("${style.name} $action", 1, keys.count { it.action == action })
            }
            assertEquals(keys.size, keys.map { it.id }.toSet().size)
            val totals = layout.rows.map { row -> row.sumOf { it.weight.toDouble() } }
            totals.forEach { assertEquals(style.name, totals.first(), it, 0.001) }
        }
    }

    @Test
    fun everyStyleReachesTheWholeAlphabet() {
        ArabicStyle.entries.forEach { style ->
            val reachable = load(style).rows.flatten().flatMap { it.letters + it.alts }
            assertTrue(style.name, reachable.containsAll(alphabet))
        }
    }

    @Test
    fun rowSizesMatchTheChosenStyle() {
        fun sizes(style: ArabicStyle) = load(style).rows.take(3).map { it.size }
        assertEquals(listOf(11, 11, 11), sizes(ArabicStyle.ELEVEN))
        assertEquals(listOf(10, 10, 10), sizes(ArabicStyle.TEN))
        assertEquals(listOf(11, 11, 11), sizes(ArabicStyle.IOS))
        assertEquals(listOf(5, 5, 6), sizes(ArabicStyle.PAIRED))
        assertEquals(listOf(7, 7, 7), sizes(ArabicStyle.PLAIN))
    }

    @Test
    fun elevenIsTheDefaultAndKeepsTheUsersArrangement() {
        assertEquals(ArabicStyle.ELEVEN, KeyboardPrefs().arabicStyle)
        val rows = load(ArabicStyle.ELEVEN).rows.map { row -> row.filter { it.action == KeyAction.COMMIT }.joinToString("") { it.label } }
        assertEquals("ضصثقفغعهخحج", rows[0])
        assertEquals("شسيبلاتنمكط", rows[1])
        assertEquals("ذءؤرىةوزظد", rows[2])
    }

    @Test
    fun pairedKeysHoldTwoLettersLeftToRight() {
        val pairs = load(ArabicStyle.PAIRED).rows.flatten().filter { it.pair }
        assertEquals(15, pairs.size)
        assertTrue(pairs.all { it.letters.size == 2 && it.label.length == 2 })
        assertEquals(listOf("ض", "ص"), pairs.first().letters)
        assertFalse(load(ArabicStyle.ELEVEN).rows.flatten().any { it.pair })
    }

    @Test
    fun theChoiceIsStoredAndDropsTheOldOrder() {
        val store = KeyboardPrefsStore(FakePreferences().prefs)
        assertEquals(ArabicStyle.ELEVEN, store.read().arabicStyle)
        store.setOrder("ar", listOf(listOf("a")))
        store.setArabicStyle(ArabicStyle.PAIRED)
        val read = store.read()
        assertEquals(ArabicStyle.PAIRED, read.arabicStyle)
        assertFalse(read.orders.containsKey("ar"))
        store.resetAll()
        assertEquals(ArabicStyle.ELEVEN, store.read().arabicStyle)
        assertEquals(ArabicStyle.ELEVEN, ArabicStyle.byId("nonsense"))
    }

    @Test
    fun numberRowLinesUpWithElevenKeyRowsAndOffersTheOtherNumerals() {
        val eleven = load(ArabicStyle.ELEVEN)
        val row = eleven.withNumberRow(
            NumberRow.digits("ar", false), NumberRow.alternates("ar", false), NumberRow.extra("ar", eleven.rows.first().size),
        ).rows.first()
        assertEquals(11, row.size)
        assertEquals(listOf("١"), row.first().alts)
        assertEquals("@", row.last().alts.first())
        assertEquals(eleven, eleven.withNumberRow(NumberRow.digits("ar", false)).withoutNumberRow())
        assertEquals(null, NumberRow.extra("ar", load(ArabicStyle.TEN).rows.first().size))
        assertEquals(10, load(ArabicStyle.TEN).withNumberRow(NumberRow.digits("ar", true), NumberRow.alternates("ar", true)).rows.first().size)
    }
}
