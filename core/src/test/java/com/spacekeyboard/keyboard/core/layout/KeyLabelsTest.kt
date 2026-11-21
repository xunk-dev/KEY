package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyLabelsTest {
    @Test
    fun markKeysCommitOnlyTheMark() {
        assertEquals("\u064E", KeyLabels.output("\u0640\u064E"))
        assertEquals("\u0651", KeyLabels.output("\u0640\u0651"))
    }

    @Test
    fun halfSpaceKeyCommitsZwnj() {
        assertEquals("\u200C", KeyLabels.output(KeyLabels.ZWNJ_KEY))
    }

    @Test
    fun ordinaryLabelsAreUntouched() {
        assertEquals("\u0640", KeyLabels.output("\u0640"))
        assertEquals("لا", KeyLabels.output("لا"))
        assertEquals("a", KeyLabels.output("a"))
    }

    @Test
    fun marksAreDetected() {
        assertTrue(KeyLabels.isMarkOnly("\u064E"))
        assertFalse(KeyLabels.isMarkOnly("ب"))
        assertFalse(KeyLabels.isMarkOnly(""))
    }
}
