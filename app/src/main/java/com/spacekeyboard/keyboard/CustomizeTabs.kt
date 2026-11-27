package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.DigitsMode
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.core.theme.ThemeSlot
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
internal fun StyleTab(theme: ThemeController) {
    val choice = rememberThemeSnapshot(theme).choice
    ThemeGrid(choice.themeId, theme::select)
    WallpaperSection(theme)
    SettingsCard {
        SwitchRow(stringResource(R.string.themes_follow_system), choice.followSystem, theme::setFollowSystem)
    }
    BodyText(R.string.themes_follow_note)
}

@Composable
internal fun ColorsTab(theme: ThemeController) {
    val snapshot = rememberThemeSnapshot(theme)
    var slot by remember { mutableStateOf(ThemeSlot.GLOW) }
    val requested = snapshot.choice.requested()
    SlotTabs(slot, requested) { slot = it }
    ColorEditor(requested[slot]) { theme.setColor(slot, it) }
    if (snapshot.palette.adjusted) Notice(R.string.colors_adjusted)
    SpaceButton(stringResource(R.string.reset_colors), theme::resetColors)
}

@Composable
internal fun KeysTab(layouts: LayoutController, language: String, onLanguage: (String) -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    val entries = LayoutCatalog.entries.filter { it.id in prefs.languages }
    val current = entries.firstOrNull { it.id == language } ?: return
    if (entries.size > 1) ChipGrid(entries, current, { it.nameRes }) { onLanguage(it.id) }
    KeyFnSection(layouts, language)
    SpaceButton(stringResource(R.string.reset_layout), onClick = { layouts.resetOrder(language) })
    if (language == "ar") ArabicStyleSection(prefs.arabicStyle, layouts)
}

@Composable
internal fun SizeTab(layouts: LayoutController) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ModeTiles(prefs.mode, layouts::setMode)
    SizeSliders(prefs, layouts)
    LabelStyleSection(prefs.label, layouts)
    SpaceButton(stringResource(R.string.reset_customization), layouts::resetAll)
}

@Composable
internal fun ToolsTab(layouts: LayoutController, onDragging: (Boolean) -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ToolbarEditor(prefs.toolbar, layouts::moveToolbar, onDragging)
    BodyText(R.string.editor_toolbar_bar_hint)
    SpaceButton(stringResource(R.string.reset_toolbar), layouts::resetToolbar)
    SpaceText(stringResource(R.string.typing_number_row), SpaceTheme.typography.titleSmall)
    ChipGrid(DigitsMode.entries, prefs.typing.digits, DigitsMode::labelRes, layouts::setDigitsMode)
    SettingsCard { SwitchRow(stringResource(R.string.typing_suggestions), prefs.typing.suggestions, layouts::setSuggestions) }
}

@Composable
internal fun TouchTab(layouts: LayoutController) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    FeedbackSection(prefs.feedback, layouts, withSound = false)
    BodyText(R.string.feedback_note)
}

@Composable
private fun ModeTiles(selected: PanelMode, onSelect: (PanelMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PanelMode.selectable.forEach { ModeTile(it, it == selected, onSelect) }
    }
}

@Composable
private fun RowScope.ModeTile(mode: PanelMode, selected: Boolean, onSelect: (PanelMode) -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton) { onSelect(mode) }
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SpaceIconView(modeIcon(mode), if (selected) colors.primary else colors.onSurface, size = 24.dp)
        SpaceText(stringResource(mode.labelRes), SpaceTheme.typography.label.copy(textAlign = TextAlign.Center))
    }
}

private fun modeIcon(mode: PanelMode): SpaceIcon = when (mode) {
    PanelMode.NORMAL -> SpaceIcon.MODE_NORMAL
    PanelMode.ONE_HAND_RIGHT -> SpaceIcon.MODE_ONE_RIGHT
    PanelMode.ONE_HAND_LEFT -> SpaceIcon.MODE_ONE_LEFT
    PanelMode.SPLIT -> SpaceIcon.MODE_SPLIT
    PanelMode.FLOAT -> SpaceIcon.FLOAT
}
