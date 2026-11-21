package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.TranslatePrefsStore
import com.spacekeyboard.keyboard.translate.TranslateRoute

@Composable
fun TranslateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { TranslatePrefsStore(context) }
    var route by remember { mutableStateOf(store.route()) }
    var consent by remember { mutableStateOf(store.consent()) }
    ScreenFrame(R.string.settings_translate_title, onBack) {
        TranslateNote(R.string.settings_translate_intro)
        RouteSection(route, consent) {
            store.setRoute(it)
            route = store.route()
        }
        ConsentSection(consent) {
            store.withdrawConsent()
            consent = store.consent()
            route = store.route()
        }
        TranslateModelsSection()
    }
}

@Composable
internal fun TranslateNote(res: Int) {
    SpaceText(stringResource(res), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
}

private fun routeLabel(route: TranslateRoute): Int = when (route) {
    TranslateRoute.DEVICE -> R.string.settings_translate_route_device
    TranslateRoute.AI -> R.string.settings_translate_route_ai
}

@Composable
private fun RouteSection(route: TranslateRoute, consent: Boolean, onPick: (TranslateRoute) -> Unit) {
    ToolSection(R.string.settings_translate_route_title) {
        ChipGrid(TranslateRoute.entries, route, ::routeLabel, onPick)
        if (!consent) TranslateNote(R.string.settings_translate_route_needs_consent)
    }
}

@Composable
private fun ConsentSection(consent: Boolean, onWithdraw: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val status = if (consent) R.string.settings_translate_consent_on else R.string.settings_translate_consent_off
        SpaceSectionTitle(stringResource(status))
        if (consent) {
            TranslateNote(R.string.settings_translate_withdraw_note)
            SpaceButton(stringResource(R.string.settings_translate_withdraw), onWithdraw)
        }
    }
}
