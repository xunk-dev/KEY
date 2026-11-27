package com.spacekeyboard.keyboard.clipboard

import com.spacekeyboard.keyboard.clipboard.ui.ClipLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipLabelsTest {
    @Test
    fun previewFlattensWhitespace() {
        assertEquals("a b c", ClipLabels.preview("a\n b\t\tc "))
    }

    @Test
    fun previewShortensLongText() {
        val long = ClipLabels.preview("x".repeat(500))
        assertEquals(91, long.length)
        assertTrue(long.endsWith("…"))
    }

    @Test
    fun everyBlockingStatusHasAMessageAndSuccessHasNone() {
        EditStatus.entries.forEach {
            val message = ClipLabels.problem(it)
            val silent = it == EditStatus.OK || it == EditStatus.UNCHANGED
            assertEquals(it.name, silent, message == null)
        }
    }
}
