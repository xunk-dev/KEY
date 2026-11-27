package com.spacekeyboard.keyboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.spaceMaxWidth
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController

@Composable
internal fun CustomizeScreen(theme: ThemeController, layouts: LayoutController, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    AppTheme {
        val prefs = rememberLayoutSnapshot(layouts).prefs
        var tab by rememberSaveable { mutableStateOf(CustomizeTab.STYLE) }
        var picked by remember { mutableStateOf(prefs.language) }
        var dragging by remember { mutableStateOf(false) }
        val language = picked.takeIf { it in prefs.languages } ?: prefs.language
        Column(Modifier.fillMaxSize().background(SpaceTheme.look.canvas).systemBarsPadding()) {
            CustomizeHeader(onBack)
            CustomizeBody(tab, theme, layouts, language, { picked = it }, dragging, { dragging = it }, Modifier.weight(1f))
            CustomizeTabBar(tab) { tab = it }
            CustomizePreview(tab == CustomizeTab.KEYS, theme, layouts, language)
        }
    }
}

@Composable
private fun CustomizeBody(
    tab: CustomizeTab,
    theme: ThemeController,
    layouts: LayoutController,
    language: String,
    onLanguage: (String) -> Unit,
    dragging: Boolean,
    onDragging: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val scroll = rememberScrollState()
    LaunchedEffect(tab) { scroll.scrollTo(0) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll, enabled = !dragging)
            .spaceMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TabIntro(tab)
        when (tab) {
            CustomizeTab.STYLE -> StyleTab(theme)
            CustomizeTab.COLORS -> ColorsTab(theme)
            CustomizeTab.KEYS -> KeysTab(layouts, language, onLanguage)
            CustomizeTab.SIZE -> SizeTab(layouts)
            CustomizeTab.TOOLS -> ToolsTab(layouts, onDragging)
            CustomizeTab.TOUCH -> TouchTab(layouts)
            CustomizeTab.SOUND -> SoundTab(layouts)
        }
    }
}

@Composable
private fun CustomizePreview(editing: Boolean, theme: ThemeController, layouts: LayoutController, language: String) {
    Box(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
        key(editing, language) {
            KeyboardPreview(
                theme = theme,
                layouts = layouts,
                fixedLayoutId = if (editing) language else null,
                editing = editing,
                onSwap = { a, b -> layouts.swap(language, a, b) },
            )
        }
    }
}
