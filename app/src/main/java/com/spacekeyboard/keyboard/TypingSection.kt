package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.apps.AppLanguageStore
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController

@Composable
fun TypingSection(prefs: KeyboardPrefs, layouts: LayoutController) {
    val typing = prefs.typing
    val context = LocalContext.current
    var remembered by remember { mutableIntStateOf(AppLanguageStore(context).size()) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceText(stringResource(R.string.section_typing), SpaceTheme.typography.title)
        SwitchRow(stringResource(R.string.typing_number_row), typing.numberRow, layouts::setNumberRow)
        SwitchRow(stringResource(R.string.typing_suggestions), typing.suggestions, layouts::setSuggestions)
        BodyText(R.string.typing_suggestions_hint)
        SpaceButton(stringResource(R.string.typing_restore_blocked), { SuggestionStore(context).restoreBlocked() })
        SpaceButton(stringResource(R.string.typing_clear_learned), { SuggestionStore(context).clear() })
        SwitchRow(stringResource(R.string.typing_native_digits), typing.nativeDigits, layouts::setNativeDigits)
        SwitchRow(stringResource(R.string.typing_double_space), typing.doubleSpace, layouts::setDoubleSpace)
        SwitchRow(stringResource(R.string.typing_incognito), typing.incognito, layouts::setIncognito)
        SwitchRow(stringResource(R.string.typing_touch_assist), typing.touchAssist, layouts::setTouchAssist)
        SwitchRow(stringResource(R.string.typing_hardware_keys), typing.hardwareKeys, layouts::setHardwareKeys)
        SwitchRow(stringResource(R.string.typing_remember_language), typing.rememberLanguage, layouts::setRememberLanguage)
        SwitchRow(stringResource(R.string.typing_auto_language), typing.autoLanguage) { KeyboardPrefsStore(context).setAutoLanguage(it) }
        BodyText(R.string.typing_auto_language_hint)
        SpaceButton(stringResource(R.string.typing_clear_languages, remembered), {
            AppLanguageStore(context).clear()
            remembered = 0
        })
        SpaceText(stringResource(R.string.section_languages), SpaceTheme.typography.title)
        SpaceText(
            text = stringResource(R.string.languages_note),
            style = SpaceTheme.typography.bodySmall,
            color = SpaceTheme.colors.onSurfaceVariant,
        )
        LayoutCatalog.entries.filter { it.id in LayoutCatalog.optional }.forEach { entry ->
            SwitchRow(stringResource(entry.nameRes), entry.id in prefs.languages) { layouts.setLanguageEnabled(entry.id, it) }
        }
    }
}
