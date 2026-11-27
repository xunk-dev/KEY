package com.spacekeyboard.keyboard.autofix

import kotlin.math.abs

private const val CENTER = 1

interface KeyNeighbors {
    fun adjacent(pressed: Char, intended: Char): Boolean

    fun toward(pressed: Char, intended: Char): Int

    fun aligned(pressed: Char, intended: Char, zone: Int): Boolean {
        val face = toward(pressed, intended)
        if (face == Zone.NONE || zone == Zone.NONE) return true
        return axis(face % 3, zone % 3) && axis(face / 3, zone / 3)
    }

    private fun axis(wanted: Int, got: Int): Boolean = wanted == CENTER || got == wanted

    object Unknown : KeyNeighbors {
        override fun adjacent(pressed: Char, intended: Char): Boolean = true

        override fun toward(pressed: Char, intended: Char): Int = Zone.NONE
    }
}

class KeyPoint(val ch: Char, val x: Float, val y: Float, val width: Float, val height: Float)

class KeyMap(points: List<KeyPoint>) : KeyNeighbors {
    private val byChar: Map<Char, List<KeyPoint>> = points
        .filter { it.width > 0f && it.height > 0f }
        .groupBy { it.ch.lowercaseChar() }

    override fun adjacent(pressed: Char, intended: Char): Boolean {
        val from = byChar[pressed.lowercaseChar()]
        val to = byChar[intended.lowercaseChar()]
        if (from == null || to == null) return true
        return from.any { a -> to.any { b -> close(a, b) } }
    }

    override fun toward(pressed: Char, intended: Char): Int {
        val from = byChar[pressed.lowercaseChar()] ?: return Zone.NONE
        val to = byChar[intended.lowercaseChar()] ?: return Zone.NONE
        val pair = from.firstNotNullOfOrNull { a -> to.firstOrNull { b -> close(a, b) }?.let { a to it } } ?: return Zone.NONE
        return Zone.of((pair.second.x - pair.first.x) / pair.first.width, (pair.second.y - pair.first.y) / pair.first.height)
    }

    private fun close(a: KeyPoint, b: KeyPoint): Boolean = reaches(a, b) || reaches(b, a)

    private fun reaches(a: KeyPoint, b: KeyPoint): Boolean =
        abs(b.x - a.x) / a.width <= REACH && abs(b.y - a.y) / a.height <= REACH

    private companion object {
        const val REACH = 1.3f
    }
}
