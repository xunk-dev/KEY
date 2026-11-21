package com.spacekeyboard.keyboard.core.text

import org.junit.Assert.assertEquals
import org.junit.Test

class WordBoundaryTest {
    @Test
    fun lastWordIsMeasured() {
        assertEquals(5, WordBoundary.lengthBefore("hello"))
        assertEquals(5, WordBoundary.lengthBefore("say hello"))
    }

    @Test
    fun trailingSpacesAreIncludedWithTheWord() {
        assertEquals(7, WordBoundary.lengthBefore("say hello  "))
    }

    @Test
    fun punctuationIsItsOwnChunk() {
        assertEquals(1, WordBoundary.lengthBefore("hello,"))
        assertEquals(2, WordBoundary.lengthBefore("hello,,"))
    }

    @Test
    fun apostropheStaysInsideTheWord() {
        assertEquals(5, WordBoundary.lengthBefore("don't"))
    }

    @Test
    fun arabicMarksAndTatweelStayInsideTheWord() {
        assertEquals(6, WordBoundary.lengthBefore("مَكـتب"))
    }

    @Test
    fun onlySpacesOrNothingStillDeletesSomething() {
        assertEquals(3, WordBoundary.lengthBefore("   "))
        assertEquals(1, WordBoundary.lengthBefore(""))
    }
}
