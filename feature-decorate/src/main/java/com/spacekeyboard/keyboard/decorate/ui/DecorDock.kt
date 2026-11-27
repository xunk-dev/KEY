package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.decorate.DecorCategory
import com.spacekeyboard.keyboard.decorate.DecorSnapshot
import com.spacekeyboard.keyboard.decorate.Language
import com.spacekeyboard.keyboard.decorate.R
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.emoji.ui.DockBar
import com.spacekeyboard.keyboard.emoji.ui.DockButton
import com.spacekeyboard.keyboard.emoji.ui.DockCategory
import com.spacekeyboard.keyboard.emoji.ui.DockKey
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon

private val Gap = Arrangement.spacedBy(2.dp)

@Composable
fun DecorDock(
    state: DecorSnapshot,
    category: DecorCategory,
    palette: ThemePalette,
    actions: DecorActions,
    onCategory: (DecorCategory) -> Unit,
) {
    DockBar(palette) {
        DockButton(PanelIcon.KEYBOARD, stringResource(R.string.decor_keys), palette, false, Modifier.size(DockKey), actions::onKeys)
        DockCategory(SpaceIcon.SYM_STARS, stringResource(R.string.decor_favorites), palette, state.favoritesOnly) { actions.onFavoritesOnly() }
        Box(Modifier.width(1.dp).height(24.dp).background(Color(palette.border).copy(alpha = 0.6f)))
        CategoryRow(category, state.language, palette, onCategory, Modifier.weight(1f))
        DockButton(PanelIcon.LANGUAGE, stringResource(R.string.decor_language), palette, false, Modifier.size(DockKey)) {
            actions.onLanguage(state.language.other())
        }
    }
}

@Composable
private fun CategoryRow(
    category: DecorCategory,
    language: Language,
    palette: ThemePalette,
    onCategory: (DecorCategory) -> Unit,
    modifier: Modifier,
) {
    val categories = DecorCategory.forLanguage(language)
    val listState = rememberLazyListState()
    LaunchedEffect(category, language) { listState.animateScrollToItem(categories.indexOf(category).coerceAtLeast(0)) }
    LazyRow(modifier = modifier, state = listState, horizontalArrangement = Gap, verticalAlignment = Alignment.CenterVertically) {
        items(categories.size) { index ->
            val item = categories[index]
            DockCategory(item.icon(), item.label(), palette, item == category) { onCategory(item) }
        }
    }
}

private fun Language.other(): Language = if (this == Language.EN) Language.AR else Language.EN
