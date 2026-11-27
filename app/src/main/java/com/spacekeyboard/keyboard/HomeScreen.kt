package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.KeyboardStatus
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntryGroup
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.spaceMaxWidth
import com.spacekeyboard.keyboard.dock.LocalDockInset
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController

@Composable
internal fun HomeScreen(
    status: KeyboardStatus,
    theme: ThemeController,
    layouts: LayoutController,
    onEnable: () -> Unit,
    onPick: () -> Unit,
    onOpen: (Screen) -> Unit,
) {
    AppTheme {
        var sample by remember { mutableStateOf("") }
        var query by remember { mutableStateOf("") }
        val look = SpaceTheme.look
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(look.canvas)
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = LocalDockInset.current)
                .spaceMaxWidth()
                .padding(look.screenPadding),
            verticalArrangement = Arrangement.spacedBy(look.sectionGap),
        ) {
            HomeHeader(status)
            HomeHero(status, onEnable, onPick)
            SampleField(value = sample, onChange = { sample = it })
            SearchField(query) { query = it }
            if (query.isBlank()) HomeGroups(theme, layouts, onOpen) else SearchResults(query, onOpen)
        }
    }
}

@Composable
private fun HomeGroups(theme: ThemeController, layouts: LayoutController, onOpen: (Screen) -> Unit) {
    LibrarySection(theme, layouts)
    homeGroups.forEach { group ->
        SpaceEntryGroup(stringResource(group.title), group.spaceEntries(onOpen), asList = group.asList)
    }
}

@Composable
internal fun SampleField(value: String, onChange: (String) -> Unit) {
    val look = SpaceTheme.look
    val hint = stringResource(R.string.sample_hint)
    val focus = remember { FocusRequester() }
    LookCard(
        modifier = Modifier.fillMaxWidth().tapToFocus(focus),
        radius = look.fieldRadius,
        fill = look.field,
        edge = look.fieldEdge,
        edgeWidth = look.fieldEdgeWidth,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = SpaceTheme.typography.body.copy(color = SpaceTheme.colors.onSurface),
            cursorBrush = SolidColor(SpaceTheme.colors.primary),
            decorationBox = { inner ->
                if (value.isEmpty()) SpaceText(hint, SpaceTheme.typography.body, color = SpaceTheme.colors.onSurfaceVariant)
                inner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .focusRequester(focus),
        )
    }
}
