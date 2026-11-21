package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.ui.TranslateActions
import com.spacekeyboard.keyboard.translate.ui.TranslatePanelHost

@Composable
fun TranslatePanelContent(theme: ThemeController, translate: TranslateController, actions: TranslateActions) {
    TranslatePanelHost(translate, rememberThemeSnapshot(theme).palette, actions)
}
