package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.DecorStyle
import com.spacekeyboard.keyboard.decorate.Language
import com.spacekeyboard.keyboard.decorate.SourceKind
import com.spacekeyboard.keyboard.decorate.ui.DecorActions
import com.spacekeyboard.keyboard.decorate.R as DecorR

class ImeDecorActions(
    private val context: Context,
    private val decor: DecorController,
    private val connection: () -> InputConnection?,
) : DecorActions {
    override fun onKeys() = decor.close()

    override fun onApply(style: DecorStyle) {
        val state = decor.snapshot
        warnScreenReader()
        if (state.kind == SourceKind.NONE) {
            decor.toggleActive(style.id)
            decor.close()
            return
        }
        val target = connection() ?: return
        if (state.kind == SourceKind.BEFORE) target.deleteSurroundingText(state.source.length, 0)
        target.commitText(style.apply(state.source), 1)
        decor.close()
    }

    override fun onLanguage(language: Language) = decor.setLanguage(language)

    override fun onFavorite(id: String) = decor.toggleFavorite(id)

    override fun onActive(id: String) {
        warnScreenReader()
        decor.toggleActive(id)
        decor.close()
    }

    override fun onFavoritesOnly() = decor.toggleFavoritesOnly()

    override fun onClearActive() = decor.clearActive()

    private fun warnScreenReader() {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return
        if (manager.isTouchExplorationEnabled) {
            Toast.makeText(context, DecorR.string.decor_screen_reader_warning, Toast.LENGTH_LONG).show()
        }
    }
}
