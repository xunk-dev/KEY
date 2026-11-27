package com.spacekeyboard.keyboard.autofix

enum class AutoFixLevel(val id: String, private val minScore: Int, private val ratio: Float) {
    GENTLE("gentle", 4, 0.85f),
    BALANCED("balanced", 3, 0.75f),
    BOLD("bold", 2, 0.6f);

    fun accepts(rule: Rule, total: Int = rule.total, extra: Int = 0): Boolean =
        rule.score >= minScore + extra && rule.fixed + rule.applied >= ratio * total

    companion object {
        fun byId(id: String?): AutoFixLevel = entries.firstOrNull { it.id == id } ?: BALANCED
    }
}
