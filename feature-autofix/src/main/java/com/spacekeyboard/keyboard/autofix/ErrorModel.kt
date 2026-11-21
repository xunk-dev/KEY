package com.spacekeyboard.keyboard.autofix

data class Spot(val prefix: String, val pressed: Char)

data class Cell(val key: Char, val zone: Int)

class ErrorModel {
    val contexts = RuleTable<Spot, Char>()
    val zones = RuleTable<Cell, Char>()
    val words = RuleTable<String, String>()
    val presses = HashMap<Cell, Int>()
    var revision = 0
        private set

    fun touch() {
        revision++
    }

    fun isEmpty(): Boolean = contexts.size + zones.size + words.size == 0

    fun press(key: Char, zone: Int) {
        val cell = Cell(key, zone)
        val count = (presses[cell] ?: 0) + 1
        touch()
        if (count < HALVE_AT) {
            presses[cell] = count
            return
        }
        presses[cell] = count / 2
        zones.updateAll(cell, Rule::halved)
    }

    fun learnSlip(prefix: String, pressed: Char, intended: Char, zone: Int, today: Int) {
        if (prefix.length >= MIN_CONTEXT) contexts.update(Spot(prefix, pressed), intended) { it.withFixed(today) }
        if (zone != Zone.NONE) zones.update(Cell(pressed, zone), intended) { it.withFixed(today) }
        touch()
    }

    fun learnWord(wrong: String, right: String, today: Int) {
        words.update(wrong, right) { it.withFixed(today) }
        touch()
    }

    fun contextFix(prefix: String, pressed: Char, level: AutoFixLevel): Char? =
        if (prefix.length < MIN_CONTEXT) null else contexts.best(Spot(prefix, pressed)) { level.accepts(it) }

    fun zoneFix(key: Char, zone: Int, level: AutoFixLevel): Char? {
        val cell = Cell(key, zone)
        val seen = presses[cell] ?: 0
        if (zone == Zone.NONE || seen < MIN_PRESSES) return null
        return zones.best(cell) { level.accepts(it, maxOf(seen, it.total), ZONE_EXTRA) }
    }

    fun wordFix(word: String, level: AutoFixLevel): String? =
        if (word.length < MIN_WORD) null else words.best(word) { level.accepts(it) }

    fun trim(today: Int) {
        contexts.trim(MAX_CONTEXTS, today)
        zones.trim(MAX_ZONES, today)
        words.trim(MAX_WORDS, today)
    }

    fun clear() {
        contexts.clear()
        zones.clear()
        words.clear()
        presses.clear()
        touch()
    }

    companion object {
        const val MIN_CONTEXT = 3
        const val MIN_WORD = 3
        const val MIN_PRESSES = 12
        const val ZONE_EXTRA = 3
        const val HALVE_AT = 600
        const val MAX_CONTEXTS = 600
        const val MAX_ZONES = 300
        const val MAX_WORDS = 400
    }
}
