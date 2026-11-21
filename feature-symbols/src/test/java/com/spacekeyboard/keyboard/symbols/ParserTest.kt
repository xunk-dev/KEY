package com.spacekeyboard.keyboard.symbols

import com.spacekeyboard.keyboard.symbols.data.SymbolParser
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserTest {
    @Test
    fun symbolsSkipHeadersBadRowsAndUnknownCategories() {
        val parsed = SymbolParser.symbols(TestData.symbolLines)
        assertEquals(listOf(0x20AC, 0x2605, 0x2606, 0x2765, 0x2190, 0x3C0, 0x2022), parsed.map { it.codePoint })
    }

    @Test
    fun symbolFieldsAreSplitIntoWords() {
        val euro = SymbolParser.symbols(TestData.symbolLines).first()
        assertEquals(SymbolCategory.CURRENCY, euro.category)
        assertEquals(listOf("currency", "euro"), euro.englishKeywords)
        assertEquals(listOf("عملة", "يورو"), euro.arabicKeywords)
        assertEquals("€", euro.text)
        assertEquals("U+20AC", euro.hex)
    }

    @Test
    fun emptyKeywordColumnsGiveEmptyLists() {
        val heart = SymbolParser.symbols(TestData.symbolLines).first { it.codePoint == 0x2765 }
        assertTrue(heart.englishKeywords.isEmpty())
        assertTrue(heart.arabicKeywords.isEmpty())
        assertEquals("rotated heavy black heart bullet", heart.label(false))
    }

    @Test
    fun labelPrefersArabicOnlyWhenAsked() {
        val star = SymbolParser.symbols(TestData.symbolLines).first { it.codePoint == 0x2605 }
        assertEquals("نجمة سوداء", star.label(true))
        assertEquals("black star", star.label(false))
        val white = SymbolParser.symbols(TestData.symbolLines).first { it.codePoint == 0x2606 }
        assertEquals("white star", white.label(true))
    }

    @Test
    fun featuredKeepsOrderAndDropsInvalidCodes() {
        assertEquals(listOf(0x2605, 0x20AC), SymbolParser.featured(TestData.featuredLines))
    }

    @Test
    fun facesParseCategoriesAndSkipUnknown() {
        val faces = SymbolParser.faces(TestData.faceLines)
        assertEquals(listOf("^_^", "(◔‿◔)", "(╥﹏╥)"), faces.map { it.text })
        assertEquals(FaceCategory.CLASSIC, faces.first().category)
    }

    @Test
    fun faceKeywordsJoinEnglishAndArabic() {
        val keywords = SymbolParser.faceKeywords(TestData.categoryLines)
        assertEquals("happy smile joy سعيد ابتسامة", keywords[FaceCategory.HAPPY])
        assertEquals(3, keywords.size)
    }
}
