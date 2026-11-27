package com.spacekeyboard.keyboard.core.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutFilesTest {
    private fun load(id: String): LayoutSpec =
        LayoutParser.parse(File("src/main/res/raw/layout_$id.json").readText(Charsets.UTF_8))

    private fun letters(layout: LayoutSpec) =
        layout.rows.flatten().filter { it.action == KeyAction.COMMIT }.map { it.label }

    @Test
    fun arabicHasEveryLetter() {
        val wanted = "ابتثجحخدذرزسشصضطظعغفقكلمنهويءئؤةى".map { it.toString() }
        val reachable = load("ar").rows.flatten().flatMap { it.letters + it.alts }
        assertTrue(reachable.containsAll(wanted))
        assertTrue(letters(load("ar")).containsAll("ابتثجحخدذرزسشصضطظعغفقكلمنهوي".map { it.toString() }))
    }

    @Test
    fun englishHasTheTwentySixLetters() {
        val wanted = ('a'..'z').map { it.toString() }
        assertTrue(letters(load("en")).containsAll(wanted))
    }

    @Test
    fun everyLayoutHasTheEssentialKeysOnce() {
        listOf("ar", "en").forEach { id ->
            val keys = load(id).rows.flatten()
            val switch = KeyAction.OPEN_SYMBOLS
            listOf(KeyAction.DELETE, KeyAction.SPACE, KeyAction.ENTER, switch).forEach { action ->
                assertEquals("$id $action", 1, keys.count { it.action == action })
            }
            assertEquals(keys.size, keys.map { it.id }.toSet().size)
        }
    }

    @Test
    fun rowsInOneLayoutHaveEqualTotalWeight() {
        listOf("ar", "en").forEach { id ->
            val totals = load(id).rows.map { row -> row.sumOf { it.weight.toDouble() } }
            totals.forEach { assertEquals(id, totals.first(), it, 0.001) }
        }
    }

    @Test
    fun spaceAndSpacersAreFixed() {
        val keys = load("en").rows.flatten()
        assertTrue(keys.filter { it.action == KeyAction.SPACE || it.action == KeyAction.SPACER }.none { it.movable })
        assertTrue(keys.first { it.label == "q" }.movable)
    }

    @Test
    fun derivedIdsAreStable() {
        assertEquals("en_71", load("en").rows[0][0].id)
        assertEquals("ar_636", load("ar").rows[0][0].id)
    }

    @Test
    fun parserRejectsDuplicatesAndEmptyRows() {
        assertThrows(IllegalArgumentException::class.java) {
            LayoutParser.parse("""{"id":"x","rows":[["a","a"]]}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            LayoutParser.parse("""{"id":"x","rows":[["a"],[]]}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            LayoutParser.parse("""{"id":"x","rows":[[{"action":"DELETE"}]]}""")
        }
    }

    @Test
    fun letterLayoutsOfferShiftOnlyInEnglishAndNumbersEverywhere() {
        assertEquals(1, load("en").rows.flatten().count { it.action == KeyAction.SHIFT })
        assertEquals(0, load("ar").rows.flatten().count { it.action == KeyAction.SHIFT })
        listOf("ar", "en", "num", "sym").forEach { id ->
            assertEquals(id, 1, load(id).rows.flatten().count { it.action == KeyAction.NUMBERS })
        }
    }

    @Test
    fun numbersLayoutHasDigitsAndEssentialKeys() {
        val keys = load("num").rows.flatten()
        assertTrue(keys.map { it.label }.containsAll("0123456789.,".map { it.toString() }))
        listOf(KeyAction.DELETE, KeyAction.SPACE, KeyAction.ENTER).forEach { action ->
            assertEquals("$action", 1, keys.count { it.action == action })
        }
        assertEquals(3, keys.count { it.action == KeyAction.RAIL })
        assertEquals(load("en").rows.size, load("num").rows.size)
        val totals = load("num").rows.map { row -> row.sumOf { it.weight.toDouble() } }
        totals.forEach { assertEquals(totals.first(), it, 0.001) }
    }

    @Test
    fun symbolsPageIsReachedFromNumbersAndReturnsBack() {
        assertEquals(1, load("num").rows.flatten().count { it.action == KeyAction.SYMBOLS })
        assertEquals(1, load("sym").rows.flatten().count { it.action == KeyAction.SYMBOLS })
        val keys = load("sym").rows.flatten()
        assertTrue(keys.map { it.label }.containsAll("@#$%&-+()/=*:;!?^√~[]{}<>".map { it.toString() }))
        listOf(KeyAction.DELETE, KeyAction.SPACE, KeyAction.ENTER).forEach { action ->
            assertEquals("$action", 1, keys.count { it.action == action })
        }
        val totals = load("sym").rows.map { row -> row.sumOf { it.weight.toDouble() } }
        totals.forEach { assertEquals(totals.first(), it, 0.001) }
    }

    @Test
    fun topRowsReachDigitsByHolding() {
        assertEquals(listOf("1"), load("en").rows[0][0].alts)
        assertEquals(listOf("1", "١"), load("ar").rows[0][0].alts)
        val english = load("en").rows[0].flatMap { it.alts }
        assertTrue(english.containsAll("1234567890".map { it.toString() }))
    }

    @Test
    fun holdListsAreParsedAndDefaultToEmpty() {
        val spec = LayoutParser.parse("""{"id":"x","rows":[[{"label":"a","hold":["b","c"]},"d"]]}""")
        assertEquals(listOf("b", "c"), spec.rows[0][0].alts)
        assertTrue(spec.rows[0][1].alts.isEmpty())
    }
}
