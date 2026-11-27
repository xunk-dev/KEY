package com.spacekeyboard.keyboard.core.otp

import android.text.InputType
import android.view.inputmethod.EditorInfo

enum class OtpFit { NONE, MAYBE, LIKELY, CERTAIN }

class FieldTraits(val inputType: Int, val imeOptions: Int, val labels: List<String?>)

object OtpFieldRules {
    private val DENIED_TEXT = setOf(
        InputType.TYPE_TEXT_VARIATION_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT,
        InputType.TYPE_TEXT_VARIATION_URI,
        InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE,
        InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE,
        InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_FILTER,
        InputType.TYPE_TEXT_VARIATION_PHONETIC,
    )

    fun fit(traits: FieldTraits): OtpFit {
        val words = traits.labels.filterNotNull().joinToString(" ").lowercase()
        val named = byName(words)
        if (named != null) return named
        return byType(traits.inputType, traits.imeOptions)
    }

    private fun byName(words: String): OtpFit? {
        if (words.isBlank()) return null
        val verifies = OtpVocabulary.FIELD_VERIFY.containsMatchIn(words)
        val codeLike = OtpVocabulary.FIELD_OTP.containsMatchIn(words)
        return when {
            OtpVocabulary.FIELD_DENY.containsMatchIn(words) && !verifies -> OtpFit.NONE
            codeLike -> OtpFit.CERTAIN
            OtpVocabulary.FIELD_PASSWORD.containsMatchIn(words) -> OtpFit.NONE
            else -> null
        }
    }

    private fun byType(inputType: Int, imeOptions: Int): OtpFit {
        val flags = inputType and InputType.TYPE_MASK_FLAGS
        return when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER ->
                if (flags and (InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL) != 0) OtpFit.NONE else OtpFit.LIKELY
            InputType.TYPE_CLASS_PHONE -> OtpFit.MAYBE
            InputType.TYPE_CLASS_DATETIME -> OtpFit.NONE
            InputType.TYPE_CLASS_TEXT -> byText(inputType, imeOptions)
            else -> OtpFit.MAYBE
        }
    }

    private fun byText(inputType: Int, imeOptions: Int): OtpFit {
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val multiline = (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        val sends = (imeOptions and EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEND
        return if (multiline || sends || variation in DENIED_TEXT) OtpFit.NONE else OtpFit.MAYBE
    }
}

object OtpGate {
    const val MAYBE_WINDOW_MS = 90_000L

    fun allows(fit: OtpFit, code: OtpCode, now: Long, existing: String): Boolean {
        val age = code.ageAt(now)
        if (fit == OtpFit.NONE || age < 0 || age > code.ttlMs) return false
        val seen = OtpText.flat(existing)
        val wanted = OtpText.flat(code.value)
        if (seen.isNotEmpty() && seen.equals(wanted, ignoreCase = true)) return false
        val compatible = seen.isEmpty() || wanted.startsWith(seen, ignoreCase = true)
        return when (fit) {
            OtpFit.CERTAIN -> true
            OtpFit.LIKELY -> compatible
            OtpFit.MAYBE -> !code.bare && seen.isEmpty() && age <= MAYBE_WINDOW_MS
            OtpFit.NONE -> false
        }
    }

    fun nextChange(fit: OtpFit, code: OtpCode): Long =
        if (fit == OtpFit.MAYBE) minOf(code.expiresAt, code.receivedAt + MAYBE_WINDOW_MS) else code.expiresAt
}
