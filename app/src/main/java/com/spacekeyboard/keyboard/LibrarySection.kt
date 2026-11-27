package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

private enum class LibraryTab(val label: Int, val soon: Int) {
    THEMES(R.string.action_themes, R.string.library_themes_more),
    EMOJI(R.string.library_tab_emoji, R.string.library_emoji_soon),
    FONTS(R.string.library_tab_fonts, R.string.library_fonts_soon),
}

@Composable
internal fun LibrarySection(theme: ThemeController, layouts: LayoutController) {
    var tab by rememberSaveable { mutableStateOf(LibraryTab.THEMES) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SpaceSectionTitle(stringResource(R.string.library_title))
        ChipGrid(LibraryTab.entries, tab, { it.label }, { tab = it })
        if (tab == LibraryTab.THEMES) ThemesTab(theme, layouts)
        SettingsCard {
            SpaceText(stringResource(tab.soon), SpaceTheme.typography.body, color = SpaceTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemesTab(theme: ThemeController, layouts: LayoutController) {
    val snapshot = rememberThemeSnapshot(theme)
    KeyboardPreview(theme, layouts)
    ThemeGrid(snapshot.choice.themeId, theme::select)
    SettingsCard {
        SwitchRow(stringResource(R.string.themes_follow_system), snapshot.choice.followSystem, theme::setFollowSystem)
    }
    BodyText(R.string.themes_follow_note)
}
