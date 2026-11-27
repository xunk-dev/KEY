package com.spacekeyboard.keyboard.clipboard

enum class ClipKind { LINK, EMAIL, PHONE, USERNAME, TOKEN, NUMBER, TEXT }

object ClipClassifier {
    private const val GENERIC_TOKEN_MIN = 20
    private const val GENERIC_TOKEN_SWITCHES = 5
    private const val NUMBER_MAX = 40
    private const val PHONE_MIN_DIGITS = 8
    private const val PHONE_MAX_DIGITS = 15
    private const val CACHE_SIZE = 256

    private val LINKS = listOf(
        Regex("""\b[a-z][a-z0-9+.-]{1,15}://\S+""", RegexOption.IGNORE_CASE),
        Regex("""\bwww\.[a-z0-9-]{1,63}(?:\.[a-z0-9-]{1,63}){1,10}\S*""", RegexOption.IGNORE_CASE),
        Regex(
            """(?<![@\w.-])(?:[a-z0-9][a-z0-9-]{0,61}\.){1,10}(?:com|net|org|io|me|app|dev|gg|tv|ly|xyz|info|ai|gov|edu|site|online|store|link|page|co|uk|de|fr|ru|tr)(?![\w@-])(?:[/?#]\S*)?""",
            RegexOption.IGNORE_CASE,
        ),
    )

    private val EMAIL = Regex(
        """(?<![\w.%+-])[a-z0-9._%+-]{1,64}@[a-z0-9-]{1,63}(?:\.[a-z0-9-]{1,63}){0,10}\.[a-z]{2,24}""",
        RegexOption.IGNORE_CASE,
    )

    private val USERNAME = Regex("""(?<![\w@.])@[a-z0-9_]{3,32}(?![\w@])""", RegexOption.IGNORE_CASE)

    private val STRONG_TOKENS = listOf(
        Regex("""\beyJ[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}"""),
        Regex("""\b(?:sk|pk|rk)-[A-Za-z0-9_-]{20,}"""),
        Regex("""\b[a-z]{2,6}_[A-Za-z0-9]{24,}"""),
        Regex("""\bgithub_pat_[A-Za-z0-9_]{40,}"""),
        Regex("""\bglpat-[A-Za-z0-9_-]{20,}"""),
        Regex("""\bxox[abprs]-[A-Za-z0-9-]{10,}"""),
        Regex("""\b(?:AKIA|ASIA)[A-Z0-9]{16}\b"""),
        Regex("""\bAIza[0-9A-Za-z_-]{35}"""),
        Regex("""\bya29\.[0-9A-Za-z_-]{20,}"""),
        Regex("""(?<!\d)\d{8,10}:[A-Za-z0-9_-]{35}\b"""),
        Regex("""\bBearer\s+[A-Za-z0-9._~+/=-]{20,}""", RegexOption.IGNORE_CASE),
        Regex("""\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\b""", RegexOption.IGNORE_CASE),
        Regex("""\b[0-9a-fA-F]{32,}\b"""),
        Regex(
            """\b(?:api[_-]?key|access[_-]?key|secret|token|password|passwd|pwd)\s*[:=]\s*\S{6,}""",
            RegexOption.IGNORE_CASE,
        ),
    )

    private val GENERIC_TOKEN = Regex("""[A-Za-z0-9_+/=.-]+""")

    private val PHONE = Regex("""(?<![\w.])\+?\d(?:[ ().-]{0,2}\d){0,24}""")

    private val NOT_PHONE = listOf(
        Regex("""\b\d{1,3}(?:\.\d{1,3}){3}\b"""),
        Regex("""\b\d{4}[-/.]\d{1,2}[-/.]\d{1,2}\b"""),
        Regex("""\b\d{1,2}[-/.]\d{1,2}[-/.]\d{2,4}\b"""),
        Regex("""\b\d{1,2}:\d{2}(?::\d{2})?\b"""),
    )

    private val NUMERIC = Regex("""[\p{Sc}+\-]?\d[\d\s.,:/%-]*\p{Sc}?""")

    private val cache = object : LinkedHashMap<String, List<ClipKind>>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<ClipKind>>?): Boolean = size > CACHE_SIZE
    }

    fun kinds(text: String): List<ClipKind> = synchronized(cache) { cache.getOrPut(text) { detect(text) } }

    fun primary(text: String): ClipKind = kinds(text).first()

    private fun detect(text: String): List<ClipKind> {
        val flat = ascii(text).trim()
        if (flat.isEmpty()) return listOf(ClipKind.TEXT)
        val found = HashSet<ClipKind>()
        if (STRONG_TOKENS.any { it.containsMatchIn(flat) }) found += ClipKind.TOKEN
        var rest = flat
        LINKS.forEach { rest = strip(rest, it, ClipKind.LINK, found) }
        rest = strip(rest, EMAIL, ClipKind.EMAIL, found)
        rest = strip(rest, USERNAME, ClipKind.USERNAME, found)
        STRONG_TOKENS.forEach { rest = it.replace(rest, " ") }
        NOT_PHONE.forEach { rest = it.replace(rest, " ") }
        collectPhones(rest, found)
        if (found.isEmpty() && isGenericToken(flat)) found += ClipKind.TOKEN
        if (found.isEmpty() && isNumeric(flat)) found += ClipKind.NUMBER
        if (found.isEmpty()) found += ClipKind.TEXT
        return ClipKind.entries.filter { it in found }
    }

    private fun strip(text: String, pattern: Regex, kind: ClipKind, found: MutableSet<ClipKind>): String {
        if (!pattern.containsMatchIn(text)) return text
        found += kind
        return pattern.replace(text, " ")
    }

    private fun collectPhones(text: String, found: MutableSet<ClipKind>) {
        if (PHONE.findAll(text).any { isPhone(it.value) }) found += ClipKind.PHONE
    }

    private fun isPhone(raw: String): Boolean {
        val digits = raw.count { it.isDigit() }
        if (digits < PHONE_MIN_DIGITS || digits > PHONE_MAX_DIGITS) return false
        if (raw.startsWith("+") || raw.startsWith("0")) return true
        return raw.any { it == ' ' || it == '-' || it == '(' || it == ')' || it == '.' }
    }

    private fun isGenericToken(text: String): Boolean {
        if (text.length < GENERIC_TOKEN_MIN || !GENERIC_TOKEN.matches(text)) return false
        if (text.none { it.isDigit() } || text.none { it in 'a'..'z' || it in 'A'..'Z' }) return false
        return classSwitches(text) >= GENERIC_TOKEN_SWITCHES
    }

    private fun classSwitches(text: String): Int {
        var switches = 0
        var last = 0
        text.forEach {
            val now = when {
                it in 'a'..'z' -> 1
                it in 'A'..'Z' -> 2
                it in '0'..'9' -> 3
                else -> 0
            }
            if (now != 0) {
                if (last != 0 && now != last) switches++
                last = now
            }
        }
        return switches
    }

    private fun isNumeric(text: String): Boolean = text.length <= NUMBER_MAX && NUMERIC.matches(text)

    private fun ascii(text: String): String = buildString(text.length) {
        text.forEach {
            append(
                when (it) {
                    in '\u0660'..'\u0669' -> '0' + (it - '\u0660')
                    in '\u06F0'..'\u06F9' -> '0' + (it - '\u06F0')
                    else -> it
                },
            )
        }
    }
}
