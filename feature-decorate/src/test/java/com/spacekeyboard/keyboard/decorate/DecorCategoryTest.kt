package com.spacekeyboard.keyboard.decorate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecorCategoryTest {
    private fun ids(category: DecorCategory, language: Language): List<String> =
        DecorCatalog.forLanguage(language).filter(category::matches).map { it.id }

    @Test
    fun allKeepsEveryStyle() {
        Language.entries.forEach { assertEquals(DecorCatalog.forLanguage(it).size, ids(DecorCategory.ALL, it).size) }
    }

    @Test
    fun arabicFramesAreFramedStylesOnly() {
        val frames = ids(DecorCategory.FRAMES, Language.AR)
        assertTrue(frames.isNotEmpty())
        assertTrue(frames.all { it.startsWith("ar_f") })
    }

    @Test
    fun arabicMarksAndStretchSkipFramedStyles() {
        listOf(DecorCategory.MARKS, DecorCategory.STRETCH).forEach { category ->
            val found = ids(category, Language.AR)
            assertTrue(found.isNotEmpty())
            assertTrue(found.none { it.startsWith("ar_f") })
        }
    }

    @Test
    fun englishFontsExcludePlainFramesAndMarks() {
        val fonts = ids(DecorCategory.FONTS, Language.EN)
        assertTrue(fonts.isNotEmpty())
        assertTrue(fonts.none { it == "en_plain" || "frame" in it || it.startsWith("en_mark") })
    }

    @Test
    fun englishFramesAndMarksAreFound() {
        assertTrue(ids(DecorCategory.FRAMES, Language.EN).all { "frame" in it })
        assertTrue(ids(DecorCategory.MARKS, Language.EN).all { it.startsWith("en_mark") })
    }
}
