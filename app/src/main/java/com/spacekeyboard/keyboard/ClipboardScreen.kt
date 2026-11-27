package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
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
import com.spacekeyboard.keyboard.clipboard.ClipPrefsStore
import com.spacekeyboard.keyboard.clipboard.ClipStoreFactory
import com.spacekeyboard.keyboard.clipboard.ClipTabOrder
import com.spacekeyboard.keyboard.clipboard.UnlockWindow
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

@Composable
fun ClipboardScreen(layouts: LayoutController, onBack: () -> Unit) {
    val context = LocalContext.current
    val keepOpen = rememberLayoutSnapshot(layouts).prefs.feedback.clipKeepOpen
    val store = remember { ClipPrefsStore(context) }
    var enabled by remember { mutableStateOf(store.enabled()) }
    var window by remember { mutableStateOf(store.window()) }
    var order by remember { mutableStateOf(store.tabOrder()) }
    ScreenFrame(R.string.clipboard_title, onBack) {
        ClipboardPoints()
        HistorySwitch(enabled) { value ->
            store.setEnabled(value)
            enabled = value
        }
        SettingsCard { SwitchRow(stringResource(R.string.feedback_clip_keep_open), keepOpen, layouts::setClipKeepOpen) }
        ToolSection(R.string.clipboard_window_title) {
            ClipNote(R.string.clipboard_window_note)
            ChipGrid(UnlockWindow.entries, window, ::windowLabel) {
                store.setWindow(it)
                window = it
            }
        }
        ToolSection(R.string.clipboard_tabs_title) {
            ClipNote(R.string.clipboard_tabs_note)
            if (order != ClipTabOrder.DEFAULT) {
                SpaceButton(stringResource(R.string.clipboard_tabs_reset), {
                    store.resetTabOrder()
                    order = store.tabOrder()
                })
            }
        }
        WipeSection { ClipStoreFactory.wipe(context); store.requestWipe() }
        ClipNote(R.string.clipboard_toolbar_note)
    }
}

private fun windowLabel(window: UnlockWindow): Int = when (window) {
    UnlockWindow.EVERY_TIME -> R.string.clipboard_window_every
    UnlockWindow.ONE_MINUTE -> R.string.clipboard_window_1m
    UnlockWindow.FIVE_MINUTES -> R.string.clipboard_window_5m
    UnlockWindow.FIFTEEN_MINUTES -> R.string.clipboard_window_15m
    UnlockWindow.ONE_HOUR -> R.string.clipboard_window_1h
}

@Composable
private fun ClipNote(res: Int) {
    SpaceText(stringResource(res), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
}

@Composable
private fun HistorySwitch(enabled: Boolean, onChange: (Boolean) -> Unit) {
    val status = if (enabled) R.string.clipboard_status_on else R.string.clipboard_status_off
    val action = if (enabled) R.string.clipboard_turn_off else R.string.clipboard_turn_on
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceSectionTitle(stringResource(status))
        SpacePrimaryButton(stringResource(action), { onChange(!enabled) })
    }
}

@Composable
private fun WipeSection(onWipe: () -> Unit) {
    ConfirmAction(
        R.string.clipboard_wipe_ask, R.string.clipboard_wipe_yes, R.string.clipboard_wipe_no, R.string.clipboard_wipe, onWipe,
    )
}
