package com.spacekeyboard.keyboard.translate

import java.util.Locale

enum class TranslateStyle(val id: String) {
    NEUTRAL("neutral"),
    FORMAL("formal"),
    CASUAL("casual"),
    CHAT("chat"),
    POLITE("polite"),
    CONCISE("concise");

    companion object {
        fun byId(id: String?): TranslateStyle = entries.firstOrNull { it.id == id } ?: NEUTRAL
    }
}

enum class TranslateRoute(val id: String) {
    DEVICE("device"),
    AI("ai");

    companion object {
        fun byId(id: String?): TranslateRoute = entries.firstOrNull { it.id == id } ?: DEVICE
    }
}

enum class TranslateSource { SELECTION, ALL, NONE, READONLY }

enum class TranslateFailure {
    FIELD_BLOCKED,
    NOT_REQUESTED,
    EMPTY,
    TOO_LONG,
    NO_CONSENT,
    NETWORK,
    QUOTA,
    REJECTED,
    SERVER,
    UNSUPPORTED,
    MODEL,
}

sealed interface TranslateOutcome {
    data class Done(val text: String, val remaining: Int?, val route: TranslateRoute) : TranslateOutcome
    data class Failed(val failure: TranslateFailure) : TranslateOutcome
}

data class TranslateRequest(val text: String, val source: String, val target: String, val style: TranslateStyle)

object TranslateLabels {
    fun preview(text: String, max: Int): String {
        val flat = text.replace(Regex("\\s+"), " ").trim()
        return if (flat.length > max) flat.take(max) + "…" else flat
    }
}

object TranslatePolicy {
    const val MAX_TEXT = 2000
    const val CONSENT_VERSION = 1
    const val AUTO = "auto"
}

object TranslateLanguages {
    val codes: List<String> = listOf(
        "af", "ar", "be", "bg", "bn", "ca", "cs", "cy", "da", "de", "el", "en", "eo", "es", "et", "fa", "fi", "fr",
        "ga", "gl", "gu", "he", "hi", "hr", "ht", "hu", "id", "is", "it", "ja", "ka", "kn", "ko", "lt", "lv", "mk",
        "mr", "ms", "mt", "nl", "no", "pl", "pt", "ro", "ru", "sk", "sl", "sq", "sv", "sw", "ta", "te", "th", "tl",
        "tr", "uk", "ur", "vi", "zh",
    )

    val strong: Set<String> = setOf(
        "ar", "de", "en", "es", "fr", "hi", "id", "it", "ja", "ko", "nl", "pl", "pt", "ru", "tr", "uk", "vi", "zh",
    )

    val ordered: List<String> = codes.filter { it in strong } + codes.filter { it !in strong }

    fun isSupported(code: String): Boolean = code in codes

    fun isStrong(code: String): Boolean = code in strong

    fun displayName(code: String, locale: Locale): String =
        Locale.forLanguageTag(code).getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }

    fun defaultTarget(locale: Locale): String = if (locale.language == "en") "ar" else "en"
}
