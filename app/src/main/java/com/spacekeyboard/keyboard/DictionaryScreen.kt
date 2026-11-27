package com.spacekeyboard.keyboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore
import com.spacekeyboard.keyboard.core.suggest.WordStats
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val MAX_ROWS = 100
private const val EXPORT_FILE = "space-keyboard-dictionary.txt"
private val TEXT_TYPES = arrayOf("text/plain", "application/octet-stream")

@Composable
fun DictionaryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SuggestionStore(context) }
    var words by remember { mutableStateOf(store.words()) }
    var query by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    val addedText = stringResource(R.string.dictionary_added)
    val invalidText = stringResource(R.string.dictionary_invalid)
    ScreenFrame(R.string.dictionary_title, onBack) {
        BodyText(R.string.dictionary_note)
        DictionaryField(query) { query = it; notice = null }
        SpacePrimaryButton(stringResource(R.string.dictionary_add), {
            val added = store.addWord(query)
            if (added) {
                query = ""
                words = store.words()
            }
            notice = if (added) addedText else invalidText
        }, icon = SpaceIcon.ADD)
        BodyText(R.string.dictionary_files_note)
        DictionaryFileButtons(store, { notice = it }, { words = store.words() })
        notice?.let { DictionaryNotice(it) }
        DictionaryList(words, query.trim().lowercase()) {
            store.removeWord(it)
            words = store.words()
        }
    }
}

@Composable
private fun DictionaryFileButtons(store: SuggestionStore, onNotice: (String) -> Unit, onChanged: () -> Unit) {
    val context = LocalContext.current
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) onNotice(DictionaryFiles.write(context, uri, store))
    }
    val emptyText = stringResource(R.string.dictionary_export_empty)
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            onNotice(DictionaryFiles.read(context, uri, store))
            onChanged()
        }
    }
    SpaceButton(stringResource(R.string.dictionary_import), { importer.launch(TEXT_TYPES) })
    SpaceButton(stringResource(R.string.dictionary_export), {
        if (store.words().isEmpty()) onNotice(emptyText) else exporter.launch(EXPORT_FILE)
    })
}

@Composable
private fun DictionaryNotice(text: String) {
    SpaceText(
        text = text,
        style = SpaceTheme.typography.title,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = SpaceTheme.colors.primary,
    )
}

@Composable
private fun DictionaryList(words: List<String>, query: String, onDelete: (String) -> Unit) {
    val shown = if (query.isEmpty()) words else words.filter { it.contains(query) }
    when {
        words.isEmpty() -> AutoFixNote(R.string.dictionary_empty)
        shown.isEmpty() -> AutoFixNote(R.string.dictionary_no_match)
        else -> {
            SpaceText(stringResource(R.string.dictionary_count, shown.size), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
            LookCard(Modifier.fillMaxWidth()) {
                shown.take(MAX_ROWS).forEach { word -> DictionaryRow(word) { onDelete(word) } }
            }
            if (shown.size > MAX_ROWS) SpaceText(stringResource(R.string.dictionary_more, MAX_ROWS), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun DictionaryField(value: String, onChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    ToolField(Modifier.tapToFocus(focus)) {
        Column(modifier = Modifier.padding(12.dp)) {
            SpaceText(stringResource(R.string.dictionary_hint), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= WordStats.MAX_WORD) onChange(it) },
                singleLine = true,
                textStyle = SpaceTheme.typography.body.copy(color = SpaceTheme.colors.onSurface),
                cursorBrush = SolidColor(SpaceTheme.colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        }
    }
}

@Composable
private fun DictionaryRow(word: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceText(word, SpaceTheme.typography.title, Modifier.weight(1f))
        SpaceText(
            text = stringResource(R.string.dictionary_delete),
            style = SpaceTheme.typography.titleSmall,
            color = SpaceTheme.colors.primary,
            modifier = Modifier.clickable(role = Role.Button, onClick = onDelete).minTouchTarget(),
        )
    }
}
