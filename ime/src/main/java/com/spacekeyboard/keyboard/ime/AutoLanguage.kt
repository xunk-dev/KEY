package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.core.text.ScriptGuess
import com.spacekeyboard.keyboard.ime.layout.LayoutController

internal class AutoLanguage(
    private val layouts: LayoutController,
    private val connection: () -> InputConnection?,
    private val allowed: () -> Boolean,
) {
    private var settled = ""

    fun onFieldStart() = decide()

    fun onCursorMoved() {
        if (settled.isNotEmpty() && layouts.snapshot.prefs.language != settled) return
        decide()
    }

    private fun decide() {
        val prefs = layouts.snapshot.prefs
        settled = prefs.language
        if (!prefs.typing.autoLanguage || !allowed()) return
        val before = connection()?.getTextBeforeCursor(ScriptGuess.WINDOW, 0) ?: return
        val target = ScriptGuess.layoutFor(ScriptGuess.of(before), prefs.language, prefs.languages) ?: return
        if (layouts.selectLanguage(target)) settled = target
    }
}
