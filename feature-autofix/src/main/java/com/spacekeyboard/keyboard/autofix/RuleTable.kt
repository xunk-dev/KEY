package com.spacekeyboard.keyboard.autofix

class RuleTable<K, V> {
    private val rows = LinkedHashMap<K, LinkedHashMap<V, Rule>>()

    val size: Int get() = rows.values.sumOf { it.size }

    fun rule(key: K, value: V): Rule? = rows[key]?.get(value)

    fun candidates(key: K): Map<V, Rule> = rows[key] ?: emptyMap()

    fun update(key: K, value: V, change: (Rule) -> Rule) {
        val row = rows.getOrPut(key) { LinkedHashMap() }
        row[value] = change(row[value] ?: Rule())
    }

    fun updateAll(key: K, change: (Rule) -> Rule) {
        val row = rows[key] ?: return
        for (entry in row.entries) entry.setValue(change(entry.value))
    }

    fun remove(key: K, value: V) {
        val row = rows[key] ?: return
        row.remove(value)
        if (row.isEmpty()) rows.remove(key)
    }

    fun best(key: K, accept: (Rule) -> Boolean): V? =
        candidates(key).filter { accept(it.value) }.maxByOrNull { it.value.score }?.key

    fun entries(): List<Triple<K, V, Rule>> =
        rows.flatMap { (key, row) -> row.map { Triple(key, it.key, it.value) } }

    fun clear() = rows.clear()

    fun trim(limit: Int, today: Int) {
        entries().filter { stale(it.third, today) }.forEach { remove(it.first, it.second) }
        val extra = size - limit
        if (extra <= 0) return
        entries().sortedBy { it.third.score * RECENCY + it.third.day }.take(extra).forEach { remove(it.first, it.second) }
    }

    private fun stale(rule: Rule, today: Int): Boolean =
        (rule.score < KEEP_SCORE && today - rule.day > MAX_AGE_DAYS) || (rule.score <= 0 && rule.total >= HOPELESS)

    private companion object {
        const val RECENCY = 1000
        const val KEEP_SCORE = 3
        const val MAX_AGE_DAYS = 120
        const val HOPELESS = 6
    }
}
