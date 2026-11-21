package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.spacekeyboard.keyboard.translate.TranslateLanguages
import com.spacekeyboard.keyboard.translate.TranslateState

@Composable
internal fun TranslateLanguagePanel(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    back: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales.get(0)
    val strong = TranslateLanguages.ordered.filter { TranslateLanguages.isStrong(it) }
    val other = TranslateLanguages.ordered.filter { !TranslateLanguages.isStrong(it) }
    val pick = { code: String ->
        controller.setTarget(code)
        back()
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TranslateKey(stringResource(R.string.translate_back), palette, onClick = back)
            SpaceText(stringResource(R.string.translate_choose_language), SpaceTheme.typography.bodySmall, color = Color(palette.label))
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(strong.size) { index -> LanguageKey(strong[index], state, palette, locale, pick) }
            item { Note(stringResource(R.string.translate_other_languages), palette) }
            items(other.size) { index -> LanguageKey(other[index], state, palette, locale, pick) }
        }
    }
}

@Composable
private fun LanguageKey(
    code: String,
    state: TranslateState,
    palette: ThemePalette,
    locale: java.util.Locale,
    pick: (String) -> Unit,
) {
    TranslateKey(TranslateLanguages.displayName(code, locale), palette, Modifier.fillMaxWidth(), code == state.target) { pick(code) }
}

@Composable
private fun Note(text: String, palette: ThemePalette) {
    SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label), modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp))
}
