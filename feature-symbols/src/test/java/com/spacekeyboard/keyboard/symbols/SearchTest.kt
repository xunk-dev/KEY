package com.spacekeyboard.keyboard.symbols

import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.search.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    private val book = TestData.data().symbols
    private val faces = TestData.data().faces

    private fun codes(query: String) = book.search(query).map { it.codePoint }

    @Test
    fun emptyAndBlankQueriesFindNothing() {
        assertTrue(codes("").isEmpty())
        assertTrue(codes("   ").isEmpty())
    }

    @Test
    fun englishNameAndKeywordsMatch() {
        assertEquals(listOf(0x20AC), codes("euro"))
        assertEquals(listOf(0x2605, 0x2606), codes("star"))
        assertEquals(listOf(0x2190), codes("arrow left"))
    }

    @Test
    fun wordStartRanksAboveInsideMatches() {
        val result = codes("eur")
        assertEquals(0x20AC, result.first())
        assertEquals(listOf(0x2605, 0x2606), codes("sta"))
    }

    @Test
    fun arabicQueriesIgnoreDiacriticsAndLetterVariants() {
        assertEquals(listOf(0x20AC), codes("يُورو"))
        assertEquals(listOf(0x2605), codes("مفضل"))
        assertEquals(listOf(0x2190), codes("سهم"))
        assertEquals(listOf(0x2605), codes("نَجْمَه سوداء"))
    }

    @Test
    fun hexCodesNeedAPrefixWhenShort() {
        assertEquals(listOf(0x2605), codes("U+2605"))
        assertEquals(listOf(0x2605), codes("u+2605"))
        assertEquals(listOf(0x2605), codes("0x2605"))
        assertEquals(listOf(0x2605), codes("2605"))
        assertEquals(listOf(0x3C0), codes("U+3c0"))
        assertTrue(codes("3c0").isEmpty())
    }

    @Test
    fun aPastedSymbolFindsItself() {
        assertEquals(listOf(0x20AC), codes("€"))
        assertEquals(listOf(0x2022), codes("•"))
    }

    @Test
    fun unknownQueriesFindNothing() {
        assertTrue(codes("zzzz").isEmpty())
        assertTrue(codes("star zzzz").isEmpty())
    }

    @Test
    fun limitCapsTheResults() {
        assertEquals(1, book.search("star", limit = 1).size)
    }

    @Test
    fun categoriesAndFeaturedFollowTheData() {
        assertEquals(listOf(0x2605, 0x20AC), book.featured.map { it.codePoint })
        assertEquals(listOf(0x2605, 0x20AC), book.category(SymbolCategory.FEATURED).map { it.codePoint })
        assertEquals(listOf(0x2605, 0x2606), book.category(SymbolCategory.STARS).map { it.codePoint })
        assertTrue(book.category(SymbolCategory.MUSIC).isEmpty())
        assertEquals("€", book.find(0x20AC)?.text)
    }

    @Test
    fun faceSearchUsesTextAndCategoryWords() {
        assertEquals(listOf("(╥﹏╥)"), faces.search("cry").map { it.text })
        assertEquals(listOf("(╥﹏╥)"), faces.search("حزين").map { it.text })
        assertEquals(listOf("^_^"), faces.search("^_^").map { it.text })
        assertEquals(listOf("^_^", "(◔‿◔)"), faces.search("smile").map { it.text })
        assertTrue(faces.search("").isEmpty())
        assertEquals(listOf("(◔‿◔)"), faces.category(FaceCategory.HAPPY).map { it.text })
    }

    @Test
    fun normalizerFoldsArabicAndSeparators() {
        assertEquals("ابتسامه", TextNormalizer.fold("ٱبْتِسَامَة"))
        assertEquals("left pointing", TextNormalizer.normalize("Left-Pointing"))
        assertEquals("123", TextNormalizer.fold("١٢٣"))
        assertEquals(listOf("a", "b"), TextNormalizer.tokens("a_b"))
    }
}
