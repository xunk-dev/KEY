package com.spacekeyboard.keyboard.core.suggest

class DialectBook(
    words: Map<Dialect, List<String>> = emptyMap(),
    pairs: Map<Dialect, Map<String, List<String>>> = emptyMap(),
) {
    class Entry(val word: String, val folded: String, val position: Int, val marker: Boolean)

    private val lists: Map<Dialect, List<Entry>> = words.mapValues { entriesOf(it.value) }
    private val markers: Map<String, Dialect> = markersOf(lists)
    private val follows: Map<Dialect, Map<String, List<String>>> =
        pairs.mapValues { (_, map) -> map.mapKeys { ArabicFold.fold(it.key) } }

    val isEmpty: Boolean
        get() = lists.values.all { it.isEmpty() }

    fun entries(dialect: Dialect): List<Entry> = lists[dialect].orEmpty()

    fun words(): Set<String> = lists.values.flatMapTo(HashSet<String>()) { list -> list.map { it.word } }

    fun markerOf(folded: String): Dialect? = markers[folded]

    fun next(dialect: Dialect, previous: String): List<String> =
        follows[dialect]?.get(ArabicFold.fold(previous)).orEmpty()

    companion object {
        const val BOOST_ONLY = "=="
        val EMPTY = DialectBook()

        private fun entriesOf(list: List<String>): List<Entry> {
            val seen = HashSet<String>()
            val result = ArrayList<Entry>()
            var marker = true
            list.forEach { raw ->
                val word = raw.trim().lowercase()
                val folded = ArabicFold.fold(word)
                if (word == BOOST_ONLY) marker = false
                else if (word.isNotEmpty() && seen.add(folded)) result += Entry(word, folded, result.size, marker)
            }
            return result
        }

        private fun markersOf(lists: Map<Dialect, List<Entry>>): Map<String, Dialect> {
            val owners = HashMap<String, MutableSet<Dialect>>()
            lists.forEach { (dialect, entries) ->
                entries.forEach { owners.getOrPut(it.folded) { HashSet() }.add(dialect) }
            }
            val result = HashMap<String, Dialect>()
            lists.forEach { (dialect, entries) ->
                entries.filter { it.marker && owners.getValue(it.folded).size == 1 }.forEach { result[it.folded] = dialect }
            }
            return result
        }
    }
}
