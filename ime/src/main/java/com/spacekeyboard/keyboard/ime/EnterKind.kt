package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.EditorInfo

enum class EnterKind {
    ENTER, GO, SEARCH, SEND, NEXT, DONE, PREVIOUS;

    companion object {
        fun of(imeOptions: Int): EnterKind {
            if ((imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return ENTER
            return when (imeOptions and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_GO -> GO
                EditorInfo.IME_ACTION_SEARCH -> SEARCH
                EditorInfo.IME_ACTION_SEND -> SEND
                EditorInfo.IME_ACTION_NEXT -> NEXT
                EditorInfo.IME_ACTION_DONE -> DONE
                EditorInfo.IME_ACTION_PREVIOUS -> PREVIOUS
                else -> ENTER
            }
        }
    }
}
