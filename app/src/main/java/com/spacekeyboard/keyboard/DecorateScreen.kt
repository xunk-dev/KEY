package com.spacekeyboard.keyboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.DecorStyle
import com.spacekeyboard.keyboard.decorate.Language
import com.spacekeyboard.keyboard.decorate.rememberDecorSnapshot
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.spaceMaxWidth
import com.spacekeyboard.keyboard.dock.dockContentPadding

@Composable
fun DecorateScreen(controller: DecorController, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val state = rememberDecorSnapshot(controller)
    var sample by remember { mutableStateOf("") }
    val shown = sample.ifEmpty { if (state.language == Language.EN) "Space" else "فضاء" }
    AppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(SpaceTheme.look.canvas).systemBarsPadding().spaceMaxWidth(),
            contentPadding = dockContentPadding(SpaceTheme.look.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                BackLink(onBack)
            }
            item { SpaceText(stringResource(R.string.decorate_title), SpaceTheme.typography.display) }
            item { DecorateSampleField(sample) { sample = it } }
            item { ChipGrid(listOf(Language.EN, Language.AR), state.language, ::languageLabel, controller::setLanguage) }
            item { ActiveStatus(state.active, controller::clearActive) }
            item {
                SpaceText(
                    stringResource(R.string.decorate_note),
                    SpaceTheme.typography.bodySmall,
                    color = SpaceTheme.colors.onSurfaceVariant,
                )
            }
            items(state.styles.size) { index ->
                val style = state.styles[index]
                StyleRow(style, shown, style.id in state.favorites, style.id == state.active, controller)
            }
        }
    }
}

private fun languageLabel(language: Language): Int =
    if (language == Language.EN) R.string.decorate_lang_en else R.string.decorate_lang_ar

@Composable
private fun DecorateSampleField(value: String, onChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    ToolField(Modifier.tapToFocus(focus)) { SampleInput(value, onChange, focus) }
}

@Composable
private fun SampleInput(value: String, onChange: (String) -> Unit, focus: FocusRequester) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = SpaceTheme.typography.body.copy(color = SpaceTheme.colors.onSurface),
        cursorBrush = SolidColor(SpaceTheme.colors.primary),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(16.dp).focusRequester(focus),
    )
}

@Composable
private fun ActiveStatus(active: String?, onClear: () -> Unit) {
    if (active == null) {
        SpaceText(
            stringResource(R.string.decorate_none),
            SpaceTheme.typography.bodySmall,
            color = SpaceTheme.colors.onSurfaceVariant,
        )
    } else {
        SpaceButton(stringResource(R.string.decorate_stop), onClear)
    }
}

@Composable
private fun StyleRow(style: DecorStyle, sample: String, favorite: Boolean, active: Boolean, controller: DecorController) {
    val shape = RoundedCornerShape(SpaceTheme.look.fieldRadius.coerceAtMost(16.dp))
    val outline = if (active) SpaceTheme.colors.primary else SpaceTheme.colors.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SpaceTheme.look.card)
            .border(if (active) 2.dp else 1.dp, outline, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val preview = stringResource(R.string.decorate_style_desc, sample)
        SpaceText(
            style.apply(sample),
            SpaceTheme.typography.body,
            modifier = Modifier.weight(1f).semantics { contentDescription = preview },
        )
        val favoriteLabel = stringResource(R.string.decorate_favorite_desc)
        val useLabel = stringResource(R.string.decorate_use_desc)
        SpaceText(
            "★",
            SpaceTheme.typography.title,
            color = if (favorite) SpaceTheme.colors.primary else SpaceTheme.colors.onSurfaceVariant,
            modifier = Modifier.semantics { contentDescription = favoriteLabel }
                .clickable(role = Role.Button) { controller.toggleFavorite(style.id) }.minTouchTarget(),
        )
        SpaceText(
            stringResource(R.string.decorate_use),
            SpaceTheme.typography.titleSmall,
            color = SpaceTheme.colors.primary,
            modifier = Modifier.semantics { contentDescription = useLabel }
                .clickable(role = Role.Button) { controller.toggleActive(style.id) }.minTouchTarget(),
        )
    }
}
