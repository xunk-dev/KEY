package com.spacekeyboard.keyboard.emoji.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PanelIconTest {
    @Test
    fun sharedIconsAreNeverReusedByTwoPanelIcons() {
        val shared = PanelIcon.entries.mapNotNull { it.space }
        assertEquals(shared.size, shared.toSet().size)
    }

    @Test
    fun onlyIconsMissingFromTheDesignSetAreDrawnByHand() {
        val drawn = PanelIcon.entries.filter { it.space == null }
        assertEquals(listOf(PanelIcon.CLOCK, PanelIcon.CLOSE, PanelIcon.LOCK), drawn)
        assertNull(PanelIcon.CLOSE.space)
    }
}
