package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.shake.ShakeAction
import com.spacekeyboard.keyboard.emoji.ui.HubTab

internal class ShakeRouter(
    private val toggles: PanelToggles,
    private val calculator: CalculatorController,
    private val dock: DockRoot,
    private val voice: () -> Unit,
    private val ready: () -> Boolean,
    private val settle: () -> Unit,
) {
    fun run(action: ShakeAction) {
        if (!ready()) return
        settle()
        when (action) {
            ShakeAction.CALCULATOR -> if (calculator.open) calculator.close() else toggles.showCalculator()
            ShakeAction.EMOJI -> toggles.showTab(HubTab.EMOJI)
            ShakeAction.CLIPBOARD -> toggles.toggleClip()
            ShakeAction.TRANSLATE -> toggles.toggleTranslate()
            ShakeAction.SHORTCUTS -> toggles.toggleShortcuts()
            ShakeAction.VOICE -> voice()
            ShakeAction.SYMBOLS -> toggles.toggleSymbols()
            ShakeAction.IMAGES -> toggles.toggleImages()
            ShakeAction.DECOR -> toggles.toggleDecor()
            ShakeAction.FLOAT -> dock.toggleFloat()
        }
    }
}
