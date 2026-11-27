package com.spacekeyboard.keyboard.core.otp

data class OtpMatch(val code: String, val bare: Boolean, val ttlMs: Long)

object OtpExtractor {
    const val DEFAULT_TTL_MS = 300_000L
    const val MIN_TTL_MS = 60_000L
    const val MAX_TTL_MS = 600_000L
    private const val MAX_TEXT = 2000
    private const val NEAR = 90
    private const val GROUP_NEAR = 40
    private const val ALNUM_NEAR = 30
    private const val YEAR_NEAR = 15
    private const val LOOK_BEFORE = 28
    private const val LOOK_AFTER = 12
    private const val MIN_LEN = 4
    private const val MAX_LEN = 8

    private const val EDGE_BEFORE = "(?<![\\p{N}_A-Za-z.,:/+#\\-])(?<!\\d[ \\-])"
    private const val EDGE_AFTER = "(?![\\p{N}_A-Za-z%]|[.,:/]\\d|-\\d)"

    private val PLAIN = Regex("(?:$EDGE_BEFORE|(?<=[A-Za-z]-))(\\d{4,8})$EDGE_AFTER")
    private val GROUPED = Regex("$EDGE_BEFORE(\\d{2,4})([ \\-])(\\d{2,4})(?:\\2(\\d{2,4}))?(?![\\p{N}_A-Za-z%]|[.,:/]\\d|[ \\-]\\d)")
    private val SPACED = Regex("$EDGE_BEFORE(\\d(?: \\d){3,7})(?![\\p{N}_A-Za-z%]|[.,:/]\\d|[ \\-]\\d)")
    private val ALNUM = Regex("(?<![\\p{L}\\p{N}_\\-])(?=[A-Za-z0-9]*\\d)(?=[A-Za-z0-9]*[A-Za-z])([A-Za-z0-9]{5,8})(?![\\p{L}\\p{N}_\\-])")
    private val BARE = Regex("^\\s*[^\\p{L}\\p{N}]{0,3}(\\d{4,8}|\\d{2,4}[ \\-]\\d{2,4}(?:[ \\-]\\d{2,4})?)[^\\p{L}\\p{N}]{0,3}\\s*$")

    private enum class Kind { PLAIN, GROUPED, SPACED, ALNUM }

    private class Candidate(val code: String, val range: IntRange, val kind: Kind)

    private class Scored(val code: String, val score: Int, val bonus: Int)

    fun extract(raw: String): OtpMatch? {
        if (raw.isBlank() || raw.length > MAX_TEXT) return null
        val text = OtpText.clean(raw)
        bare(text)?.let { return OtpMatch(it, true, DEFAULT_TTL_MS) }
        val keywords = OtpVocabulary.KEYWORD.findAll(text).map { it.range }.toList()
        if (keywords.isEmpty()) return null
        val best = candidates(text)
            .mapNotNull { score(text, it, keywords) }
            .maxWithOrNull(compareBy<Scored>({ it.score }, { it.bonus })) ?: return null
        val ttl = OtpText.validity(text, DEFAULT_TTL_MS, MIN_TTL_MS, MAX_TTL_MS)
        return OtpMatch(best.code, false, ttl)
    }

    private fun bare(text: String): String? {
        val hit = BARE.find(text) ?: return null
        val code = hit.groupValues[1].filter(Char::isDigit)
        val year = code.length == 4 && code.toInt() in 1900..2100
        return code.takeIf { it.length in MIN_LEN..MAX_LEN && !year }
    }

    private fun candidates(text: String): List<Candidate> {
        val found = ArrayList<Candidate>()
        PLAIN.findAll(text).forEach { found += Candidate(it.groupValues[1], it.groups[1]!!.range, Kind.PLAIN) }
        GROUPED.findAll(text).forEach { hit ->
            val digits = listOf(1, 3, 4).joinToString("") { hit.groupValues[it] }
            if (digits.length in MIN_LEN..MAX_LEN) found += Candidate(digits, hit.range, Kind.GROUPED)
        }
        SPACED.findAll(text).forEach { found += Candidate(it.groupValues[1].replace(" ", ""), it.groups[1]!!.range, Kind.SPACED) }
        ALNUM.findAll(text).forEach { found += Candidate(it.groupValues[1], it.groups[1]!!.range, Kind.ALNUM) }
        return found
    }

    private fun score(text: String, item: Candidate, keywords: List<IntRange>): Scored? {
        val gap = keywords.minOf { gap(it, item.range) }
        val limit = when (item.kind) {
            Kind.PLAIN -> NEAR
            Kind.GROUPED, Kind.SPACED -> GROUP_NEAR
            Kind.ALNUM -> ALNUM_NEAR
        }
        if (gap > limit || blocked(text, item)) return null
        if (item.kind == Kind.PLAIN && yearLike(item.code) && gap > YEAR_NEAR) return null
        val bonus = if (item.code.length == 6) 3 else 0
        return Scored(item.code, NEAR - gap, bonus)
    }

    private fun gap(keyword: IntRange, item: IntRange): Int = when {
        keyword.last < item.first -> item.first - keyword.last - 1
        item.last < keyword.first -> keyword.first - item.last - 1
        else -> 0
    }

    private fun blocked(text: String, item: Candidate): Boolean {
        val start = item.range.first
        val end = item.range.last + 1
        val before = text.substring(maxOf(0, start - LOOK_BEFORE), start)
        val after = text.substring(end, minOf(text.length, end + LOOK_AFTER))
        return OtpVocabulary.NEGATIVE_BEFORE.containsMatchIn(before) || OtpVocabulary.NEGATIVE_AFTER.containsMatchIn(after)
    }

    private fun yearLike(code: String): Boolean = code.length == 4 && code.toInt() in 1900..2100
}
