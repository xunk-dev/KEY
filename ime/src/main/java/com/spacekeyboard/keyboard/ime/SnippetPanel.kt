package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView

@Composable
internal fun SnippetManagerContent(snippets: SnippetController, look: ButtonLook) {
    val p = look.palette
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MacroChip(stringResource(R.string.macro_back), false, p, snippets::closeManager)
            BasicText(stringResource(R.string.snip_title), style = TextStyle(color = Color(p.label), fontSize = 14.sp))
        }
        NewShortcutButton(p, snippets::startNew)
        BasicText(
            stringResource(if (snippets.items.isEmpty()) R.string.snip_empty else R.string.snip_note),
            style = TextStyle(color = Color(p.label).copy(alpha = 0.7f), fontSize = 12.sp),
        )
        BasicText(stringResource(R.string.snip_vars), style = TextStyle(color = Color(p.label).copy(alpha = 0.7f), fontSize = 12.sp))
        snippets.items.asReversed().forEach { (key, value) -> SnippetRow(key, value, snippets, p) }
    }
}

@Composable
private fun NewShortcutButton(p: ThemePalette, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(ColorMath.mix(p.background, p.keyFill, 0.9f)))
            .border(1.dp, Color(p.glow), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(SpaceIcon.ADD, Color(p.glow), Modifier, 18.dp)
        BasicText(stringResource(R.string.snip_new), style = TextStyle(color = Color(p.label), fontSize = 14.sp))
    }
}

@Composable
private fun SnippetRow(key: String, value: String, snippets: SnippetController, p: ThemePalette) {
    var asking by remember(key) { mutableStateOf(false) }
    val ink = Color(p.label)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(ColorMath.mix(p.background, p.keyFill, 0.75f)))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(key, style = TextStyle(color = Color(p.glow), fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            BasicText(
                value,
                style = TextStyle(color = ink.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Start),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (asking) {
            MacroChip(stringResource(R.string.macro_delete_confirm), true, p) { snippets.remove(key) }
            MacroIconButton(SpaceIcon.CLOSE, stringResource(R.string.macro_back), ink, ink) { asking = false }
        } else {
            MacroIconButton(SpaceIcon.INSERT, stringResource(R.string.snip_insert), Color(p.glow), ink) { snippets.insertItem(value) }
            MacroIconButton(SpaceIcon.PENCIL, stringResource(R.string.snip_edit), ink, ink) { snippets.edit(key, value) }
            MacroIconButton(SpaceIcon.TRASH, stringResource(R.string.macro_delete), ink, ink) { asking = true }
        }
    }
}
