package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipSnapshot
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.ui.edgeFade

private val SUGGESTIONS = listOf(
    R.string.clip_suggest_work,
    R.string.clip_suggest_personal,
    R.string.clip_suggest_links,
    R.string.clip_suggest_numbers,
    R.string.clip_suggest_addresses,
    R.string.clip_suggest_prayers,
)
private const val FADE_FROM = 3

@Composable
fun ClipNameSuggestions(state: ClipSnapshot, palette: ThemePalette, actions: ClipActions) {
    if (state.titling != null) return
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val haptics = rememberSpaceHaptics()
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val names = SUGGESTIONS.map { stringResource(it) }.filter { name ->
            !name.equals(state.naming, ignoreCase = true) && state.sections.none { it.name.equals(name, ignoreCase = true) }
        }
        LazyRow(
            modifier = Modifier.fillMaxSize().edgeFade(rtl, names.size > FADE_FROM),
            contentPadding = PaddingValues(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(names.size) { index ->
                val name = names[index]
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 3.dp)
                        .clip(CircleShape)
                        .background(Color(palette.keyFill))
                        .clickable(role = Role.Button) {
                            haptics.perform(SpaceHaptic.Tap)
                            actions.onNameSuggest(name)
                        }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) { SpaceText(name, SpaceTheme.typography.bodySmall, color = Color(palette.keyText), maxLines = 1) }
            }
        }
    }
}
