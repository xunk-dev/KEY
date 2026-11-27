package com.spacekeyboard.keyboard.autofix

class Parked(val chars: List<TypedChar>, val episodes: List<Episode>, val boundary: String)

class Veto(val key: Char, val size: Int)

class WordTracker {
    val chars = ArrayList<TypedChar>()
    val episodes = ArrayList<Episode>()
    var opaque = false
    var veto: Veto? = null
    var justApplied: TypedChar? = null
    private var open: Episode? = null

    val size: Int get() = chars.size

    val correcting: Boolean get() = open != null

    fun word(): String = String(CharArray(chars.size) { chars[it].ch })

    fun add(tc: TypedChar) {
        open?.typed?.add(tc)
        chars.add(tc)
    }

    fun pop(): TypedChar? {
        if (chars.isEmpty()) return null
        val episode = open ?: Episode(chars.toList(), chars.size).also { open = it; episodes.add(it) }
        val tc = chars.removeAt(chars.lastIndex)
        if (episode.typed.lastOrNull() === tc) episode.typed.removeAt(episode.typed.lastIndex)
        episode.cut = minOf(episode.cut, chars.size)
        return tc
    }

    fun restore(parked: Parked) {
        clear()
        chars.addAll(parked.chars)
        episodes.addAll(parked.episodes)
    }

    fun park(boundary: String): Parked = Parked(chars.toList(), episodes.toList(), boundary)

    fun clear() {
        chars.clear()
        episodes.clear()
        open = null
        opaque = false
        veto = null
        justApplied = null
    }
}
