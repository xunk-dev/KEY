package com.spacekeyboard.keyboard.emoji.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class HubTabOrderTest {
    @Test
    fun arrangeFollowsTheSavedOrderForAvailableTabsOnly() {
        assertEquals(listOf("i", "s", "h"), HubTabOrder.arrange(listOf("i", "e", "s", "h"), listOf("s", "h", "i")))
        assertEquals(listOf("s", "e"), HubTabOrder.arrange(listOf("s"), listOf("e", "s")))
    }

    @Test
    fun mergeKeepsHiddenTabsInPlaceAndLosesNothing() {
        assertEquals(listOf("i", "e", "s", "h"), HubTabOrder.merge(listOf("e", "s", "h", "i"), listOf("i", "e", "s", "h")))
        assertEquals(listOf("e", "i", "s", "h"), HubTabOrder.merge(listOf("e", "s", "h", "i"), listOf("i", "s", "h")))
    }

    @Test
    fun moveToClampsAndSlotsMirrorInRtl() {
        assertEquals(listOf("c", "a", "b"), HubTabOrder.moveTo(listOf("a", "b", "c"), "c", -3))
        assertEquals(listOf("b", "c", "a"), HubTabOrder.moveTo(listOf("a", "b", "c"), "a", 9))
        assertEquals(0, HubTabOrder.slotAt(10f, 400f, 4, false))
        assertEquals(3, HubTabOrder.slotAt(10f, 400f, 4, true))
        assertEquals(300f, HubTabOrder.slotLeft(0, 100f, 4, true), 0.001f)
    }
}
