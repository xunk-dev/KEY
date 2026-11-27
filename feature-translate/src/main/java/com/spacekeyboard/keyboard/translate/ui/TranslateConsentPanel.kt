package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslatePolicy
import com.spacekeyboard.keyboard.translate.TranslateState

@Composable
internal fun TranslateConsentPanel(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    back: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SpaceText(stringResource(R.string.translate_consent_title), SpaceTheme.typography.body, color = Color(palette.label))
            SpaceText(
                stringResource(R.string.translate_consent_body, TranslatePolicy.MAX_TEXT),
                SpaceTheme.typography.bodySmall,
                color = Color(palette.label),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConsentKeys(state, palette, controller, back)
        }
    }
}

@Composable
private fun ConsentKeys(state: TranslateState, palette: ThemePalette, controller: TranslateController, back: () -> Unit) {
    if (state.consent) {
        TranslateKey(stringResource(R.string.translate_withdraw), palette, selected = true) {
            controller.withdrawConsent()
            back()
        }
    } else {
        TranslateKey(stringResource(R.string.translate_agree), palette, selected = true) {
            controller.grantConsent()
            back()
        }
    }
    TranslateKey(stringResource(if (state.consent) R.string.translate_back else R.string.translate_not_now), palette, onClick = back)
}
