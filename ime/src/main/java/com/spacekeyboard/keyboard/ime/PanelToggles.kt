package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController

class PanelToggles(
    private val panel: SymbolPanelModel,
    private val emoji: EmojiController,
    private val decor: DecorController,
    private val clip: ClipboardController,
    private val translate: TranslateController,
    private val images: ImagesController,
    private val connection: () -> InputConnection?,
) {
    private enum class Kind { DECOR, CLIP, TRANSLATE, IMAGES }

    fun showSymbols() {
        closeOthers(null)
        panel.open()
    }

    fun toggleDecor() {
        if (decor.snapshot.open) {
            decor.close()
            return
        }
        closeOthers(Kind.DECOR)
        val (text, kind) = DecorInput.read(connection())
        decor.open(text, kind)
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

    fun toggleImages() {
        if (images.snapshot.open) {
            images.close()
            return
        }
        closeOthers(Kind.IMAGES)
        images.open()
    }

    private fun closeOthers(keep: Kind?) {
        emoji.close()
        if (keep != Kind.DECOR) decor.close()
        if (keep != Kind.CLIP) clip.close()
        if (keep != Kind.TRANSLATE) translate.close()
        if (keep != Kind.IMAGES) images.close()
        if (keep != null) panel.close()
    }
}
