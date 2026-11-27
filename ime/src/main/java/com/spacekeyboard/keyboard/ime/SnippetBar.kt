package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutCheck
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon

private const val CARET = "|"
private const val TAIL = 80

@Composable
internal fun SnippetBar(palette: ThemePalette, snippets: SnippetController) {
    val ink = Color(palette.label)
    val fill = Color(ColorMath.mix(palette.background, palette.keyFill, 0.5f))
    Row(
        Modifier.fillMaxSize().padding(vertical = 3.dp).clip(RoundedCornerShape(14.dp)).background(fill).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(
            barText(snippets),
            modifier = Modifier.weight(1f),
            style = TextStyle(color = barInk(snippets, palette), fontSize = 13.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (snippets.step == SnippetStep.TEXT) PasteChip(palette, snippets::paste)
        if (snippets.composing) {
            MacroIconButton(SpaceIcon.DONE, stringResource(R.string.snip_save), Color(palette.glow), ink, snippets::confirm)
            MacroIconButton(SpaceIcon.CLOSE, stringResource(R.string.snip_cancel), ink, ink, snippets::cancel)
        }
    }
}

@Composable
private fun PasteChip(palette: ThemePalette, onClick: () -> Unit) {
    BasicText(
        stringResource(R.string.snip_paste),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(ColorMath.withAlpha(palette.glow, 0.22f)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        style = TextStyle(color = Color(palette.glow), fontSize = 12.sp),
        maxLines = 1,
    )
}

private fun barInk(snippets: SnippetController, palette: ThemePalette): Color =
    if (snippets.problem != null) RecordRed else Color(palette.label)

@Composable
private fun barText(snippets: SnippetController): String {
    snippets.problem?.let { return stringResource(problemRes(it)) }
    val blank = stringResource(
        if (snippets.step == SnippetStep.TRIGGER) R.string.snip_hint_trigger else R.string.snip_hint_text,
    )
    return when (snippets.step) {
        SnippetStep.TRIGGER -> if (snippets.trigger.isEmpty()) hintFor(snippets, blank) else typedTrigger(snippets)
        SnippetStep.TEXT -> snippets.trigger + " → " + if (snippets.text.isEmpty()) blank else snippets.text.takeLast(TAIL) + CARET
        SnippetStep.DONE -> stringResource(R.string.snip_saved, snippets.trigger)
        SnippetStep.OFF -> ""
    }
}

@Composable
private fun hintFor(snippets: SnippetController, blank: String): String =
    snippets.existing?.let { stringResource(R.string.snip_already, it) } ?: blank

@Composable
private fun typedTrigger(snippets: SnippetController): String {
    val typed = snippets.trigger + CARET
    return if (snippets.replaces) typed + "  ·  " + stringResource(R.string.snip_replaces) else typed
}

private fun problemRes(problem: ShortcutCheck): Int = when (problem) {
    ShortcutCheck.TEXT_TOO_LONG -> R.string.snip_too_long
    ShortcutCheck.FULL -> R.string.snip_full
    ShortcutCheck.TRIGGER_TOO_LONG -> R.string.snip_trigger_long
    ShortcutCheck.NO_TRIGGER, ShortcutCheck.NO_TEXT, ShortcutCheck.OK -> R.string.snip_empty_field
}
