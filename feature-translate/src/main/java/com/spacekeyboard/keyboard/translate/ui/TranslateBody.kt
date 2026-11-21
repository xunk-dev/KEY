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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateFailure
import com.spacekeyboard.keyboard.translate.TranslatePolicy
import com.spacekeyboard.keyboard.translate.TranslateRoute
import com.spacekeyboard.keyboard.translate.TranslateState
import com.spacekeyboard.keyboard.translate.TranslateStatus

@Composable
internal fun TranslateBody(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    actions: TranslateActions,
    show: (TranslateView) -> Unit,
) {
    when (state.status) {
        TranslateStatus.IDLE -> IdleView(state, palette, controller)
        TranslateStatus.WORKING -> Message(stringResource(workingText(state)), palette)
        TranslateStatus.DONE -> DoneView(state, palette, actions)
        TranslateStatus.FAILED -> FailedView(state.failure, palette, controller, show)
    }
}

private fun workingText(state: TranslateState): Int =
    if (state.route == TranslateRoute.AI) R.string.translate_working_ai else R.string.translate_working_device

@Composable
private fun Message(text: String, palette: ThemePalette, content: @Composable () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label))
        content()
    }
}

@Composable
private fun IdleView(state: TranslateState, palette: ThemePalette, controller: TranslateController) {
    val hint = if (state.aiReady) R.string.translate_hint_ai else R.string.translate_hint_device
    Message(stringResource(hint), palette) {
        TranslateKey(stringResource(R.string.translate_action), palette, selected = true, onClick = controller::request)
    }
}

@Composable
private fun FailedView(
    failure: TranslateFailure?,
    palette: ThemePalette,
    controller: TranslateController,
    show: (TranslateView) -> Unit,
) {
    val reason = failure ?: TranslateFailure.SERVER
    Message(stringResource(TranslateText.failure(reason), TranslatePolicy.MAX_TEXT), palette) {
        when {
            reason == TranslateFailure.NO_CONSENT -> {
                TranslateKey(stringResource(R.string.translate_read_consent), palette, selected = true) {
                    show(TranslateView.DISCLOSURE)
                }
            }
            TranslateText.canRetry(reason) -> {
                TranslateKey(stringResource(R.string.translate_retry), palette, selected = true, onClick = controller::request)
            }
        }
    }
}

@Composable
private fun DoneView(state: TranslateState, palette: ThemePalette, actions: TranslateActions) {
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            SpaceText(state.result, SpaceTheme.typography.body, color = Color(palette.label))
        }
        DoneNote(state, palette)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TranslateKey(stringResource(TranslateText.use(state.sourceKind)), palette, selected = true, icon = TranslateText.useIcon(state.sourceKind)) {
                actions.onUse(state.result, state.sourceKind)
            }
            if (state.usedRoute == TranslateRoute.AI) {
                TranslateKey(stringResource(R.string.translate_report), palette, onClick = actions::onReport)
            }
        }
    }
}

@Composable
private fun DoneNote(state: TranslateState, palette: ThemePalette) {
    val remaining = state.remaining
    val note = when {
        state.fellBack -> stringResource(R.string.translate_note_fell_back)
        state.usedRoute == TranslateRoute.AI && remaining != null -> stringResource(R.string.translate_note_remaining, remaining)
        else -> return
    }
    SpaceText(note, SpaceTheme.typography.bodySmall, color = Color(palette.label))
}
