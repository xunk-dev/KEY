package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection

internal class SpellingStep(private val extras: TypingExtras) {
    private var lastOriginal: String? = null
    private var skip: String? = null

    fun apply(target: InputConnection): Pair<String, String>? {
        if (!extras.spellFix) return null
        val after = target.getTextAfterCursor(1, 0)?.toString().orEmpty()
        if (after.isNotEmpty() && after.first().isLetterOrDigit()) return null
        val before = target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        val fix = extras.spelling(before) ?: return null
        if (fix.original == skip) return null
        val inserted = fix.fixed + " "
        target.beginBatchEdit()
        target.deleteSurroundingText(fix.original.length, 0)
        target.commitText(inserted, 1)
        target.endBatchEdit()
        lastOriginal = fix.original
        return fix.original to inserted
    }

    fun reverted(word: String) {
        if (word == lastOriginal) skip = word
    }

    private companion object {
        const val WINDOW = 48
    }
}
