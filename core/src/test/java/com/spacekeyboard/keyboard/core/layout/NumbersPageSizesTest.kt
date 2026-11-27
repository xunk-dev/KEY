package com.spacekeyboard.keyboard.core.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class NumbersPageSizesTest {
    private fun load(file: String): LayoutSpec =
        LayoutParser.parse(File("src/main/res/raw/layout_$file.json").readText(Charsets.UTF_8))

    private fun share(spec: LayoutSpec, action: KeyAction): Double {
        val row = spec.rows.first { r -> r.any { it.action == action } }
        return row.first { it.action == action }.weight / row.sumOf { it.weight.toDouble() }
    }

    @Test
    fun numbersPageEnterAndDeleteMatchTheLetterPages() {
        val num = load("num")
        assertEquals(0.14, share(num, KeyAction.ENTER), 0.002)
        assertEquals(0.14, share(num, KeyAction.DELETE), 0.002)
    }

    @Test
    fun numbersPageRowsKeepEqualTotalWeight() {
        val totals = load("num").rows.map { row -> row.sumOf { it.weight.toDouble() } }
        totals.forEach { assertEquals(totals.first(), it, 0.001) }
    }

    @Test
    fun everyLetterLayoutOpensSymbolsAndNumbersFromTheSameTwoKeys() {
        listOf("ar", "ar_ten", "ar_ios", "ar_pair", "ar_plain", "en", "az", "fa").forEach { id ->
            val last = load(id).rows.last()
            assertEquals(id, KeyAction.OPEN_SYMBOLS, last[0].action)
            assertEquals(id, KeyAction.NUMBERS, last[1].action)
        }
    }
}
