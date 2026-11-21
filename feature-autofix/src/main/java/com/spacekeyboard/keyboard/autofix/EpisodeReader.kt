package com.spacekeyboard.keyboard.autofix

object EpisodeReader {
    fun read(episode: Episode, final: List<TypedChar>): Slip? {
        val removed = episode.removed
        if (removed.isEmpty() || removed.any { !it.raw } || !intact(episode, final)) return null
        val sc = removed.map { it.ch }
        val sizes = listOf(sc.size + 1, sc.size, sc.size - 1).filter { it >= 1 }
        return sizes.firstNotNullOfOrNull { n -> survivors(episode, final, n)?.let { classify(episode, removed, sc, it) } }
    }

    private fun intact(episode: Episode, final: List<TypedChar>): Boolean =
        final.size >= episode.cut && String(CharArray(episode.cut) { final[it].ch }) == episode.prefix

    private fun survivors(episode: Episode, final: List<TypedChar>, count: Int): List<Char>? {
        val typed = episode.typed
        if (typed.size < count || final.size < episode.cut + count) return null
        for (i in 0 until count) if (final[episode.cut + i] !== typed[i]) return null
        return typed.take(count).map { it.ch }
    }

    private fun classify(episode: Episode, removed: List<TypedChar>, sc: List<Char>, tc: List<Char>): Slip? {
        val pos = episode.cut
        return when {
            tc.size == sc.size && tc[0] != sc[0] && sc.drop(1) == tc.drop(1) -> Swap(pos, episode, removed[0], tc[0])
            tc.size == sc.size && twisted(sc, tc) -> Twist(pos, episode)
            tc.size == sc.size + 1 && tc.drop(1) == sc -> Missing(pos, episode, tc[0])
            tc.size + 1 == sc.size && sc.drop(1) == tc -> Extra(pos, episode, sc[0])
            else -> null
        }
    }

    private fun twisted(sc: List<Char>, tc: List<Char>): Boolean =
        sc.size >= 2 && sc[0] != sc[1] && sc[0] == tc[1] && sc[1] == tc[0] && sc.drop(2) == tc.drop(2)
}
