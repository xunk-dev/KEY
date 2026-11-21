package com.spacekeyboard.keyboard.decorate

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderabilityTest {
    @Test
    fun acceptsWhenEveryVisibleCodePointHasAGlyph() {
        assertTrue(Renderability.ok("ab c") { true })
    }

    @Test
    fun rejectsWhenOneCodePointIsMissing() {
        assertFalse(Renderability.ok("a𝐚") { it < 0x80 })
    }

    @Test
    fun ignoresSpacesAndCombiningMarks() {
        assertTrue(Renderability.ok("a̶ َ") { it == 'a'.code })
    }

    @Test
    fun probesCoverBothLanguages() {
        assertTrue(Renderability.probe(Language.EN).isNotEmpty())
        assertTrue(Renderability.probe(Language.AR).any { ArabicJoin.isLetter(it) })
    }
}
