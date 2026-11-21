package com.spacekeyboard.keyboard.core

import android.text.InputType
import android.view.inputmethod.EditorInfo

object FieldPolicy {
    private val textPasswordVariations = setOf(
        InputType.TYPE_TEXT_VARIATION_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
    )

    private val plainOnlyVariations = setOf(
        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
        InputType.TYPE_TEXT_VARIATION_URI,
    )

    fun isSensitive(inputType: Int): Boolean {
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_TEXT -> variation in textPasswordVariations
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    fun prefersLatin(inputType: Int): Boolean {
        if ((inputType and InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) return false
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return variation in plainOnlyVariations || variation in textPasswordVariations
    }

    fun needsRawKeys(inputType: Int): Boolean = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_NULL

    fun requestsNoLearning(imeOptions: Int): Boolean =
        (imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0

    fun acceptsEmoji(inputType: Int): Boolean {
        if ((inputType and InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) return false
        return (inputType and InputType.TYPE_MASK_VARIATION) !in plainOnlyVariations
    }

    fun mayLearn(inputType: Int, imeOptions: Int): Boolean =
        !isSensitive(inputType) && !requestsNoLearning(imeOptions)

    fun mayAutoCorrect(inputType: Int): Boolean {
        if ((inputType and InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) return false
        if (isSensitive(inputType)) return false
        if ((inputType and InputType.TYPE_MASK_VARIATION) in plainOnlyVariations) return false
        return (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == 0
    }
}
