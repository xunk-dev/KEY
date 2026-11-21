package com.spacekeyboard.keyboard.core

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldPolicyTest {
    private val text = InputType.TYPE_CLASS_TEXT

    @Test
    fun textPasswordIsSensitive() {
        assertTrue(FieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_PASSWORD))
    }

    @Test
    fun visiblePasswordIsSensitive() {
        assertTrue(FieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD))
    }

    @Test
    fun webPasswordIsSensitive() {
        assertTrue(FieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
    }

    @Test
    fun numberPasswordIsSensitive() {
        val type = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        assertTrue(FieldPolicy.isSensitive(type))
    }

    @Test
    fun plainTextIsNotSensitive() {
        assertFalse(FieldPolicy.isSensitive(text))
    }

    @Test
    fun noLearningFlagStopsLearning() {
        val options = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        assertFalse(FieldPolicy.mayLearn(text, options))
    }

    @Test
    fun textFieldsAcceptEmoji() {
        assertTrue(FieldPolicy.acceptsEmoji(text))
        assertTrue(FieldPolicy.acceptsEmoji(text or InputType.TYPE_TEXT_FLAG_MULTI_LINE))
        assertTrue(FieldPolicy.acceptsEmoji(text or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE))
    }

    @Test
    fun numbersEmailsAndAddressesRejectEmoji() {
        assertFalse(FieldPolicy.acceptsEmoji(InputType.TYPE_CLASS_NUMBER))
        assertFalse(FieldPolicy.acceptsEmoji(InputType.TYPE_CLASS_PHONE))
        assertFalse(FieldPolicy.acceptsEmoji(text or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertFalse(FieldPolicy.acceptsEmoji(text or InputType.TYPE_TEXT_VARIATION_URI))
    }

    @Test
    fun plainTextMayLearn() {
        assertTrue(FieldPolicy.mayLearn(text, 0))
    }

    @Test
    fun plainTextMayAutoCorrect() {
        assertTrue(FieldPolicy.mayAutoCorrect(text))
        assertTrue(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_FLAG_MULTI_LINE))
        assertTrue(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE))
    }

    @Test
    fun passwordsNeverAutoCorrect() {
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD))
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
        assertFalse(FieldPolicy.mayAutoCorrect(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
    }

    @Test
    fun emailsAndAddressesNeverAutoCorrect() {
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS))
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_VARIATION_URI))
    }

    @Test
    fun noSuggestionsFlagStopsAutoCorrect() {
        assertFalse(FieldPolicy.mayAutoCorrect(text or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS))
    }

    @Test
    fun nonTextClassesNeverAutoCorrect() {
        assertFalse(FieldPolicy.mayAutoCorrect(InputType.TYPE_CLASS_NUMBER))
        assertFalse(FieldPolicy.mayAutoCorrect(InputType.TYPE_CLASS_PHONE))
    }

    @Test
    fun emailsAddressesAndPasswordsPreferLatin() {
        assertTrue(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertTrue(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS))
        assertTrue(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_VARIATION_URI))
        assertTrue(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertTrue(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
    }

    @Test
    fun ordinaryFieldsKeepTheChosenLanguage() {
        assertFalse(FieldPolicy.prefersLatin(text))
        assertFalse(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_FLAG_MULTI_LINE))
        assertFalse(FieldPolicy.prefersLatin(text or InputType.TYPE_TEXT_VARIATION_PERSON_NAME))
        assertFalse(FieldPolicy.prefersLatin(InputType.TYPE_CLASS_NUMBER))
        assertFalse(FieldPolicy.prefersLatin(InputType.TYPE_NULL))
    }

    @Test
    fun onlyEditorsWithoutAnInputTypeNeedRawKeys() {
        assertTrue(FieldPolicy.needsRawKeys(InputType.TYPE_NULL))
        assertFalse(FieldPolicy.needsRawKeys(text))
        assertFalse(FieldPolicy.needsRawKeys(InputType.TYPE_CLASS_NUMBER))
        assertFalse(FieldPolicy.needsRawKeys(InputType.TYPE_CLASS_PHONE))
    }
}
