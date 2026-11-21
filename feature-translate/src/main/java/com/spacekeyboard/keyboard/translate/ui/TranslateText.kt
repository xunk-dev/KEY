package com.spacekeyboard.keyboard.translate.ui

import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateFailure
import com.spacekeyboard.keyboard.translate.TranslateSource
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
    }

    fun style(style: TranslateStyle): Int = when (style) {
        TranslateStyle.NEUTRAL -> R.string.translate_style_neutral
        TranslateStyle.FORMAL -> R.string.translate_style_formal
        TranslateStyle.CASUAL -> R.string.translate_style_casual
        TranslateStyle.CHAT -> R.string.translate_style_chat
        TranslateStyle.POLITE -> R.string.translate_style_polite
        TranslateStyle.CONCISE -> R.string.translate_style_concise
    }

    fun use(kind: TranslateSource): Int = when (kind) {
        TranslateSource.SELECTION -> R.string.translate_use_selection
        TranslateSource.ALL -> R.string.translate_use_all
        TranslateSource.NONE -> R.string.translate_use_insert
        TranslateSource.READONLY -> R.string.translate_use_copy
    }

    fun useIcon(kind: TranslateSource): PanelIcon =
        if (kind == TranslateSource.READONLY) PanelIcon.COPY else PanelIcon.DONE

    fun canRetry(failure: TranslateFailure): Boolean =
        failure == TranslateFailure.NETWORK || failure == TranslateFailure.SERVER || failure == TranslateFailure.MODEL
}
