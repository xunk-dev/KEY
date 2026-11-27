package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RailSymbolsTest {
    @Test
    fun everyRequestedSymbolIsOnTheRailOnce() {
        val labels = RailSymbols.items.map { it.label }
        assertEquals(labels.size, labels.toSet().size)
        assertTrue(labels.containsAll("+-\u00D7\u00F7=/*_%$#@():,.".map { it.toString() }))
    }

    @Test
    fun railHasMoreSymbolsThanItShowsAtOnce() {
        assertTrue(RailSymbols.items.size > 3)
    }
}
