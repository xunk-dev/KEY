package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntry
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntryList
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.TypingPrefs
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

@Composable
internal fun TypingSettingsScreen(layouts: LayoutController, onOpen: (Screen) -> Unit, onBack: () -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ScreenFrame(R.string.settings_typing_title, onBack) {
        TypingBasics(prefs.typing, layouts)
        OtpSettings(prefs.typing)
        SpaceEntryList(listOf(SpaceEntry(stringResource(R.string.action_dictionary), SpaceIcon.WORDS, BadgeTone.Tool) { onOpen(Screen.DICTIONARY) }))
        LanguagesSection(prefs, layouts)
        AdvancedGroup {
            TypingAdvanced(prefs.typing, layouts)
            VoiceSection()
        }
    }
}

@Composable
private fun TypingBasics(typing: TypingPrefs, layouts: LayoutController) {
    val context = LocalContext.current
    SettingsCard {
        SwitchRow(stringResource(R.string.typing_suggestions), typing.suggestions, layouts::setSuggestions)
        SwitchRow(stringResource(R.string.typing_emoji_suggestions), typing.emojiSuggestions, layouts::setEmojiSuggestions)
        SwitchRow(stringResource(R.string.typing_app_suggestions), typing.appSuggestions, layouts::setAppSuggestions)
        SwitchRow(stringResource(R.string.typing_image_suggestions), typing.imageSuggestions) { KeyboardPrefsStore(context).setImageSuggestions(it) }
        SwitchRow(stringResource(R.string.typing_copy_offers), typing.copyOffers) { KeyboardPrefsStore(context).setCopyOffers(it) }
        SwitchRow(stringResource(R.string.typing_auto_language), typing.autoLanguage) { KeyboardPrefsStore(context).setAutoLanguage(it) }
        SwitchRow(stringResource(R.string.typing_double_space), typing.doubleSpace, layouts::setDoubleSpace)
        SwitchRow(stringResource(R.string.typing_auto_capitalize), typing.autoCapitalize, layouts::setAutoCapitalize)
    }
}

@Composable
private fun LanguagesSection(prefs: KeyboardPrefs, layouts: LayoutController) {
    ToolSection(R.string.section_languages) {
        BodyText(R.string.languages_note)
        SettingsCard {
            LayoutCatalog.entries.filter { it.id in LayoutCatalog.optional }.forEach { entry ->
                SwitchRow(stringResource(entry.nameRes), entry.id in prefs.languages) { layouts.setLanguageEnabled(entry.id, it) }
            }
        }
    }
}

@Composable
private fun TypingAdvanced(typing: TypingPrefs, layouts: LayoutController) {
    SettingsCard {
        SwitchRow(stringResource(R.string.typing_glide), typing.glide, layouts::setGlide)
        SwitchRow(stringResource(R.string.typing_convert), typing.convertOffers, layouts::setConvertOffers)
        SwitchRow(stringResource(R.string.typing_spell_fix), typing.spellFix, layouts::setSpellFix)
        SwitchRow(stringResource(R.string.typing_touch_assist), typing.touchAssist, layouts::setTouchAssist)
        SwitchRow(stringResource(R.string.typing_hardware_keys), typing.hardwareKeys, layouts::setHardwareKeys)
        SwitchRow(stringResource(R.string.typing_remember_language), typing.rememberLanguage, layouts::setRememberLanguage)
    }
    BodyText(R.string.typing_glide_hint)
    BodyText(R.string.typing_convert_hint)
    BodyText(R.string.typing_spell_fix_hint)
    BodyText(R.string.typing_suggestions_hint)
    BodyText(R.string.typing_emoji_suggestions_hint)
    BodyText(R.string.typing_app_suggestions_hint)
    BodyText(R.string.typing_image_suggestions_hint)
    BodyText(R.string.typing_copy_offers_hint)
    BodyText(R.string.typing_auto_language_hint)
}
