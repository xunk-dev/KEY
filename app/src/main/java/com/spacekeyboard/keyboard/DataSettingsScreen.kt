package com.spacekeyboard.keyboard

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.autofix.AutoFixPrefsStore
import com.spacekeyboard.keyboard.autofix.ModelStore
import com.spacekeyboard.keyboard.clipboard.ClipPrefsStore
import com.spacekeyboard.keyboard.clipboard.ClipStoreFactory
import com.spacekeyboard.keyboard.core.apps.AppLanguageStore
import com.spacekeyboard.keyboard.core.macro.MacroStore
import com.spacekeyboard.keyboard.core.suggest.ScopedStore
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntry
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntryList
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.images.ImageStore
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

private class ClearItem(val label: Int, val run: (Context) -> Unit)

private fun clearItems(emoji: EmojiController) = listOf(
    ClearItem(R.string.typing_clear_learned) { SuggestionStore(it).clear(); ScopedStore(it).clear() },
    ClearItem(R.string.settings_clear_languages) { AppLanguageStore(it).clear() },
    ClearItem(R.string.clipboard_wipe) { ClipStoreFactory.wipe(it); ClipPrefsStore(it).requestWipe() },
    ClearItem(R.string.emoji_clear_recents) { emoji.clearRecents() },
    ClearItem(R.string.images_clear) { ImageStore(it).clear() },
    ClearItem(R.string.autofix_wipe) { ModelStore(it).clear(); AutoFixPrefsStore(it).bumpEpoch() },
    ClearItem(R.string.macro_clear_all) { MacroStore(it).clear() },
)

@Composable
internal fun DataSettingsScreen(layouts: LayoutController, emoji: EmojiController, onOpen: (Screen) -> Unit, onBack: () -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ScreenFrame(R.string.settings_data_title, onBack) {
        SettingsCard {
            SwitchRow(stringResource(R.string.typing_incognito), prefs.typing.incognito, layouts::setIncognito)
        }
        ToolSection(R.string.settings_clear_title) { ClearActions(emoji) }
        SpaceEntryList(
            listOf(
                SpaceEntry(stringResource(R.string.action_dictionary), SpaceIcon.WORDS, BadgeTone.Tool) { onOpen(Screen.DICTIONARY) },
                SpaceEntry(stringResource(R.string.action_privacy), SpaceIcon.PRIVACY, BadgeTone.Help) { onOpen(Screen.PRIVACY) },
                SpaceEntry(stringResource(R.string.action_backup), SpaceIcon.BACKUP, BadgeTone.Help) { onOpen(Screen.BACKUP) },
            ),
        )
    }
}

@Composable
private fun ClearActions(emoji: EmojiController) {
    val context = LocalContext.current
    clearItems(emoji).forEach { item ->
        ConfirmAction(R.string.settings_clear_ask, R.string.settings_clear_yes, R.string.settings_clear_no, item.label) {
            item.run(context)
        }
    }
    SpaceButton(stringResource(R.string.typing_restore_blocked), { SuggestionStore(context).restoreBlocked() })
}
