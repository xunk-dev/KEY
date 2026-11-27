package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.text.ScriptGuess
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.Language
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.emoji.ui.HubTab
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.symbols.state.PanelTab
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateSource

class PanelToggles(
    private val panel: SymbolPanelModel,
    private val emoji: EmojiController,
    private val decor: DecorController,
    private val clip: ClipboardController,
    private val translate: TranslateController,
    private val images: ImagesController,
    private val shortcuts: ShortcutsController,
    private val calculator: CalculatorController,
    private val connection: () -> InputConnection?,
    private val keyboardLanguage: () -> String = { "en" },
) {
    private enum class Kind { DECOR, CLIP, TRANSLATE, IMAGES }

    fun toggleShortcuts() {
        if (shortcuts.open) {
            shortcuts.close()
            return
        }
        closeOthers(null)
        panel.close()
        shortcuts.show()
    }

    fun showCalculator() {
        closeOthers(null)
        panel.close()
        calculator.show()
    }

    fun showSymbols() {
        closeOthers(null)
        panel.open()
    }

    fun toggleSymbols() {
        if (panel.snapshot.open) panel.close() else showSymbols()
    }

    fun showTab(tab: HubTab) {
        if (isCurrent(tab)) return
        when (tab) {
            HubTab.EMOJI -> {
                panel.close()
                closeOthers(null)
                emoji.open()
            }
            HubTab.SYMBOLS -> showSymbols()
            HubTab.FACES -> {
                closeOthers(null)
                panel.openFaces()
            }
            HubTab.ART -> {
                closeOthers(null)
                panel.openArt()
            }
            HubTab.IMAGES -> {
                closeOthers(Kind.IMAGES)
                images.open()
            }
        }
    }

    private fun isCurrent(tab: HubTab) = when (tab) {
        HubTab.EMOJI -> emoji.snapshot.mode == EmojiMode.BROWSE
        HubTab.SYMBOLS -> panel.snapshot.open && panel.snapshot.tab == PanelTab.SYMBOLS
        HubTab.FACES -> panel.snapshot.open && panel.snapshot.tab == PanelTab.FACES
        HubTab.ART -> panel.snapshot.open && panel.snapshot.tab == PanelTab.ART
        HubTab.IMAGES -> images.snapshot.open
    }

    fun toggleDecor() {
        if (decor.snapshot.open) {
            decor.close()
            return
        }
        closeOthers(Kind.DECOR)
        val (text, kind) = DecorInput.read(connection())
        decor.open(text, kind, decorLanguage(text))
    }

    private fun decorLanguage(text: String): Language = when (ScriptGuess.of(text)) {
        ScriptGuess.Script.ARABIC -> Language.AR
        ScriptGuess.Script.LATIN -> Language.EN
        null -> if (LayoutCatalog.isRtl(keyboardLanguage())) Language.AR else Language.EN
    }

    fun toggleClip() {
        if (clip.snapshot.open) {
            clip.close()
            return
        }
        closeOthers(Kind.CLIP)
        clip.open()
    }

    fun toggleTranslate() {
        if (translate.state.open || !translate.state.allowed) {
            translate.close()
            return
        }
        closeOthers(Kind.TRANSLATE)
        val (text, kind) = TranslateInput.read(connection())
        translate.open(text, kind)
    }

    fun translateText(text: String) {
        if (!translate.state.allowed) return
        closeOthers(Kind.TRANSLATE)
        translate.open(text, TranslateSource.NONE)
    }

    fun toggleImages() {
        if (images.snapshot.open) {
            images.close()
            return
        }
        closeOthers(Kind.IMAGES)
        images.open()
    }

    fun showKeyboard() {
        shortcuts.close()
        calculator.close()
    }

    private fun closeOthers(keep: Kind?) {
        shortcuts.close()
        calculator.close()
        emoji.close()
        if (keep != Kind.DECOR) decor.close()
        if (keep != Kind.CLIP) clip.close()
        if (keep != Kind.TRANSLATE) translate.close()
        if (keep != Kind.IMAGES) images.close()
        if (keep != null) panel.close()
    }
}
