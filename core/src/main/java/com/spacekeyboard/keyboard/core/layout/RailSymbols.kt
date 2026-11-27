package com.spacekeyboard.keyboard.core.layout

object RailSymbols {
    class Item(val label: String, val alts: List<String> = emptyList())

    val items: List<Item> = listOf(
        Item("+", listOf("\u00B1")),
        Item("-", listOf("\u2212", "\u2013")),
        Item("\u00D7"),
        Item("\u00F7"),
        Item("="),
        Item("/"),
        Item("*"),
        Item("_"),
        Item("%"),
        Item("$"),
        Item("#"),
        Item("@"),
        Item("("),
        Item(")"),
        Item(":"),
        Item(",", listOf("\u060C")),
        Item(".", listOf("\u2026")),
    )
}
