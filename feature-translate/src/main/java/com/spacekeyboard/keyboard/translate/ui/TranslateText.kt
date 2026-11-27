package com.spacekeyboard.keyboard.translate.ui

import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateFailure
import com.spacekeyboard.keyboard.translate.TranslateStyle

object TranslateText {
    fun failure(failure: TranslateFailure): Int = when (failure) {
        TranslateFailure.FIELD_BLOCKED -> R.string.translate_fail_blocked
        TranslateFailure.NOT_REQUESTED -> R.string.translate_fail_not_requested
        TranslateFailure.EMPTY -> R.string.translate_fail_empty
        TranslateFailure.TOO_LONG -> R.string.translate_fail_too_long
        TranslateFailure.NO_CONSENT -> R.string.translate_fail_no_consent
        TranslateFailure.NETWORK -> R.string.translate_fail_network
        TranslateFailure.QUOTA -> R.string.translate_fail_quota
        TranslateFailure.REJECTED -> R.string.translate_fail_rejected
        TranslateFailure.SERVER -> R.string.translate_fail_server
        TranslateFailure.UNSUPPORTED -> R.string.translate_fail_unsupported
        TranslateFailure.MODEL -> R.string.translate_fail_model
        TranslateFailure.NEEDS_MODEL -> R.string.translate_needs_model_generic
        TranslateFailure.STORAGE -> R.string.translate_fail_storage
    }

    fun style(style: TranslateStyle): Int = when (style) {
        TranslateStyle.NEUTRAL -> R.string.translate_style_neutral
        TranslateStyle.FORMAL -> R.string.translate_style_formal
        TranslateStyle.CASUAL -> R.string.translate_style_casual
        TranslateStyle.CHAT -> R.string.translate_style_chat
        TranslateStyle.POLITE -> R.string.translate_style_polite
        TranslateStyle.CONCISE -> R.string.translate_style_concise
    }

    fun canRetry(failure: TranslateFailure): Boolean =
        failure == TranslateFailure.NETWORK || failure == TranslateFailure.SERVER || failure == TranslateFailure.MODEL ||
            failure == TranslateFailure.STORAGE
}
