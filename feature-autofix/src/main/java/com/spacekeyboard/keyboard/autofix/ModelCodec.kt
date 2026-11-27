package com.spacekeyboard.keyboard.autofix

object ModelCodec {
    private const val HEADER = "v1"
    private const val SEP = "\t"

    fun encode(model: ErrorModel): String {
        val lines = ArrayList<String>()
        lines += HEADER
        model.contexts.entries().forEach { (k, v, r) -> lines += row("c", k.prefix, k.pressed.toString(), v.toString(), *numbers(r)) }
        model.zones.entries().forEach { (k, v, r) -> lines += row("z", k.key.toString(), k.zone.toString(), v.toString(), *numbers(r)) }
        model.presses.forEach { (k, n) -> lines += row("p", k.key.toString(), k.zone.toString(), n.toString()) }
        model.words.entries().forEach { (k, v, r) -> lines += row("w", k, v, *numbers(r)) }
        return lines.joinToString("\n")
    }

    fun decode(text: String?, into: ErrorModel = ErrorModel()): ErrorModel {
        val lines = text?.lineSequence()?.toList().orEmpty()
        if (lines.firstOrNull() != HEADER) return into
        lines.drop(1).forEach { line -> runCatching { read(line.split(SEP), into) } }
        return into
    }

    private fun read(parts: List<String>, model: ErrorModel) {
        when (parts[0]) {
            "c" -> model.contexts.update(Spot(parts[1], single(parts[2])), single(parts[3])) { rule(parts, 4) }
            "z" -> model.zones.update(Cell(single(parts[1]), zone(parts[2])), single(parts[3])) { rule(parts, 4) }
            "p" -> model.presses[Cell(single(parts[1]), zone(parts[2]))] = parts[3].toInt().coerceAtLeast(0)
            "w" -> model.words.update(parts[1], parts[2]) { rule(parts, 3) }
        }
    }

    private fun row(vararg parts: String): String = parts.joinToString(SEP)

    private fun numbers(rule: Rule): Array<String> =
        arrayOf(rule.fixed, rule.applied, rule.undone, rule.kept, rule.day).map { it.toString() }.toTypedArray()

    private fun rule(parts: List<String>, from: Int): Rule {
        val n = (from until from + 5).map { parts[it].toInt().coerceAtLeast(0) }
        return Rule(n[0], n[1], n[2], n[3], n[4])
    }

    private fun single(text: String): Char {
        require(text.length == 1 && !text[0].isWhitespace())
        return text[0]
    }

    private fun zone(text: String): Int = text.toInt().also { require(it in 0 until Zone.COUNT) }
}
