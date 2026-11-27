package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class PageLabelsTest {
    private fun key(id: String, label: String, action: KeyAction, alts: List<String> = emptyList()) =
        KeySpec(id, label, action, 1f, false, alts)

    private val page = LayoutSpec(
        "num",
        listOf(
            listOf(
                key("abc", "ABC", KeyAction.NUMBERS),
                key("q", "?", KeyAction.COMMIT, listOf("؟")),
                key("c", ",", KeyAction.COMMIT, listOf("،")),
                key("at", "@", KeyAction.COMMIT),
            ),
        ),
    )

    @Test
    fun arabicPageUsesArabicPunctuationAndKeepsLatinOnHold() {
        val keys = PageLabels.page(page, "ar").rows.first()
        assertEquals("أبج", keys[0].label)
        assertEquals("؟", keys[1].label)
        assertEquals(listOf("?"), keys[1].alts)
        assertEquals("،", keys[2].label)
        assertEquals(listOf(","), keys[2].alts)
        assertEquals("@", keys[3].label)
    }

    @Test
    fun englishPageIsUnchangedExceptNothing() {
        val keys = PageLabels.page(page, "en").rows.first()
        assertEquals("ABC", keys[0].label)
        assertEquals("?", keys[1].label)
    }

    @Test
    fun lettersPageNumbersKeyOnlyOpensThePad() {
        val letters = LayoutSpec("ar", listOf(listOf(key("num", "123", KeyAction.NUMBERS))))
        assertEquals("123", PageLabels.letters(letters, "ar").rows.first().first().label)
        assertEquals("123", PageLabels.letters(letters, "en").rows.first().first().label)
    }
}
