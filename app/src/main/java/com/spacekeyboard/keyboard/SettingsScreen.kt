package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntry
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntryList
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

private class SettingsRow(val label: Int, val icon: SpaceIcon, val screen: Screen)

private val settingsRows = listOf(
    SettingsRow(R.string.settings_typing_title, SpaceIcon.WORDS, Screen.SETTINGS_TYPING),
    SettingsRow(R.string.settings_touch_title, SpaceIcon.TOOLS, Screen.SETTINGS_TOUCH),
    SettingsRow(R.string.settings_data_title, SpaceIcon.PRIVACY, Screen.SETTINGS_DATA),
)

@Composable
internal fun SettingsScreen(layouts: LayoutController, onOpen: (Screen) -> Unit, onBack: () -> Unit) {
    val notes = settingsNotes(rememberLayoutSnapshot(layouts).prefs)
    ScreenFrame(R.string.settings_title, onBack) {
        SpaceEntryList(
            settingsRows.mapIndexed { index, row ->
                SpaceEntry(stringResource(row.label), row.icon, BadgeTone.Look, notes[index]) { onOpen(row.screen) }
            },
        )
    }
}

@Composable
private fun settingsNotes(prefs: KeyboardPrefs): List<String> {
    val typing = if (prefs.typing.suggestions) R.string.settings_note_typing_on else R.string.settings_note_typing_off
    val touch = if (prefs.feedback.vibrate) R.string.settings_note_touch_on else R.string.settings_note_touch_off
    val data = if (prefs.typing.incognito) R.string.settings_note_data_incognito else R.string.settings_note_data
    return listOf(stringResource(typing), stringResource(touch), stringResource(data))
}
