package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.decorate.DecorCategory
import com.spacekeyboard.keyboard.decorate.DecorSnapshot
import com.spacekeyboard.keyboard.decorate.Language
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode

private const val SCRIM_ALPHA = 0.55f

@Composable
fun DecorPanel(state: DecorSnapshot, palette: ThemePalette, actions: DecorActions) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
            var category by remember(state.language) { mutableStateOf(DecorCategory.ALL) }
            var typeMode by remember { mutableStateOf(false) }
            val visible = remember(state.styles, category) { state.styles.filter(category::matches) }
            val sample = state.source.ifEmpty { if (state.language == Language.EN) "Space" else "فضاء" }
            Column(modifier = Modifier.fillMaxSize()) {
                DecorTopBar(state, sample, typeMode, palette, actions) { typeMode = it }
                Box(Modifier.weight(1f).fillMaxWidth().background(Color(palette.background).copy(alpha = SCRIM_ALPHA))) {
                    DecorGrid(
                        visible,
                        sample,
                        state,
                        palette,
                        onPick = {
                            when {
                                it.id == state.active -> actions.onClearActive()
                                typeMode || state.active != null -> actions.onActive(it.id)
                                else -> actions.onApply(it)
                            }
                        },
                        onFavorite = actions::onFavorite,
                    )
                }
                DecorDock(state, category, palette, actions) { category = it }
            }
        }
    }
}
