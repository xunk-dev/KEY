package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.decorate.DecorSnapshot
import com.spacekeyboard.keyboard.decorate.DecorStyle
import com.spacekeyboard.keyboard.decorate.Language
import com.spacekeyboard.keyboard.decorate.R
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.ui.PanelKey

private const val SCRIM_ALPHA = 0.55f

@Composable
fun DecorPanel(state: DecorSnapshot, palette: ThemePalette, actions: DecorActions) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark) {
            Column(modifier = Modifier.fillMaxSize()) {
                DecorBar(state, palette, actions)
                Box(Modifier.weight(1f).fillMaxWidth().background(Color(palette.background).copy(alpha = SCRIM_ALPHA))) {
                    DecorList(state, palette, actions)
                }
            }
        }
    }
}

@Composable
private fun DecorBar(state: DecorSnapshot, palette: ThemePalette, actions: DecorActions) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BarKey(stringResource(R.string.decor_keys), palette, false, 48.dp) { actions.onKeys() }
        BarKey("EN", palette, state.language == Language.EN, 44.dp) { actions.onLanguage(Language.EN) }
        BarKey("AR", palette, state.language == Language.AR, 44.dp) { actions.onLanguage(Language.AR) }
        BarKey("★", palette, state.favoritesOnly, 44.dp) { actions.onFavoritesOnly() }
        Box(Modifier.weight(1f))
        if (state.active != null) BarKey(stringResource(R.string.decor_typing_off), palette, true, 96.dp) { actions.onClearActive() }
    }
}

@Composable
private fun BarKey(
    text: String,
    palette: ThemePalette,
    selected: Boolean,
    width: androidx.compose.ui.unit.Dp,
    description: String = text,
    onClick: () -> Unit,
) {
    PanelKey(palette, description, Modifier.width(width).height(48.dp), selected = selected, onClick = onClick) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.keyText))
    }
}

@Composable
private fun DecorList(state: DecorSnapshot, palette: ThemePalette, actions: DecorActions) {
    val sample = state.source.ifEmpty { if (state.language == Language.EN) "Space" else "فضاء" }
    Column(modifier = Modifier.fillMaxSize()) {
        SpaceText(
            stringResource(R.string.decor_warning),
            SpaceTheme.typography.bodySmall,
            color = Color(palette.label),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(state.styles.size) { index ->
                val style = state.styles[index]
                StyleRow(style, sample, style.id in state.favorites, style.id == state.active, palette, actions)
            }
        }
    }
}

@Composable
private fun StyleRow(style: DecorStyle, sample: String, favorite: Boolean, active: Boolean, palette: ThemePalette, actions: DecorActions) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val apply = stringResource(R.string.decor_apply_desc, sample)
        Box(
            Modifier.weight(1f).semantics { contentDescription = apply }
                .clickable(role = Role.Button) { actions.onApply(style) }.padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            SpaceText(
                style.apply(sample),
                SpaceTheme.typography.body,
                color = Color(palette.label),
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        BarKey("★", palette, favorite, 40.dp, stringResource(R.string.decor_favorite_desc)) { actions.onFavorite(style.id) }
        BarKey(stringResource(R.string.decor_type), palette, active, 64.dp) { actions.onActive(style.id) }
    }
}
