package com.spacekeyboard.keyboard.autofix

enum class Kind { WORD, LETTER, TOUCH }

data class Learned(
    val kind: Kind,
    val from: String,
    val to: String,
    val zone: Int,
    val strength: Int,
    val active: Boolean,
)

object LearnedView {
    fun list(model: ErrorModel, level: AutoFixLevel): List<Learned> {
        val words = model.words.entries().map { (k, v, r) -> Learned(Kind.WORD, k, v, Zone.NONE, r.score, level.accepts(r)) }
        val letters = model.contexts.entries().map { (k, v, r) ->
            Learned(Kind.LETTER, k.prefix + k.pressed, k.prefix + v, Zone.NONE, r.score, level.accepts(r))
        }
        val touches = model.zones.entries().map { (k, v, r) ->
            Learned(Kind.TOUCH, k.key.toString(), v.toString(), k.zone, r.score, false)
        }
        return (words + letters + touches).filter { it.strength > 0 }.sortedByDescending { it.strength }
    }

    fun forget(model: ErrorModel, item: Learned) {
        when (item.kind) {
            Kind.WORD -> model.words.remove(item.from, item.to)
            Kind.LETTER -> model.contexts.remove(Spot(item.from.dropLast(1), item.from.last()), item.to.last())
            Kind.TOUCH -> model.zones.remove(Cell(item.from[0], item.zone), item.to[0])
        }
        model.touch()
    }
}
