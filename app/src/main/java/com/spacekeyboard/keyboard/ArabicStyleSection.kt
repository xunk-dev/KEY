package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.spacekeyboard.keyboard.core.layout.ArabicStyle
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.LayoutRepository
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController

private val PREVIEW_BG = Color(0xFFD9D9D9)
private val PREVIEW_KEY = Color(0xFFFFFFFF)
private val PREVIEW_FUNCTION = Color(0xFFB4B4B4)
private val PREVIEW_ENTER = Color(0xFF1E88E5)
private val PREVIEW_INK = Color(0xFF3A3A3A)

@Composable
fun ArabicStyleSection(selected: ArabicStyle, layouts: LayoutController) {
    val context = LocalContext.current
    val repository = remember(context) { LayoutRepository(context) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceSectionTitle(stringResource(R.string.section_arabic_style))
        SpaceText(stringResource(R.string.arabic_style_hint), SpaceTheme.typography.bodySmall)
        ArabicStyle.entries.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { style ->
                    StyleCard(style, style == selected, repository, Modifier.weight(1f)) { layouts.setArabicStyle(style) }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StyleCard(style: ArabicStyle, selected: Boolean, repository: LayoutRepository, modifier: Modifier, onClick: () -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val spec = remember(style) { repository.base("ar", style) }
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MiniKeyboard(spec)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Radio(selected)
            SpaceText(stringResource(style.labelRes), SpaceTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Radio(selected: Boolean) {
    val colors = SpaceTheme.colors
    val ring = if (selected) colors.primary else colors.outline
    Box(Modifier.size(20.dp).clip(CircleShape).border(2.dp, ring, CircleShape), contentAlignment = Alignment.Center) {
        if (selected) Box(Modifier.size(10.dp).clip(CircleShape).background(colors.primary))
    }
}

@Composable
private fun MiniKeyboard(spec: LayoutSpec) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(PREVIEW_BG).padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            spec.rows.forEach { row ->
                Row(Modifier.fillMaxWidth().height(22.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { key -> MiniKey(key, Modifier.weight(key.weight).fillMaxHeight()) }
                }
            }
        }
    }
}

@Composable
private fun MiniKey(key: KeySpec, modifier: Modifier) {
    val fill = when (key.action) {
        KeyAction.ENTER -> PREVIEW_ENTER
        KeyAction.COMMIT, KeyAction.SPACE -> PREVIEW_KEY
        else -> PREVIEW_FUNCTION
    }
    val ink = if (key.action == KeyAction.ENTER) PREVIEW_KEY else PREVIEW_INK
    val text = when {
        key.action == KeyAction.SPACE -> ""
        key.pair -> key.letters.joinToString(" ")
        else -> key.label
    }
    Box(modifier.clip(RoundedCornerShape(3.dp)).background(fill), contentAlignment = Alignment.Center) {
        BasicText(
            text,
            style = TextStyle(fontSize = 9.sp, color = ink, textAlign = TextAlign.Center),
            maxLines = 1,
            softWrap = false,
        )
    }
}
