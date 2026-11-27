package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.layout.TouchCorrector
import com.spacekeyboard.keyboard.core.suggest.TextContext

internal class TouchAssist(
    suggest: SuggestionController,
    private val connection: () -> InputConnection?,
    private val enabled: () -> Boolean,
) {
    private val corrector = TouchCorrector(suggest::priorWeight)

    fun resolve(placement: Placement, hit: PlacedKey, x: Float, y: Float): PlacedKey {
        if (!enabled()) return hit
        return corrector.choose(placement, hit, x, y) {
            TextContext.parse(connection()?.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()).prefix
        }
    }

    private companion object {
        const val WINDOW = 24
    }
}
