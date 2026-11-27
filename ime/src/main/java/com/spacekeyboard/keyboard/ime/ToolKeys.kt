package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.emoji.ui.HubTab

internal fun PanelToggles.openTool(action: KeyAction) = when (action) {
    KeyAction.EMOJI -> showTab(HubTab.EMOJI)
    KeyAction.CLIPBOARD -> toggleClip()
    else -> toggleTranslate()
}
