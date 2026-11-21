package com.spacekeyboard.keyboard.decorate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DecorCatalogTest {
    private val english = "Hello World 2026"
    private val arabic = "مرحبا بكم في الكيبورد الفضائي"

    @Test
    fun enoughStylesPerLanguageWithUniqueIds() {
        assertTrue(DecorCatalog.forLanguage(Language.EN).size >= 100)
        assertTrue(DecorCatalog.forLanguage(Language.AR).size >= 100)
        assertEquals(DecorCatalog.all.size, DecorCatalog.all.map { it.id }.toSet().size)
        assertNotNull(DecorCatalog.byId("en_bold"))
    }

    @Test
    fun everyStyleProducesReadableDefinedText() {
        DecorCatalog.all.forEach { style ->
            val sample = if (style.language == Language.EN) english else arabic
            val out = style.apply(sample)
            assertTrue(style.id, out.isNotEmpty())
            out.codePoints().forEach { assertTrue("${style.id} U+${it.toString(16)}", Character.isDefined(it) && it != 0xFFFD) }
        }
    }

    @Test
    fun englishStylesAreDistinctFromEachOther() {
        val outputs = DecorCatalog.forLanguage(Language.EN).map { it.apply(english) }
        assertTrue(outputs.toSet().size >= 100)
    }

    @Test
    fun knownEnglishMappings() {
        assertEquals("𝐇𝐞", DecorCatalog.byId("en_bold")!!.apply("He"))
        assertEquals("ℋℯ", DecorCatalog.byId("en_script")!!.apply("He"))
        assertEquals("ℎ", DecorCatalog.byId("en_italic")!!.apply("h"))
        assertEquals("①⓪", DecorCatalog.byId("en_circled")!!.apply("10"))
        assertEquals("Ａ　ｂ", DecorCatalog.byId("en_fullwidth")!!.apply("A b"))
        assertEquals("★ x ★", DecorCatalog.byId("en_frame0")!!.apply("x"))
    }

    @Test
    fun upsideDownReversesWholeTextButNotTypedLetters() {
        val style = DecorCatalog.byId("en_upsidedown")!!
        assertEquals("oɔ", style.apply("co"))
        assertEquals("ɔ", style.typed("", "c"))
    }

    @Test
    fun markStylesAppendAfterEveryNonSpaceChar() {
        assertEquals("a̶b̶ c̶", DecorCatalog.byId("en_mark0")!!.apply("ab c"))
    }

    @Test
    fun arabicStylesNeverBreakJoining() {
        DecorCatalog.forLanguage(Language.AR).forEach { style ->
            val out = style.apply(arabic)
            val stripped = out.filter { it.isLetter() || it.isWhitespace() }
            val core = ArabicJoin.plain(stripped)
            assertTrue(style.id, ArabicJoin.plain(out).filter { ArabicJoin.isLetter(it) || it == ' ' }.contains("مرحبا بكم"))
            assertFalse(style.id, core.isEmpty())
        }
    }

    @Test
    fun arabicPlainTextIsRecoverable() {
        DecorCatalog.forLanguage(Language.AR).forEach { style ->
            val out = style.apply(arabic)
            val body = out.filter { ArabicJoin.isLetter(it) || it == ' ' || it == ArabicJoin.TATWEEL || ArabicJoin.isDiacritic(it) }
            assertEquals(style.id, arabic, ArabicJoin.plain(body).trim())
        }
    }

    @Test
    fun tatweelOnlyBetweenConnectedLetters() {
        val out = DecorCatalog.byId("ar_t1_s0_m0")!!.apply("بكم ار")
        assertEquals("بـكـم ار", out)
        assertFalse(out.contains("ـ "))
    }

    @Test
    fun lookalikesKeepTheJoiningClass() {
        ArabicJoin.lookalikes.forEach { (from, to) ->
            assertEquals("$from", ArabicJoin.isDual(from), ArabicJoin.isDual(to))
            assertTrue(ArabicJoin.isLetter(to))
        }
    }

    @Test
    fun diacriticsFollowEachLetterAndCycle() {
        assertEquals("بَتَ", DecorCatalog.byId("ar_t0_s0_m1")!!.apply("بت"))
        assertEquals("بَتُثِبَ", DecorCatalog.byId("ar_t0_s0_m4")!!.apply("بتثب"))
    }

    @Test
    fun directTypingAddsTatweelOnlyAfterAJoiningLetter() {
        val style = DecorCatalog.byId("ar_t2_s0_m0")!!
        assertEquals("ب", style.typed("", "ب"))
        assertEquals("\u0640\u0640م", style.typed("ب", "م"))
        assertEquals("ا", style.typed("ب ", "ا"))
        assertEquals("م", style.typed("ا", "م"))
        assertEquals("\u0640\u0640م", style.typed("ب", "م"))
        assertEquals("1", style.typed("ب", "1"))
    }

    @Test
    fun directTypingEnglishMapsEachLetter() {
        assertEquals("𝐚", DecorCatalog.byId("en_bold")!!.typed("", "a"))
        assertEquals("a", DecorCatalog.byId("en_plain")!!.typed("", "a"))
    }
}
