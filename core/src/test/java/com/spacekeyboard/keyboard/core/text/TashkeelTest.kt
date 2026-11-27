package com.spacekeyboard.keyboard.core.text

import com.spacekeyboard.keyboard.core.layout.KeyLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TashkeelTest {
    @Test
    fun barHasEightMarksStartingWithShadda() {
        assertEquals(8, Tashkeel.MARKS.size)
        assertEquals("\u0651", Tashkeel.MARKS.first())
        assertEquals(Tashkeel.MARKS.size, Tashkeel.MARKS.toSet().size)
        assertTrue(Tashkeel.MARKS.all { it.length == 1 && Tashkeel.isMark(it[0]) })
    }

    @Test
    fun labelsTypeTheBareMark() {
        Tashkeel.LABELS.forEachIndexed { index, label ->
            assertEquals(Tashkeel.MARKS[index], KeyLabels.output(label))
            assertTrue(KeyLabels.isMarkOnly(KeyLabels.output(label)))
        }
    }

    @Test
    fun marksCanStackAfterALetterOrAMark() {
        assertTrue(Tashkeel.canMark("\u0643\u062A\u0628"))
        assertTrue(Tashkeel.canMark("\u0643\u062A\u0628\u0651"))
        assertTrue(Tashkeel.canMark("\u0643\u062A\u0628\u0670"))
    }

    @Test
    fun noBarAfterSpaceDigitLatinOrEmptyText() {
        assertFalse(Tashkeel.canMark(""))
        assertFalse(Tashkeel.canMark("\u0643\u062A\u0628 "))
        assertFalse(Tashkeel.canMark("abc"))
        assertFalse(Tashkeel.canMark("\u0661\u0662"))
        assertFalse(Tashkeel.canMark("\u0643\u062A\u0628\u060C"))
    }
}
