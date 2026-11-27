package com.spacekeyboard.keyboard.core.suggest

import android.text.InputType
import android.view.inputmethod.EditorInfo

enum class FieldKind(val separate: Boolean) {
    PROSE(false),
    SEARCH(true),
    NAME(true),
    ADDRESS(true),
    ;

    fun scope(packageName: String?): String? = when {
        this != PROSE -> "kind:${name.lowercase()}"
        packageName.isNullOrEmpty() -> null
        else -> "app:$packageName"
    }

    companion object {
        fun of(inputType: Int, imeOptions: Int): FieldKind {
            if ((inputType and InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) return PROSE
            return when (inputType and InputType.TYPE_MASK_VARIATION) {
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME -> NAME
                InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS -> ADDRESS
                else -> if ((imeOptions and EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEARCH) SEARCH else PROSE
            }
        }
    }
}
