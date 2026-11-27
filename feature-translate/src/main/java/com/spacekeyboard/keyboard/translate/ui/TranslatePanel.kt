package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.rememberTranslateState

internal enum class TranslateView { MAIN, DISCLOSURE }

@Composable
fun TranslatePanelHost(
    controller: TranslateController,
    palette: ThemePalette,
    actions: TranslateActions,
    compact: Boolean = true,
) {
    val state = rememberTranslateState(controller)
    var view by remember { mutableStateOf(TranslateView.MAIN) }
    LaunchedEffect(state.open) { if (!state.open) view = TranslateView.MAIN }
    LaunchedEffect(state.open, view) { controller.setExpanded(state.open && view != TranslateView.MAIN) }
    if (!state.open) return
    val show: (TranslateView) -> Unit = { view = it }
    val back = { view = TranslateView.MAIN }
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        when (view) {
            TranslateView.MAIN -> TranslateMain(state, palette, controller, actions, show, compact)
            TranslateView.DISCLOSURE -> TranslateConsentPanel(state, palette, controller, back)
        }
    }
}
