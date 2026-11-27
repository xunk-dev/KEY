package com.spacekeyboard.keyboard.core.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicGapsTest {
    private fun load(id: String, dir: String = "raw"): LayoutSpec =
        LayoutParser.parse(File("src/main/res/$dir/layout_$id.json").readText(Charsets.UTF_8))

    private fun keys(id: String) = load(id).rows.flatten()

    @Test
    fun arabicHasLamAlefTatweelAndMarks() {
        val ar = keys("ar")
        assertTrue(ar.none { it.label == "لا" })
        assertEquals("لا", ar.first { it.label == "ل" }.alts.first())
        assertTrue(ar.first { it.label == "،" }.alts.contains("\u0640"))
        val marks = ar.first { it.label == "ظ" }.alts
        assertEquals(9, marks.size)
        assertTrue(marks.all { KeyLabels.isMarkOnly(KeyLabels.output(it)) })
    }

    @Test
    fun arabicReachesPersianAndUrduLetters() {
        val held = keys("ar").flatMap { it.alts }
        assertTrue(held.containsAll(listOf("پ", "چ", "گ", "ژ", "ڤ", "ی")))
    }

    @Test
    fun persianAndArabiziLayoutsHaveTheEssentialKeys() {
        listOf("fa", "az").forEach { id ->
            val all = keys(id)
            listOf(KeyAction.DELETE, KeyAction.SPACE, KeyAction.ENTER, KeyAction.OPEN_SYMBOLS, KeyAction.NUMBERS).forEach { action ->
                assertEquals("$id $action", 1, all.count { it.action == action })
            }
            val totals = load(id).rows.map { row -> row.sumOf { it.weight.toDouble() } }
            totals.forEach { assertEquals(id, totals.first(), it, 0.001) }
        }
        assertTrue(keys("fa").any { it.label == KeyLabels.ZWNJ_KEY })
    }

    @Test
    fun editLayoutsMatchInBothLanguages() {
        val en = load("edit")
        val ar = load("edit", "raw-ar")
        assertEquals(en.rows.flatten().map { it.id }, ar.rows.flatten().map { it.id })
        assertEquals(en.rows.flatten().map { it.action }, ar.rows.flatten().map { it.action })
        val actions = en.rows.flatten().map { it.action }
        assertTrue(actions.containsAll(listOf(KeyAction.SELECT_ALL, KeyAction.COPY, KeyAction.CUT, KeyAction.PASTE, KeyAction.UNDO, KeyAction.REDO)))
        assertTrue(actions.filter { it != KeyAction.COMMIT }.size == actions.size)
    }

    @Test
    fun numberRowIsAddedAndRemoved() {
        val base = load("en")
        val withRow = base.withNumberRow(NumberRow.digits("en", false))
        assertEquals(base.rows.size + 1, withRow.rows.size)
        assertEquals(10, withRow.rows[0].size)
        assertEquals(base.rows[0].sumOf { it.weight.toDouble() }, withRow.rows[0].sumOf { it.weight.toDouble() }, 0.001)
        assertEquals(base, withRow.withoutNumberRow())
        assertEquals(base, base.withoutNumberRow())
    }

    @Test
    fun nativeDigitsFollowTheLanguage() {
        assertEquals("١", NumberRow.digits("ar", true)[0])
        assertEquals("۱", NumberRow.digits("fa", true)[0])
        assertEquals("1", NumberRow.digits("en", true)[0])
        assertEquals("1", NumberRow.digits("ar", false)[0])
    }

    @Test
    fun digitsModeMapsToStoredPrefs() {
        val store = KeyboardPrefsStore(FakePreferences().prefs)
        assertEquals(DigitsMode.LATIN, store.read().typing.digits)
        store.setDigitsMode(DigitsMode.NATIVE)
        assertEquals(DigitsMode.NATIVE, store.read().typing.digits)
        store.setDigitsMode(DigitsMode.HIDDEN)
        assertEquals(DigitsMode.HIDDEN, store.read().typing.digits)
        store.setDigitsMode(DigitsMode.LATIN)
        assertEquals(DigitsMode.LATIN, store.read().typing.digits)
    }

    @Test
    fun languageCycleFollowsTheEnabledList() {
        assertEquals("en", LayoutCatalog.next("ar"))
        assertEquals("fa", LayoutCatalog.next("en", listOf("ar", "en", "fa")))
        assertEquals("ar", LayoutCatalog.next("fa", listOf("ar", "en", "fa")))
    }

    @Test
    fun typingPrefsRoundTripAndLanguagesStaySane() {
        val store = KeyboardPrefsStore(FakePreferences().prefs)
        store.setNumberRow(true)
        store.setNativeDigits(true)
        store.setDoubleSpace(false)
        store.setIncognito(true)
        store.setLanguageEnabled("az", true)
        store.setLanguageEnabled("fa", true)
        store.setLanguageEnabled("ar", false)
        val read = store.read()
        assertEquals(TypingPrefs(true, true, false, true, true), read.typing)
        assertEquals(listOf("ar", "en", "fa", "az"), read.languages)
        store.setLanguageEnabled("az", false)
        assertEquals(listOf("ar", "en", "fa"), store.read().languages)
        store.resetAll()
        assertEquals(KeyboardPrefs(), store.read())
    }

    @Test
    fun savedLanguageOutsideTheEnabledListFallsBack() {
        val store = KeyboardPrefsStore(FakePreferences().prefs)
        store.setLanguageEnabled("fa", true)
        store.setLanguage("fa")
        store.setLanguageEnabled("fa", false)
        assertEquals("ar", store.read().language)
    }

    @Test
    fun editActionsAreCommands() {
        assertTrue(KeyAction.COPY.isEditCommand)
        assertTrue(!KeyAction.EDIT.isEditCommand)
        assertTrue(!KeyAction.COMMIT.isEditCommand)
        assertTrue(KeyAction.UNDO.isEditCommand)
        assertTrue(KeyAction.REDO.isEditCommand)
    }
}
