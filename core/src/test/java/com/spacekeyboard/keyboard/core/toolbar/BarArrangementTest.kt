package com.spacekeyboard.keyboard.core.toolbar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BarArrangementTest {
    private val languages = listOf("ar", "en")
    private val default = BarArrangement.default(languages)

    private fun widths(arrangement: BarArrangement) = arrangement.all.associateWith { 44f }

    @Test
    fun defaultPutsToolsOnTheLeftAndLanguagesOnTheRightWhateverTheLanguage() {
        assertEquals(listOf("tools", "emoji", "translate", "clipboard", "macro_play"), default.left)
        assertEquals(listOf("globe", "lang:en", "lang:ar"), default.right)
    }

    @Test
    fun placeMovesAnySlotAcrossTheGapWithoutLosingOrDuplicatingAny() {
        val moved = default.place("clipboard", true, 0).place("lang:ar", false, 1)
        assertEquals(default.all.toSet(), moved.all.toSet())
        assertEquals(default.all.size, moved.all.size)
        assertEquals(listOf("tools", "lang:ar", "emoji", "translate", "macro_play"), moved.left)
        assertEquals(listOf("clipboard", "globe", "lang:en"), moved.right)
    }

    @Test
    fun placeIgnoresUnknownSlotsAndClampsTheIndex() {
        assertSame(default, default.place("bogus", true, 0))
        assertEquals("tools", default.place("tools", true, 99).right.last())
        assertEquals("globe", default.place("globe", false, -5).left.first())
    }

    @Test
    fun resolveRoundTripsAndFallsBackToTheDefault() {
        val custom = default.place("globe", false, 0)
        assertEquals(custom, BarArrangement.resolve(custom.encode(), languages))
        assertEquals(default, BarArrangement.resolve(null, languages))
        assertEquals(default, BarArrangement.resolve("garbage", languages))
        assertEquals(default, BarArrangement.resolve("a|b|c", languages))
    }

    @Test
    fun resolveDropsUnknownAndDuplicateSlotsAndAppendsMissingOnes() {
        val resolved = BarArrangement.resolve("emoji,bogus,emoji|globe", languages)
        assertEquals(default.all.toSet(), resolved.all.toSet())
        assertEquals(default.all.size, resolved.all.size)
        assertEquals(listOf("emoji", "tools", "translate", "clipboard", "macro_play"), resolved.left)
        assertEquals(listOf("globe", "lang:en", "lang:ar"), resolved.right)
    }

    @Test
    fun aNewlyEnabledLanguageJoinsTheOtherLanguageButtons() {
        val saved = default.place("globe", false, 0).encode()
        val withThree = BarArrangement.resolve(saved, listOf("ar", "en", "fa"))
        assertTrue("lang:fa" in withThree.right)
        assertEquals(withThree.right.indexOf("lang:fa") + 1, withThree.right.indexOf("lang:en"))
    }

    @Test
    fun aDisabledLanguageVanishesAndComesBackWhereItWas() {
        val three = BarArrangement.resolve(null, listOf("ar", "en", "fa"))
        val raw = three.place("lang:fa", false, 0).encode()
        val two = BarArrangement.resolve(raw, languages)
        assertTrue("lang:fa" !in two.all)
        val back = BarArrangement.resolve(raw, listOf("ar", "en", "fa"))
        assertEquals("lang:fa", back.left.first())
    }

    @Test
    fun dropAtPicksTheNearestSlotAndNeverLeavesAHole() {
        val total = 400f
        val widths = widths(default)
        val toTheFarLeft = default.dropAt("lang:ar", 10f, widths, total)
        assertEquals("lang:ar", toTheFarLeft.left.first())
        val toTheFarRight = default.dropAt("tools", 399f, widths, total)
        assertEquals("tools", toTheFarRight.right.last())
        val between = default.dropAt("clipboard", 400f - 3 * 44f + 30f, widths, total)
        assertEquals(default.all.toSet(), between.all.toSet())
        assertEquals(default.all.size, between.all.size)
    }

    @Test
    fun dropAtOnItsOwnSpotChangesNothing() {
        val total = 400f
        val widths = widths(default)
        default.all.forEach { slot ->
            val x = (if (slot in default.left) default.left.indexOf(slot) * 44f else total - (default.right.size - default.right.indexOf(slot)) * 44f) + 22f
            assertEquals(slot, default, default.dropAt(slot, x, widths, total))
        }
    }

    @Test
    fun dropAtOnlySwapsOncePastTheNeighbourCentre() {
        val widths = widths(default)
        assertEquals(default, default.dropAt("lang:en", 329f, widths, 400f))
        assertEquals(listOf("lang:en", "globe", "lang:ar"), default.dropAt("lang:en", 289f, widths, 400f).right)
    }

    @Test
    fun stepMovesOneSlotAndCrossesTheGapAtTheEdges() {
        assertEquals(listOf("emoji", "tools", "translate", "clipboard", "macro_play"), default.step("tools", 1).left)
        val across = default.step("macro_play", 1)
        assertEquals("macro_play", across.right.first())
        assertEquals("macro_play", across.step("macro_play", -1).left.last())
        assertSame(default, default.step("tools", -1))
        assertSame(default, default.step("lang:ar", 1))
    }

    @Test
    fun hiddenWhileTypingCoversTheGlobeAndEveryLanguage() {
        assertTrue(BarSlots.hiddenWhileTyping("globe"))
        assertTrue(BarSlots.hiddenWhileTyping(BarSlots.language("ar")))
        assertTrue(!BarSlots.hiddenWhileTyping("tools"))
    }

    @Test
    fun aTileCanMoveBetweenTheBarAndThePageAndSurvivesSaving() {
        val onBar = default.bring("float")
        assertEquals("float", onBar.left.last())
        val off = onBar.sendOff("emoji")
        assertTrue("emoji" !in off.all)
        assertEquals(off, BarArrangement.resolve(off.encode(), languages))
        assertEquals(off.encode().split("|").size, 3)
        val back = off.bring("emoji")
        assertTrue("emoji" in back.all && "emoji" !in back.off)
    }

    @Test
    fun toolsGlobeAndLanguagesNeverLeaveTheBar() {
        listOf("tools", "globe", "lang:en").forEach { assertSame(default, default.sendOff(it)) }
        assertSame(default, default.bring("tools"))
    }

    @Test
    fun theBarHasAMaximumOfMovableItems() {
        var bar = default
        listOf("mode", "theme", "decorate", "edit", "resize", "float").forEach { bar = bar.bring(it) }
        assertEquals(BarSlots.MAX_MOVABLE, bar.movableCount)
        assertTrue(!bar.canBring("dim"))
        assertSame(bar, bar.bring("dim"))
    }

    @Test
    fun oldTwoPartSavesStillKeepAllThreeDefaultTiles() {
        val old = BarArrangement.resolve("tools,emoji,translate,clipboard|globe,lang:en,lang:ar", languages)
        assertEquals(default, old)
    }

    @Test
    fun aTileSentOffIsNotBroughtBackByMissingFill() {
        val raw = default.sendOff("translate").encode()
        assertTrue("translate" !in BarArrangement.resolve(raw, languages).all)
    }

    @Test
    fun theQuickPlayButtonIsPartOfTheDefaultBarButCannotBeSentOff() {
        assertTrue("macro_play" in default.all)
        assertTrue(!BarSlots.movable("macro_play"))
        assertSame(default, default.sendOff("macro_play"))
    }

    @Test
    fun aBarSavedBeforeQuickPlayGetsTheButtonAtTheEndOfTheLeftSide() {
        val saved = "clipboard,tools,emoji,translate|globe,lang:en,lang:ar"
        val resolved = BarArrangement.resolve(saved, languages)
        assertEquals(listOf("clipboard", "tools", "emoji", "translate", "macro_play"), resolved.left)
        assertEquals(listOf("globe", "lang:en", "lang:ar"), resolved.right)
    }

    @Test
    fun theQuickPlayButtonKeepsItsPlaceAfterBeingDragged() {
        val moved = default.place("macro_play", false, 0)
        assertEquals("macro_play", BarArrangement.resolve(moved.encode(), languages).left.first())
    }
}
