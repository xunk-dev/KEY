package com.spacekeyboard.keyboard.core.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsSearchTest {
    private val items = listOf("الحافظة نسخ لصق", "النسخ الاحتياطي كلمة سر", "Themes colors")

    private fun find(query: String) = SettingsSearch.filter(query, items) { it }

    @Test
    fun blankQueryKeepsEverything() {
        assertEquals(items, find("  "))
    }

    @Test
    fun matchesPartOfAWord() {
        assertEquals(listOf(items[2]), find("color"))
    }

    @Test
    fun everyWordMustMatch() {
        assertEquals(listOf(items[1]), find("نسخ سر"))
        assertEquals(emptyList<String>(), find("نسخ themes"))
    }

    @Test
    fun ignoresArabicSpellingVariantsAndCase() {
        assertEquals(listOf(items[0]), find("الحافظه"))
        assertEquals(listOf(items[2]), find("THEMES"))
    }

    @Test
    fun noMatchGivesEmptyList() {
        assertEquals(emptyList<String>(), find("zzz"))
    }
}
