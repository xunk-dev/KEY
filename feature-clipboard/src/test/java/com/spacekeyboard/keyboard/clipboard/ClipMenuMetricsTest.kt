package com.spacekeyboard.keyboard.clipboard

import com.spacekeyboard.keyboard.clipboard.ui.ClipMenuMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipMenuMetricsTest {
    @Test
    fun menuNeverExceedsThePanelForAnyRowCountOrHeight() {
        for (rows in 0..8) for (available in 0..400 step 7) for (wanted in -50..450 step 13) {
            val height = ClipMenuMetrics.height(rows, available)
            val top = ClipMenuMetrics.top(wanted, height, available)
            assertTrue(height <= ClipMenuMetrics.fullHeight(rows))
            assertTrue(height <= maxOf(available, 0))
            if (height + ClipMenuMetrics.MARGIN * 2 <= available) {
                assertTrue(top >= ClipMenuMetrics.MARGIN)
                assertTrue(top + height + ClipMenuMetrics.MARGIN <= available)
            }
        }
    }

    @Test
    fun roomyPanelShowsEverythingAndTinyOneScrolls() {
        assertEquals(48 + 44 * 4, ClipMenuMetrics.height(4, 400))
        assertEquals(100 - 8, ClipMenuMetrics.height(4, 100))
        assertEquals(4, ClipMenuMetrics.top(-20, 100, 400))
        assertEquals(400 - 100 - 4, ClipMenuMetrics.top(390, 100, 400))
    }
}
