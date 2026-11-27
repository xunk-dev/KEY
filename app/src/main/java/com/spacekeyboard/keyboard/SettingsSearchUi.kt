package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.search.SettingsSearch
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntry
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntryList
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private class SearchEntry(val label: String, val keywords: String, val screen: Screen)

@Composable
internal fun SearchField(value: String, onChange: (String) -> Unit) {
    val look = SpaceTheme.look
    val hint = stringResource(R.string.search_hint)
    val muted = SpaceTheme.colors.onSurfaceVariant
    val focus = remember { FocusRequester() }
    LookCard(
        modifier = Modifier.fillMaxWidth().tapToFocus(focus),
        radius = look.searchRadius,
        fill = look.searchFill,
        edge = look.searchEdge,
        edgeWidth = if (look.searchEdge == Color.Transparent) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = look.searchHeight).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpaceIconView(SpaceIcon.SEARCH, muted, size = 20.dp)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = SpaceTheme.typography.body.copy(color = SpaceTheme.colors.onSurface),
                cursorBrush = SolidColor(SpaceTheme.colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { inner ->
                    if (value.isEmpty()) SpaceText(hint, SpaceTheme.typography.body, color = muted)
                    inner()
                },
                modifier = Modifier.weight(1f).focusRequester(focus).semantics { contentDescription = hint },
            )
        }
    }
}

@Composable
internal fun SearchResults(query: String, onOpen: (Screen) -> Unit) {
    val entries = searchEntries()
    val found = SettingsSearch.filter(query, entries) { "${it.label} ${it.keywords}" }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (found.isEmpty()) BodyText(R.string.search_empty)
        else SpaceEntryList(found.map { SpaceEntry(it.label, it.screen.icon(), it.screen.tone()) { onOpen(it.screen) } })
    }
}

@Composable
private fun searchEntries(): List<SearchEntry> = homeEntries.map { (label, screen) ->
    SearchEntry(stringResource(label), keywordRes[screen]?.let { stringResource(it) }.orEmpty(), screen)
}
