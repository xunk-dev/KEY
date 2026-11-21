package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateLabels
import com.spacekeyboard.keyboard.translate.TranslateLanguages
import com.spacekeyboard.keyboard.translate.TranslateRoute
import com.spacekeyboard.keyboard.translate.TranslateState
import com.spacekeyboard.keyboard.translate.TranslateStyle

private const val PREVIEW_MAX = 40

@Composable
internal fun TranslateBar(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    actions: TranslateActions,
    keysLabel: Int,
    show: (TranslateView) -> Unit,
) {
    val locale = LocalConfiguration.current.locales.get(0)
    val language = TranslateLanguages.displayName(state.target, locale)
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TranslateKey(stringResource(keysLabel), palette, onClick = actions::onKeys)
        Box(Modifier.weight(1f).padding(horizontal = 4.dp)) {
            SpaceText(
                TranslateLabels.preview(state.source, PREVIEW_MAX),
                SpaceTheme.typography.bodySmall,
                color = Color(palette.label),
            )
        }
        TranslateKey(
            stringResource(R.string.translate_to, language),
            palette,
            description = stringResource(R.string.translate_choose_language),
        ) { show(TranslateView.LANGUAGES) }
        TranslateKey(routeLabel(state), palette, selected = state.aiReady) { toggleRoute(state, controller, show) }
    }
}

@Composable
private fun routeLabel(state: TranslateState): String =
    stringResource(if (state.aiReady) R.string.translate_route_ai else R.string.translate_route_device)

private fun toggleRoute(state: TranslateState, controller: TranslateController, show: (TranslateView) -> Unit) {
    when {
        state.aiReady -> controller.setRoute(TranslateRoute.DEVICE)
        state.consent -> controller.setRoute(TranslateRoute.AI)
        else -> show(TranslateView.DISCLOSURE)
    }
}

@Composable
internal fun TranslateStyleBar(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    show: (TranslateView) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(TranslateStyle.entries.size) { index ->
            val style = TranslateStyle.entries[index]
            TranslateKey(stringResource(TranslateText.style(style)), palette, selected = style == state.style) {
                controller.setStyle(style)
            }
        }
        item { TranslateKey(stringResource(R.string.translate_privacy), palette) { show(TranslateView.DISCLOSURE) } }
    }
}
