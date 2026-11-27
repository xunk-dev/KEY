package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import com.spacekeyboard.keyboard.emoji.ui.PanelIconView

internal val TranslateKeyShape = RoundedCornerShape(14.dp)
internal val TranslateTouch = 48.dp
internal val TranslateGap = 8.dp
private val KeyInset = 2.dp
private val KeyBorder = 1.5.dp
private const val BORDER_ALPHA = 0.55f

internal fun Modifier.translateKeyLook(palette: ThemePalette, selected: Boolean, filled: Boolean = false): Modifier {
    val fill = when {
        filled -> Color(palette.glow)
        selected -> Color(palette.keyPressed)
        else -> Color(palette.keyFill)
    }
    val edge = if (selected || filled) Color(palette.glow) else Color(palette.border).copy(alpha = BORDER_ALPHA)
    return clip(TranslateKeyShape).background(fill).border(KeyBorder, edge, TranslateKeyShape)
}

@Composable
private fun KeyBox(
    palette: ThemePalette,
    description: String,
    modifier: Modifier,
    selected: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    Box(
        modifier = modifier
            .height(TranslateTouch)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            }
            .padding(vertical = KeyInset),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.translateKeyLook(palette, selected, filled).height(TranslateTouch - KeyInset * 2).widthIn(min = TranslateTouch - KeyInset * 2),
            Alignment.Center,
        ) { content() }
    }
}

@Composable
fun TranslateKey(
    text: String,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    description: String = text,
    icon: PanelIcon? = null,
    onClick: () -> Unit,
) {
    KeyBox(palette, description, modifier, selected, false, onClick) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) PanelIconView(icon, Color(palette.keyText), Modifier.padding(end = 6.dp))
            SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.keyText), maxLines = 1)
        }
    }
}

@Composable
internal fun TranslateChip(
    text: String,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    description: String = text,
    onClick: () -> Unit,
) {
    KeyBox(palette, description, modifier, selected, false, onClick) {
        SpaceText(text, SpaceTheme.typography.bodySmall, Modifier.padding(horizontal = 14.dp), Color(palette.keyText), maxLines = 1)
    }
}

@Composable
internal fun TranslateIconKey(
    icon: SpaceIcon,
    palette: ThemePalette,
    description: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    onClick: () -> Unit,
) {
    val tint = Color(if (filled) palette.background else palette.keyText)
    KeyBox(palette, description, modifier, false, filled, onClick) { SpaceIconView(icon, tint, Modifier.size(24.dp)) }
}

@Composable
internal fun TranslateLanguageKey(code: String, palette: ThemePalette, description: String, onClick: () -> Unit) {
    KeyBox(palette, description, Modifier, true, false, onClick) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            SpaceIconView(SpaceIcon.LANGUAGE, Color(palette.keyText), Modifier.padding(end = 6.dp), size = 20.dp)
            SpaceText(code.uppercase(), SpaceTheme.typography.body, color = Color(palette.keyText), maxLines = 1)
        }
    }
}
