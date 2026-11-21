package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateState
import com.spacekeyboard.keyboard.translate.rememberTranslateState

private const val SCRIM_ALPHA = 0.55f

internal enum class TranslateView { MAIN, LANGUAGES, DISCLOSURE }

@Composable
fun TranslatePanelHost(
    controller: TranslateController,
    palette: ThemePalette,
    actions: TranslateActions,
    keysLabel: Int = R.string.translate_keys,
) {
    val state = rememberTranslateState(controller)
    var view by remember { mutableStateOf(TranslateView.MAIN) }
    LaunchedEffect(state.open) { if (!state.open) view = TranslateView.MAIN }
    if (!state.open) return
    val back = { view = TranslateView.MAIN }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark) {
            when (view) {
                TranslateView.MAIN -> TranslateMain(state, palette, controller, actions, keysLabel) { view = it }
                TranslateView.LANGUAGES -> TranslateLanguagePanel(state, palette, controller, back)
                TranslateView.DISCLOSURE -> TranslateConsentPanel(state, palette, controller, back)
            }
        }
    }
}

@Composable
internal fun TranslateMain(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    actions: TranslateActions,
    keysLabel: Int,
    show: (TranslateView) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TranslateBar(state, palette, controller, actions, keysLabel, show)
        if (state.aiReady) TranslateStyleBar(state, palette, controller, show)
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(palette.background).copy(alpha = SCRIM_ALPHA))) {
            TranslateBody(state, palette, controller, actions, show)
        }
    }
}
