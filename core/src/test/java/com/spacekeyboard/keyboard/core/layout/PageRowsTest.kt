package com.spacekeyboard.keyboard.core.layout

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageRowsTest {
    private val letterFiles = listOf("ar", "ar_ten", "ar_ios", "ar_pair", "ar_plain", "en", "az", "fa")

    private fun load(file: String): LayoutSpec =
        LayoutParser.parse(File("src/main/res/raw/layout_$file.json").readText(Charsets.UTF_8))

    private fun letters(file: String, tall: Boolean): LayoutSpec {
        val base = load(file)
        return if (tall) base.withNumberRow(NumberRow.digits(base.id, false)) else base
    }

    private fun pages(tall: Boolean): List<LayoutSpec> = listOf(
        PageRows.symbols(load("sym"), tall),
        PageRows.edit(load("edit"), tall),
    )

    private fun share(spec: LayoutSpec, row: Int, action: KeyAction): Double {
        val keys = spec.rows[row]
        return keys.first { it.action == action }.weight / keys.sumOf { it.weight.toDouble() }
    }

    @Test
    fun everyPageHasTheSameNumberOfRows() {
        listOf(true, false).forEach { tall ->
            val wanted = PageRows.count(tall)
            letterFiles.forEach { assertEquals(it, wanted, letters(it, tall).rows.size) }
            pages(tall).forEachIndexed { i, page -> assertEquals("page $i tall=$tall", wanted, page.rows.size) }
        }
    }

    @Test
    fun rowsOfEveryPageKeepEqualTotalWeight() {
        listOf(true, false).forEach { tall ->
            pages(tall).forEach { page ->
                val totals = page.rows.map { row -> row.sumOf { it.weight.toDouble() } }
                totals.forEach { assertEquals(page.id, totals.first(), it, 0.001) }
            }
        }
    }

    @Test
    fun enterIsTheSameWidthOnEveryPageAndLanguage() {
        listOf(true, false).forEach { tall ->
            val all = letterFiles.map { letters(it, tall) } + pages(tall)
            all.forEach { page -> assertEquals(page.id, 0.14, share(page, page.rows.lastIndex, KeyAction.ENTER), 0.002) }
        }
    }

    @Test
    fun backspaceIsTheSameWidthOnTheSameRowOnEveryPageAndLanguage() {
        listOf(true, false).forEach { tall ->
            val all = letterFiles.map { letters(it, tall) } + pages(tall)
            all.forEach { page ->
                val row = page.rows.lastIndex - 1
                assertTrue(page.id, page.rows[row].any { it.action == KeyAction.DELETE })
                assertEquals(page.id, 0.14, share(page, row, KeyAction.DELETE), 0.002)
            }
        }
    }

    @Test
    fun spaceBarStartsAndEndsAtTheSameXOnLettersNumbersAndSymbols() {
        listOf(true, false).forEach { tall ->
            val pageSet = listOf(letters("en", tall)) + pages(tall).take(1)
            val edges = pageSet.map { page ->
                val row = page.rows.last()
                val total = row.sumOf { it.weight.toDouble() }
                val before = row.takeWhile { it.action != KeyAction.SPACE }.sumOf { it.weight.toDouble() }
                val space = row.first { it.action == KeyAction.SPACE }.weight
                (before + space) / total
            }
            edges.forEach { assertEquals(edges.first(), it, 0.002) }
        }
    }

    @Test
    fun numberRowOffKeepsSymbolsWithoutTheDigitRow() {
        val sym = PageRows.symbols(load("sym"), false)
        assertEquals(PageRows.SHORT, sym.rows.size)
        assertTrue(sym.rows.flatten().none { it.label == "1" })
    }

    @Test
    fun numberPadKeepsItsOwnRowsWithOrWithoutTheNumberRow() {
        listOf(true, false).forEach { tall ->
            val pad = PageRows.numbers(load("num"), tall)
            assertEquals(PageRows.SHORT, pad.rows.size)
            val totals = pad.rows.map { row -> row.sumOf { it.weight.toDouble() } }
            totals.forEach { assertEquals(totals.first(), it, 0.001) }
            assertEquals(pad.rows.flatten().size, pad.rows.flatten().map { it.id }.toSet().size)
            assertTrue(pad.rows.flatten().any { it.action == KeyAction.SYMBOLS })
            assertTrue(pad.rows.flatten().any { it.action == KeyAction.NUMBERS })
        }
    }

    @Test
    fun padDigitsFollowTheNativeDigitChoice() {
        val pad = PageRows.numbers(load("num"), false)
        val one = PageLabels.page(pad, "ar", true).rows.flatten().first { it.id == "num_31" }
        assertEquals("١", one.label)
        assertEquals(listOf("1"), one.alts)
        val latin = PageLabels.page(pad, "ar", false).rows.flatten().first { it.id == "num_31" }
        assertEquals("1", latin.label)
        assertEquals(listOf("١"), latin.alts)
    }

    @Test
    fun tallPagesKeepEveryOriginalKey() {
        val edit = PageRows.edit(load("edit"), true)
        assertEquals(load("edit").rows.flatten().map { it.id }.toSet(), edit.rows.flatten().map { it.id }.toSet())
    }
}
