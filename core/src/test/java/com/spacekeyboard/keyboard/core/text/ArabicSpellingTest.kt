package com.spacekeyboard.keyboard.core.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArabicSpellingTest {
    private fun spelling(vararg words: String): ArabicSpelling {
        val set = words.toSet()
        return ArabicSpelling { it in set }
    }

    private fun fixed(spelling: ArabicSpelling, before: String): String? = spelling.fix(before)?.fixed

    @Test
    fun finalHaBecomesTaMarbuta() {
        val s = spelling("مدرسة", "المدرسة", "سيارة")
        assertEquals("مدرسة", fixed(s, "ذهبت الى مدرسه"))
        assertEquals("المدرسة", fixed(s, "المدرسه"))
        assertEquals("سيارة", fixed(s, "سياره"))
    }

    @Test
    fun finalTaMarbutaBecomesHaWhenOnlyThatIsAWord() {
        assertEquals("وجه", fixed(spelling("وجه"), "وجة"))
    }

    @Test
    fun finalYaAndAlefMaqsura() {
        val s = spelling("مستشفى", "مصطفى")
        assertEquals("مستشفى", fixed(s, "مستشفي"))
        assertEquals("مصطفى", fixed(s, "مصطفي"))
        assertEquals("مصطفي", fixed(spelling("مصطفي"), "مصطفى"))
    }

    @Test
    fun initialAlefTakesItsHamza() {
        val s = spelling("أكل", "إلى", "آمن", "أحمد")
        assertEquals("أكل", fixed(s, "اكل"))
        assertEquals("إلى", fixed(s, "الى"))
        assertEquals("إلى", fixed(s, "الي"))
        assertEquals("أحمد", fixed(s, "احمد"))
        assertEquals("أحمد", fixed(s, "إحمد"))
    }

    @Test
    fun hamzaSeatInTheMiddleAndAtTheEnd() {
        val s = spelling("مسؤول", "مسألة", "شيء", "قرآن")
        assertEquals("مسؤول", fixed(s, "مسئول"))
        assertEquals("مسؤول", fixed(s, "مسأول"))
        assertEquals("مسألة", fixed(s, "مساله"))
        assertEquals("مسألة", fixed(s, "مسئلة"))
        assertEquals("شيء", fixed(s, "شيئ"))
        assertEquals("قرآن", fixed(s, "قران"))
    }

    @Test
    fun prefixesAreKeptAndStemIsRepaired() {
        val s = spelling("مدرسة", "المدرسة", "أكل", "مسؤول")
        assertEquals("والمدرسة", fixed(s, "والمدرسه"))
        assertEquals("بالمدرسة", fixed(s, "بالمدرسه"))
        assertEquals("للمدرسة", fixed(s, "للمدرسه"))
        assertEquals("والأكل", fixed(spelling("أكل"), "والاكل"))
        assertEquals("فمسؤول", fixed(s, "فمسئول"))
    }

    @Test
    fun knownWordsAreNeverTouched() {
        val s = spelling("كتابه", "كتابة", "علي", "على", "وكتابه")
        assertNull(s.fix("كتابه"))
        assertNull(s.fix("علي"))
        assertNull(s.fix("وكتابه"))
    }

    @Test
    fun knownStemMeansTheWordIsLeftAlone() {
        val s = spelling("بيته", "بيتة", "ولده")
        assertNull(s.fix("وبيته"))
    }

    @Test
    fun ambiguousFixesAreSkipped() {
        val s = spelling("أمل", "إمل")
        assertNull(s.fix("امل"))
    }

    @Test
    fun oneChangeBeatsTwo() {
        val s = spelling("مسألة", "مساله")
        assertNull(s.fix("مساله"))
        assertEquals("مسألة", fixed(spelling("مسألة"), "مساله"))
    }

    @Test
    fun skipsWhatItShouldNotTouch() {
        val s = spelling("مدرسة", "أكل")
        assertNull(s.fix("ما"))
        assertNull(s.fix("اك"))
        assertNull(s.fix("abcمدرسه"))
        assertNull(s.fix("مَدرسه"))
        assertNull(s.fix("12مدرسه"))
        assertNull(s.fix("مدرسه "))
        assertNull(s.fix(""))
        assertNull(s.fix("مدرســه"))
    }

    @Test
    fun wordAfterPunctuationAndNewlineIsFixed() {
        val s = spelling("مدرسة")
        assertEquals("مدرسة", fixed(s, "قال: مدرسه"))
        assertEquals("مدرسة", fixed(s, "سطر\nمدرسه"))
        assertEquals("مدرسة", fixed(s, "(مدرسه"))
        assertEquals("مدرسة", fixed(s, "،مدرسه"))
    }

    @Test
    fun originalIsReportedForUndo() {
        val fix = spelling("مدرسة").fix("في مدرسه")!!
        assertEquals("مدرسه", fix.original)
        assertEquals("مدرسة", fix.fixed)
    }

    @Test
    fun unknownWordsWithNoKnownVariantStay() {
        val s = spelling("مدرسة")
        assertNull(s.fix("فلسطين"))
        assertNull(s.fix("كمبيوتر"))
    }
}
